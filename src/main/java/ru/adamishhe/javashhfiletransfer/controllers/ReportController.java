package ru.adamishhe.javashhfiletransfer.controllers;

import com.jcraft.jsch.Session;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import ru.adamishhe.javashhfiletransfer.SSHSessionManager;

public class ReportController {

    private Timeline timeline;

    @FXML
    private Button stopButton;

    @FXML
    private TextArea logArea;

    public void addLog(String text) {
        logArea.appendText(text);
    }

    public void setTimeline(Timeline timeline) {
        this.timeline = timeline;
    }

    @FXML
    private void stopButtonAction() {
        if (timeline != null) {
            timeline.stop();
        }
        SSHSessionManager sessionManager = SSHSessionManager.getInstance();
        Session session = sessionManager.getSession(); // Получаем сеанс SSH
        session.disconnect();
        logArea.appendText("disconnected from the device\n");
    }
}
