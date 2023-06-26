package ru.adamishhe.javashhfiletransfer.controllers;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.util.Duration;
import ru.adamishhe.javashhfiletransfer.Downloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class ReportController {

    private Timeline timeline;

    @FXML
    private Button stopButton;

    @FXML
    private TextArea logArea;

    private String hostname;
    private int port;
    private int refreshTime;
    private String username;
    private String password;
    private String folder;
    private String serverAddress;
    private String wellName;

    private Session session;

    public void addLog(String text) {
        logArea.appendText(text);
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setRefreshTime(int refreshTime) {
        this.refreshTime = refreshTime;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void setWellName(String wellName) {
        this.wellName = wellName;
    }

    public void startDownload() {
        System.out.println("startDownload()");
        Set<String> downloadedFiles = new HashSet<>();

        try {
            // Получаем список файлов в директории
            Files.list(Paths.get(folder))
                    .filter(Files::isRegularFile)
                    .forEach(path -> downloadedFiles.add(path.getFileName().toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, hostname, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            System.out.println("Connected");

            Downloader.download(folder, downloadedFiles, session, this);

            timeline = new Timeline(new KeyFrame(Duration.seconds(refreshTime), new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    System.out.println("call download method");
                    Downloader.download(folder, downloadedFiles, session, ReportController.this);
                }
            }));
            // Установка повторения бесконечного количества раз
            timeline.setCycleCount(Timeline.INDEFINITE);

            // Запуск Timeline
            timeline.play();

        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void stopButtonAction() {
        if (timeline != null) {
            timeline.stop();
        }
        session.disconnect();
        Platform.runLater(() -> {
            logArea.appendText("disconnected from the device\n");
        });
    }
}
