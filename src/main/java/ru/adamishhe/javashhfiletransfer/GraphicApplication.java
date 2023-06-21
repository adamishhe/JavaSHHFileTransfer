package ru.adamishhe.javashhfiletransfer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.adamishhe.javashhfiletransfer.controllers.VisualizationController;

import java.io.IOException;

public class GraphicApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GraphicApplication.class.getResource("/ru/adamishhe/javashhfiletransfer/visualization.fxml"));

        Parent root = fxmlLoader.load();
        VisualizationController visualizationController = fxmlLoader.getController();

        Scene scene = new Scene(root);

        stage.setTitle("Connection");
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}