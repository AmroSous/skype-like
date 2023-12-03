package najah.skypelike.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import najah.skypelike.common.Functions;
import najah.skypelike.common.User;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 *      the controller for the login to server page,
 *      this page is the first page to load in client application.
 */
public class LoginController {

    /**
     * class attributes
     */
    @FXML
    public TextField serverIPField;
    @FXML
    public TextField serverPortField;
    @FXML
    public TextField usernameField;
    @FXML
    public TextField passwordField;
    @FXML
    public TextField clientIPField;
    @FXML
    public TextField clientPortField;
    @FXML
    public Label errorLabel;

    /**
     * the function to call when connect button pressed in login page
     * this function takes inputs from fields and validate it, then it
     * communicates with the server to login and take online users list.
     */
    @FXML
    public void onConnect() {
        // clear error label
        errorLabel.setText("");

        // read inputs
        String serverIP = serverIPField.getText();
        String serverPort = serverPortField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String clientIP = clientIPField.getText();
        String clientPort = clientPortField.getText();

        // validate inputs
        if (!Functions.validateIP(serverIP)) {
            errorLabel.setText("Invalid server ip address."); return;
        } else if (!Functions.validatePort(serverPort)) {
            errorLabel.setText("Invalid server port number."); return;
        } else if (!Functions.validateIP(clientIP)) {
            errorLabel.setText("Invalid client ip address."); return;
        } else if (!Functions.validatePort(clientPort)) {
            errorLabel.setText("Invalid client port number."); return;
        }

        // try to connect
        try (Socket socket = new Socket(serverIP, Integer.parseInt(serverPort))) {
            BufferedReader input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            output.println("login"); // request type
            output.println(username);
            output.println(password);

            String response = input.readLine();

            if (!response.equals("OK")) {
                errorLabel.setText("Invalid username or password.");
                return;
            }

            // if success (OK)
            // send client ip and port

            output.println(clientIP);
            output.println(clientPort);

            // get online users
            ObjectInputStream objectInput = new ObjectInputStream(socket.getInputStream());
            @SuppressWarnings("unchecked")
            ConcurrentHashMap<String, User> onlineUsers = (ConcurrentHashMap<String, User>)objectInput.readObject();

            // close socket with server
            socket.close();

            // start client application with this information
            FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource("client-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 858, 580);
            Stage currStage = (Stage) serverIPField.getScene().getWindow();
            currStage.setScene(scene);
            ClientController controller = fxmlLoader.getController();
            controller.setOnlineUsers(onlineUsers);
            controller.setMe(new User(username, clientIP, clientPort));
            controller.setServerIp(serverIP);
            controller.setServerPort(serverPort);
            PortListener listenerThread = new PortListener(clientIP, clientPort, controller);
            controller.setPortListener(listenerThread);
            listenerThread.start();
        }
        catch (IOException e) {
            errorLabel.setText("Fail to make connection."); return;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
