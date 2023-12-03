module najah.skypelike {
    requires javafx.controls;
    requires javafx.fxml;

    opens najah.skypelike.client to javafx.fxml;
    exports najah.skypelike.client;
    opens najah.skypelike.server to javafx.fxml;
    exports najah.skypelike.server;
    exports najah.skypelike.common;
    opens najah.skypelike.common to javafx.fxml;
}