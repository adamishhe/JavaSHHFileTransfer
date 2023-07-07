package ru.adamishhe.javashhfiletransfer.controllers;

import com.jcraft.jsch.ChannelSftp;
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
import ru.adamishhe.javashhfiletransfer.FileChecker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    private String getRequest;
    private String serverAddress;
    private String wellName;

    private Session session;

    private ChannelSftp sftpChannel;

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

    public void setFolder(String getRequest) {
        this.getRequest = getRequest;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void setWellName(String wellName) {
        this.wellName = wellName;
    }

    @FXML
    private void initialize() {
        stopButton.setOnAction(event -> stopButtonAction());
    }

    public void startTransfer() {

        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH-mm-ss");

            Date targetDateTime = getDateFromServer(dateFormat);

            JSch jsch = new JSch();
            session = jsch.getSession(username, hostname, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            System.out.println("Connected");

            FileChecker.checkNewFiles(getRequest, session, targetDateTime,this, dateFormat, sftpChannel,
                                                                                        wellName, serverAddress);
            System.out.println("trying to circle");
            timeline = new Timeline(new KeyFrame(Duration.seconds(refreshTime), new EventHandler<>() {
                @Override
                public void handle(ActionEvent event) {
                    Date targetDateTime = null;
                    try {
                        targetDateTime = getDateFromServer(dateFormat);
                    } catch (ParseException | IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("call checkNewFiles method");
                    FileChecker.checkNewFiles(getRequest, session, targetDateTime, ReportController.this,
                            dateFormat, sftpChannel, wellName, serverAddress);
                }
            }));
            // Установка повторения бесконечного количества раз
            timeline.setCycleCount(Timeline.INDEFINITE);

            // Запуск Timeline
            timeline.play();
        } catch (IOException | ParseException | JSchException e) {
            e.printStackTrace();
        }
    }

    private Date getDateFromServer(SimpleDateFormat dateFormat) throws ParseException, IOException {

        // Создаем объект URL с указанным адресом
        URL url = new URL(getRequest);

        // Открываем соединение
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Устанавливаем метод запроса
        connection.setRequestMethod("GET");

        // Получаем ответный код
        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        // Читаем ответ от сервера
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder response = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Выводим полученный ответ
        System.out.println("Response: " + response.toString());

        int startIndex = response.toString().lastIndexOf(":");
        int endIndex = response.toString().lastIndexOf(".");
        String targetDateTimeString = response.substring(startIndex + 1, endIndex);

        Date timestampDate = new Date(Long.parseLong(targetDateTimeString)*1000);


        String target = dateFormat.format(timestampDate);
        Date targetDateTime = dateFormat.parse(target);

        connection.disconnect();

        return targetDateTime;
    }

    @FXML
    private void stopButtonAction() {
        if (timeline != null) {
            timeline.stop();
        }
        if (sftpChannel != null && sftpChannel.isConnected()) {
            sftpChannel.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        Platform.runLater(() -> {
            logArea.appendText("disconnected from the device\n");
        });
    }
}
