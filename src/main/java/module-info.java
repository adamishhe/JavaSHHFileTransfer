module ru.adamishhe.javashhfiletransfer {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.jcraft.jsch;
    requires java.base;


    opens ru.adamishhe.javashhfiletransfer to javafx.fxml;
    exports ru.adamishhe.javashhfiletransfer;
    exports ru.adamishhe.javashhfiletransfer.controllers;
    opens ru.adamishhe.javashhfiletransfer.controllers to javafx.fxml;
}