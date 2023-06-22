package ru.adamishhe.javashhfiletransfer.controllers;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import ru.adamishhe.javashhfiletransfer.Downloader;
import ru.adamishhe.javashhfiletransfer.SSHSessionManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class VisualizationController {


    @FXML
    private Button browseButton;

    @FXML
    private Button submitButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button quitButton;

    @FXML
    private Button deleteButton;

    @FXML
    private ListView<String> listView;

    @FXML
    private TextField folderTextField;

    @FXML
    private TextField hostnameTextField;

    @FXML
    private TextField portTextField;

    @FXML
    private TextField refreshTextField;

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField serverAddressTextField;

    @FXML
    private TextField wellNameTextField;

    @FXML
    private TextField savedTextField;

    @FXML
    public void initialize() {
        File directory = new File("saved");
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    String itemName = fileName.substring(0, fileName.lastIndexOf('.'));
                    listView.getItems().add(itemName);
                }
            }
        }
    }

    @FXML
    private void browseButtonAction() {

        // Создание диалогового окна выбора директории
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose directory");

        // Отображение диалогового окна и получение выбранной директории
        Stage stage = (Stage) browseButton.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(stage);

        // Проверка выбранной директории и выполнение необходимых действий
        if (selectedDirectory != null) {
            folderTextField.setText(selectedDirectory.getAbsolutePath());
        }

    }

    @FXML
    private void submitButtonAction(ActionEvent event) throws IOException {

        Set<String> downloadedFiles = new HashSet<>();

        try {
            // Получаем список файлов в директории
            Files.list(Paths.get(folderTextField.getText()))
                    .filter(Files::isRegularFile)
                    .forEach(path -> downloadedFiles.add(path.getFileName().toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }


        String hostname = hostnameTextField.getText();
        int port = Integer.parseInt(portTextField.getText());
        int refreshTime = Integer.parseInt(refreshTextField.getText());
        String username = usernameTextField.getText();
        String password = passwordField.getText();
        String folder = folderTextField.getText();
        String serverAddress = serverAddressTextField.getText();
        String wellName = wellNameTextField.getText();

        // Получение Stage из текущего события
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Data transfer");

        // Загрузка FXML-файла в новое окно
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/adamishhe/javashhfiletransfer/report.fxml"));
        Parent root = loader.load();

        ReportController reportController = loader.getController();

        // Создание новой сцены с загруженным макетом из FXML-файла
        Scene newScene = new Scene(root);

        // Замена текущей сцены на новую
        stage.setScene(newScene);
        stage.show();


        // 6. спросить про http запрос.
        // 7. реализовать отправку запаршенных данных на сервер
        //---------------------------
        // не работает кнопка save
        // не работает log area
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(username, hostname, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            System.out.println("Connected");

            SSHSessionManager sessionManager = SSHSessionManager.getInstance();
            sessionManager.setSession(session);

            Downloader.download(folder, downloadedFiles, session);

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(refreshTime), new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    System.out.println("call download method");
                    Downloader.download(folder, downloadedFiles, session);
                }
            }));
            // Установка повторения бесконечного количества раз
            timeline.setCycleCount(Timeline.INDEFINITE);

            // Запуск Timeline
            timeline.play();

            reportController.setTimeline(timeline);
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void saveButtonAction() {
        String data = hostnameTextField.getText() + "\n" + portTextField.getText()
                + "\n" + refreshTextField.getText() + "\n" + usernameTextField.getText() + "\n"
                + passwordField.getText() + "\n" + folderTextField.getText() + "\n"
                + serverAddressTextField.getText() + "\n" + wellNameTextField.getText();

        String filePath = "saved/" + savedTextField.getText() + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false))) {
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!listView.getItems().contains(savedTextField.getText())) {
            listView.getItems().add(savedTextField.getText());
        }
    }

    @FXML
    private void listViewClicked() {
        String selectedValue = listView.getSelectionModel().getSelectedItem();
        if (selectedValue != null) {
            String filePath = "saved/" + selectedValue + ".txt";

            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                StringBuilder content = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }

                // Заполняем поля TextField данными из файла
                String[] data = content.toString().split("\n");
                savedTextField.setText(selectedValue);
                if (data.length >= 8) {
                    hostnameTextField.setText(data[0]);
                    portTextField.setText(data[1]);
                    refreshTextField.setText(data[2]);
                    usernameTextField.setText(data[3]);
                    passwordField.setText(data[4]);
                    folderTextField.setText(data[5]);
                    serverAddressTextField.setText(data[6]);
                    wellNameTextField.setText(data[7]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void quitButtonAction() {

        // Получение ссылки на текущую сцену
        Scene scene =quitButton.getScene();

        // Получение ссылки на текущий Stage
        Stage stage = (Stage) scene.getWindow();

        // Закрытие программы
        stage.close();
    }

    @FXML
    private void deleteButtonAction() {
        String selectedValue = listView.getSelectionModel().getSelectedItem();
        if (selectedValue != null) {
            // Удаляем элемент из ListView
            listView.getItems().remove(selectedValue);

            // Удаляем файл с соответствующим именем
            String filePath = "saved/" + selectedValue + ".txt";

            File file = new File(filePath);
            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("file deleted: " + filePath);
                } else {
                    System.out.println("file not deleted: " + filePath);
                }
            }
        }
    }
}
