package najah.skypelike.server;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import najah.skypelike.common.Functions;
import najah.skypelike.common.OnlineUserController;
import najah.skypelike.common.User;

import java.io.IOException;

/**
 *      This is the controller class in view control model.
 *      It controls the server GUI and handle user clicks and inputs.
 */
public class ServerController {

    /**
     * server object reference.
     */
    private Server server;

    /**
     * GUI elements.
     */
    @FXML
    public VBox onlineUsersBox;
    @FXML
    public TextField serverIpField;
    @FXML
    public TextField serverPortField;
    @FXML
    public TextFlow statusArea;
    @FXML
    public Button startServer;
    @FXML
    public Button stopServer;

    /**
     * this method is to add logged-in users to the list of
     * online users in the application UI, by loading an object from
     * online-user.fxml file
     *
     * @param user - User to add
     */
    public synchronized void addOnlineUser(User user) {
        paintOnlineUser(user);
        server.addOnlineUser(user);
    }

    private void paintOnlineUser(User user) {
        FXMLLoader fxmlLoader = new FXMLLoader(OnlineUserController.class.getResource("online-user.fxml"));
        Platform.runLater(() -> {
            try {
                Parent root = fxmlLoader.load();
                OnlineUserController userController = fxmlLoader.getController();
                userController.setIp(user.ip(), user.port());
                userController.setName(user.name());
                Platform.runLater(() -> onlineUsersBox.getChildren().add(root));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * this method is used to add status lines to the status text area
     * in server GUI with specific message color.
     * it is synchronized method to ensure thread-safety because
     * this method is used by different threads.
     *
     * @param msg - the message to print
     * @param color - color of message
     */
    public synchronized void addStatusLine(String msg, Color color) {
        Text text = new Text(msg);
        text.setFill(color);
        text.setFont(Font.font("Monospaced", FontPosture.REGULAR, 14));
        Platform.runLater(() -> { statusArea.getChildren().add(text); });
    }

    /**
     * event handler when click on Start Listening button
     * it takes the ip and port specified by the user and checks its validity
     * if inputs are correct it initializes object of server thread and run it
     * on these operands.
     */
    @FXML
    public void onStartServer() {

        String ip = serverIpField.getText();
        String port = serverPortField.getText();

        if (!Functions.validateIP(ip)) {
            addStatusLine("Invalid IP format.\n", Color.RED);
        }
        else if (!Functions.validatePort(port)) {
            addStatusLine("Invalid port number format.\n", Color.RED);
        }
        else {
            startServer.setDisable(true);
            stopServer.setDisable(false);
            server = new Server(ip, port, this);
        }
    }

    /**
     * event handler when clicking on stop server button,
     * it calls stop method in Server class.
     */
    @FXML
    public void onStopServer() {
        server.stop();
        server = null;
        stopServer.setDisable(true);
        startServer.setDisable(false);
        onlineUsersBox.getChildren().clear();
    }

    public synchronized void removeUser(String name) {
        server.removeUser(name);
        addStatusLine("User logged out.\n", server.SUCCESS);
        addStatusLine("name: " + name + "\n", server.NORMAL);
        Platform.runLater(() -> {
            onlineUsersBox.getChildren().clear();
            server.getOnlineUsers().forEach((n, u) -> {
                paintOnlineUser(u);
            });
        });
    }
}