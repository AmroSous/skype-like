package najah.skypelike.common;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import najah.skypelike.client.ClientController;

/**
 * controller for online-user-view.fxml view that represents one box
 * for online user.
 */
public class OnlineUserController {

    @FXML
    public Label ipLabel;
    @FXML
    public Label nameLabel;
    @FXML
    public Label deleteLabel;

    private ClientController clientController;

    @FXML
    public void onChatClicked() {
            clientController.setOpenedChat(nameLabel.getText());
    }

    @FXML
    public void onMouseEnter() {
        deleteLabel.setVisible(true);
    }

    @FXML
    public void onMouseExit() {
        deleteLabel.setVisible(false);
    }

    @FXML
    public void onDeleteClicked() {
        ButtonType meOnly = new ButtonType("Delete for me only.", ButtonBar.ButtonData.LEFT);
        ButtonType all = new ButtonType("Delete for all.", ButtonBar.ButtonData.RIGHT);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "choose type of deletion.", meOnly, all, cancel);
        alert.showAndWait();
        boolean isAll = (alert.getResult() == all);
        clientController.deleteChat(nameLabel.getText(), isAll);
    }

    public void setIp(String ip, String port) {
        ipLabel.setText(ip + " : " + port);
    }

    public void setName(String name) {
        nameLabel.setText(name);
    }

    public void setClientController(ClientController controller) {
        this.clientController = controller;
    }
}
