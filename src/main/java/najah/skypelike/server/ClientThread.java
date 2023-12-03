package najah.skypelike.server;

import javafx.scene.chart.PieChart;
import javafx.scene.paint.Color;
import najah.skypelike.common.User;

import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

/**
 *      class to represent thread of client connection with server
 *      in the multithreaded server. this class communicate with clients to
 *      logging-in and send info to clients like online users and add clients to UI
 */
public class ClientThread extends Thread {

    /**
     * class attributes
     */
    Socket socket;
    ServerController controller;
    Server server;
    private final BufferedReader input;
    private final PrintWriter output;


    /**
     * constructor to accept client connection socket and UI controller
     * and assigns it in attributes.
     *
     * @param socket - client-server socket
     * @param controller - UI controller to add online users
     * @param server - send server object to access server methods
     */
    public ClientThread(Socket socket, ServerController controller, Server server) throws IOException {
        this.socket = socket;
        this.controller = controller;
        this.server = server;
        input = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
    }

    /**
     * thread run method that reads from socket and check login validity
     * and send online users to client and add the client to online users list
     */
    @Override
    public void run() {
        controller.addStatusLine("client in socket now.\n", server.PROGRESS);
        try {

            String requestType = input.readLine().trim();
            if (requestType.equals("login")) loginHandler();
            else if (requestType.equals("logout")) logoutHandler();

        } catch (IOException e) {
            controller.addStatusLine("Error in communication with user.\n", server.ERR);
        } catch (NoSuchAlgorithmException e) {
            controller.addStatusLine("Error in Database encryption.\n", server.ERR);
        }
    }

    private void logoutHandler() throws IOException {
        String name = input.readLine();
        controller.removeUser(name);
        socket.close();
    }

    private void loginHandler() throws IOException, NoSuchAlgorithmException {
        String username = input.readLine();   // get server ip
        String password = input.readLine();   // get server port

        boolean isValid = Database.checkCredentials(username, password);

        if (isValid) {
            controller.addStatusLine("Successfully login.\n", server.SUCCESS);
            controller.addStatusLine("username: " + username +
                    " - password: " + password + "\n", server.NORMAL);

            output.println("OK");

            String clientIP = input.readLine();   // get client ip
            String clientPort = input.readLine(); // get client port

            ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
            objectOutput.writeObject(server.getOnlineUsers());

            socket.close();
            controller.addOnlineUser(new User(username, clientIP, clientPort));

        } else {
            controller.addStatusLine("Invalid credentials.\n", server.ERR);
            controller.addStatusLine("username: " + username +
                    " - password: " + password + "\n", server.NORMAL);
            output.println("UNAUTHORIZED");
        }
    }
}
