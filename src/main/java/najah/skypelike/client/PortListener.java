package najah.skypelike.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *      this class extends Thread, it creates an always on thread
 *      that listening for the specific client ip and port which
 *      specified by the client in the login page, if any connection
 *      request is accepted it creates an object of ConnectionThread class
 *      that deal with this request.
 */
public class PortListener extends Thread {

    private final String ip;
    private final String port;
    public boolean isOn;
    private final ClientController controller;
    private ServerSocket socket;

    public PortListener(String ip, String port, ClientController controller) {
        this.ip = ip;
        this.port = port;
        this.controller = controller;
        isOn = true;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port))) {
            this.socket = serverSocket;
            while(isOn) {
                Socket socket = serverSocket.accept();
                new ConnectionThread(socket, controller).start();
            }
        }
        catch(IOException e) {
            if (isOn) e.printStackTrace();
        }
    }

    public void turnOff() {
        isOn = false;
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
