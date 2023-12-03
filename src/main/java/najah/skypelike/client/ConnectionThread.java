package najah.skypelike.client;

import najah.skypelike.common.Message;
import najah.skypelike.common.User;

import java.io.*;
import java.net.Socket;

public class ConnectionThread extends Thread {

    private final ClientController controller;
    private final Socket socket;

    public ConnectionThread(Socket socket, ClientController controller) {
        this.controller = controller;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {

            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            String typeOfRequest = ((String) input.readObject()).trim();
            switch (typeOfRequest) {
                case "message" -> messageHandler(input);
                case "login" -> loginHandler(input);
                case "logout" -> logoutHandler(input);
                case "deleteMessage" -> deleteMessageHandler(input);
                case "deleteChat" -> deleteChatHandler(input);
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteChatHandler(ObjectInputStream input) throws IOException, ClassNotFoundException {
        String chat = (String) input.readObject();
        controller.deleteChat(chat, false);
    }

    private void deleteMessageHandler(ObjectInputStream input) throws IOException, ClassNotFoundException {
        String chat = ((String) input.readObject());
        Integer index = (Integer) input.readObject();
        controller.deleteMessage(index, false, chat);
    }

    private void logoutHandler(ObjectInputStream input) throws IOException, ClassNotFoundException {
        String name = ((String) input.readObject()).trim();
        controller.removeUser(name);
    }

    private void messageHandler(ObjectInputStream input) throws IOException, ClassNotFoundException {
        Message msg = (Message) input.readObject();
        controller.addMessage(msg, true);
    }

    private void loginHandler(ObjectInputStream input) throws IOException, ClassNotFoundException {
        User user = (User) input.readObject();
        controller.addUser(user);
    }
}
