module najah.skypelike {
    requires javafx.controls;
    requires javafx.fxml;


    opens najah.skypelike to javafx.fxml;
    exports najah.skypelike;
    exports najah.skypelike.server;
    opens najah.skypelike.server to javafx.fxml;
}