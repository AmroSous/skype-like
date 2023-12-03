package najah.skypelike.server;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import najah.skypelike.common.User;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 *      Server class that implements Runnable to run the server
 *      in another thread from the FX Application thread.
 *      It has HashSet to maintain all online users.
 */
public class Server implements Runnable {

    /**
     * server attributes and flags
     */
    private final ConcurrentHashMap<String, User> onlineUsers;
    private final String serverIP;
    private final String serverPort;
    private final ServerController controller;
    private boolean isOn;
    private ServerSocket serverSocket;
    public final Color INFO = Color.BLUE, ERR = Color.RED, SUCCESS = Color.GREEN,
            PROGRESS = Color.ORANGE, NORMAL = Color.BLACK;

    /**
     * Server constructor assigns attributes to the server
     * and start thread to listen on the specified ip and port.
     *
     * @param ip - server ip
     * @param port - server port
     * @param controller -> the UI controller to access UI elements controls
     */
    public Server(String ip, String port, ServerController controller) {
        this.serverIP = ip;
        this.serverPort = port;
        this.controller = controller;
        onlineUsers = new ConcurrentHashMap<>();
        Thread thread = new Thread(this, "skype-server");
        isOn = true;
        thread.start();
    }

    /**
     * run method for server to listen on the specified ip and port number
     * for clients requests and logging in requests.
     * if it accepts connection request then it run this connection on another thread
     * for each client.
     */
    @Override
    public void run() {
        try (ServerSocket ss = new ServerSocket(Integer.parseInt(serverPort))) {

            controller.addStatusLine("Server started.\n", SUCCESS);
            controller.addStatusLine("Listening .. " + serverIP + " : " + serverPort + "\n", INFO);
            serverSocket = ss;
            while (isOn) {
                new ClientThread(serverSocket.accept(), controller, this).start();
            }

        } catch (IOException e) {
            if (isOn)
                controller.addStatusLine("Error: Cannot create server socket.\n", ERR);
            else
                controller.addStatusLine("Server stopped.\n", INFO);
        }
    }

    /**
     * this method is used by controller to stop the server thread.
     */
    public void stop() {
        isOn = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            controller.addStatusLine("Error while trying to stop server.\n", ERR);
        }
    }

    /**
     * getter for online users, used by ClientThread,
     * it is thread-safe method because different threads can access this getter.
     */
    public synchronized ConcurrentHashMap<String, User> getOnlineUsers() {
        return new ConcurrentHashMap<>(onlineUsers);
    }

    /**
     * setter on online users set to add online users by ClientThread class,
     * and send to all clients this new online user.
     * it is synchronized method to ensure thread-safety,
     * because different threads can try to put users.
     */
    public synchronized void addOnlineUser (User user) {
        onlineUsers.put(user.name(), user);
        onlineUsers.forEach((n, u) -> {
            if (n.equals(user.name())) return;
            try (Socket socket = new Socket(u.ip(), Integer.parseInt(u.port()));){

                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject("login");
                output.writeObject(user);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * to remove user from online users set, when the user logout from server,
     * and send to all clients that this user was logged out.
     * this method is used by ClientThread class, so it is synchronized to ensure
     * thread-safety
     */
    public synchronized void removeUser (String name) {
        onlineUsers.remove(name);
        onlineUsers.forEach((n, u) -> {
            try (Socket socket = new Socket(u.ip(), Integer.parseInt(u.port()));){

                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject("logout"); // request type
                output.writeObject(name);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
