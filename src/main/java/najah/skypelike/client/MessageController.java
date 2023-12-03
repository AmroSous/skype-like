package najah.skypelike.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import najah.skypelike.common.Message;

public class MessageController {

    private final Color MYSIDE_COLOR = Color.RED;
    private final Color HISSIDE_COLOR = Color.GREEN;
    private ClientController clientController;
    private Message message;

    @FXML
    public TextFlow messageBox;
    @FXML
    public Label timeLabel;
    @FXML
    public Label deleteLabel;

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
        clientController.deleteMessage(message.index(), isAll);
    }

    public void putMessage(Message msg, boolean side) {
        this.message = msg;
        Text text = new Text(msg.msg());

        text.setFill(side ? HISSIDE_COLOR : MYSIDE_COLOR);
        text.setFont(Font.font("Arial", FontPosture.REGULAR, 16));
        messageBox.getChildren().add(text);
        timeLabel.setText(msg.timestamp());
    }

    public void setClientController(ClientController controller) {
        this.clientController = controller;
    }
}
