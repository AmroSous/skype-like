package najah.skypelike.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import najah.skypelike.common.Message;
import najah.skypelike.common.OnlineUserController;
import najah.skypelike.common.User;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 *      this is the controller for the main page of client application
 *      this view appear if the login passed successfully.
 */
public class ClientController {

     // data structure to save chats for this session of login
     // HashMap(Name, List(Messages(my/his, msg))) false -> me / true -> him
    private HashMap<String, List<Pair<Boolean, Message>>> chats;
    private HashMap<String, Integer> indexes;
    private ConcurrentHashMap<String, User> onlineUsers;
    private PortListener portListener;
    private String openedChat = "";
    private User me;

    @FXML
    public TextArea messageField;
    @FXML
    public VBox messagesBox;
    @FXML
    public VBox onlineUsersBox;
    @FXML
    public Pane chatArea;
    @FXML
    public Label chatNameLabel;
    private String serverIp;
    private String serverPort;

    @FXML
    private void onSend() {
        String msg = messageField.getText().trim();
        messageField.setText("");
        if (msg.isEmpty()) return;

        // create message
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestamp = LocalDateTime.now().format(formatter);

        Message message = new Message(me.name(), openedChat, msg, timestamp, indexes.get(openedChat));

        // add message to chats and GUI
        addMessage(message, false);

        // send message
        User to = onlineUsers.get(openedChat);
        try (Socket socket = new Socket(to.ip(), Integer.parseInt(to.port()))){

            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            output.writeObject("message");
            output.writeObject(message);

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void onLogout() {
        portListener.turnOff();
        try (Socket socket = new Socket(serverIp, Integer.parseInt(serverPort))) {
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            output.println("logout");
            output.println(me.name());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.exit(0); // exit the program
    }

    public void loadChat(String chat) {
        this.chatNameLabel.setText(chat);
        messagesBox.getChildren().clear();
        chats.get(chat).forEach(m -> {
            paintMessage(m.getKey(), m.getValue());
        });
    }

    public synchronized void addMessage(Message msg, boolean side) {
        String chat = side ? msg.from() : msg.to();
        chats.get(chat).add(new Pair<>(side, msg));
        indexes.put(openedChat, indexes.get(openedChat) + 1);
        if (openedChat.equals(side ? msg.from() : msg.to())) {
            paintMessage(side, msg);
        }
    }

    private void paintMessage(boolean side, Message msg) {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientController.class.getResource(
                side ? "his-side-message.fxml" : "my-side-message.fxml"));
        Platform.runLater(() -> {
            try {
                messagesBox.getChildren().add(fxmlLoader.load());
                MessageController controller = fxmlLoader.getController();
                controller.putMessage(msg, side);
                controller.setClientController(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public synchronized void addUser(User user) {
        onlineUsers.put(user.name(), user);
        chats.put(user.name(), new ArrayList<>());
        indexes.put(user.name(), 0);
        paintOnlineUser(user);
    }

    private void paintOnlineUser(User user) {
        FXMLLoader fxmlLoader = new FXMLLoader(OnlineUserController.class.getResource("online-user.fxml"));
        Parent root;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        OnlineUserController userController = fxmlLoader.getController();
        userController.setIp(user.ip(), user.port());
        userController.setName(user.name());
        userController.setClientController(this);
        Platform.runLater(() -> onlineUsersBox.getChildren().add(root));
    }

    /**
     * get online users and assigns it to class attributes,
     * and fill online users list in client GUI.
     *
     * @param users - HashSet of users
     */
    public void setOnlineUsers(ConcurrentHashMap<String, User> users) {
        this.onlineUsers = users;
        this.chatArea.setVisible(false);
        chats = new HashMap<>();
        indexes = new HashMap<>();
        users.forEach((name, u) -> {
            addUser(u);
        });
    }

    /**
     * setter for the port listener of this client application.
     * the controller uses this variable to stop the client program and
     * stop the listener thread on the peer port.
     *
     * @param listener -
     */
    public void setPortListener (PortListener listener) {
        this.portListener = listener;
    }

    public void setOpenedChat(String chat) {
        this.openedChat = chat;
        this.chatArea.setVisible(true);
        loadChat(chat);
    }

    public void setMe(User user) { me = user; }

    public void setServerIp(String serverIP) {
        this.serverIp = serverIP;
    }

    public void setServerPort(String port) {
        this.serverPort = port;
    }

    public synchronized void removeUser(String name) {
        onlineUsers.remove(name);
        chats.remove(name);
        Platform.runLater(() -> {
            if (name.equals(openedChat)) chatArea.setVisible(false);
            onlineUsersBox.getChildren().clear();
            onlineUsers.forEach((n, u) -> paintOnlineUser(u));
        });
    }

    public synchronized void deleteMessage(int index, boolean all, String chat) {
        chats.get(chat).removeIf(p -> p.getValue().index() == index);
        if (openedChat.equals(chat)) Platform.runLater(() -> loadChat(chat));
        if (all) {
            try (Socket socket = new Socket(onlineUsers.get(chat).ip(), Integer.parseInt(onlineUsers.get(chat).port()))){

                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject("deleteMessage");
                output.writeObject(me.name());
                output.writeObject(index);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized void deleteMessage(int index, boolean all) {
        deleteMessage(index, all, openedChat);
    }

    public synchronized void deleteChat(String chat, boolean all) {
        chats.get(chat).clear();
        if (chat.equals(openedChat)) Platform.runLater(() -> messagesBox.getChildren().clear());
        if (all) {
            try (Socket socket = new Socket(onlineUsers.get(chat).ip(), Integer.parseInt(onlineUsers.get(chat).port()))){

                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject("deleteChat");
                output.writeObject(me.name());

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}