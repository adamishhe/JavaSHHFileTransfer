package ru.adamishhe.javashhfiletransfer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javafx.application.Platform;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.adamishhe.javashhfiletransfer.controllers.ReportController;

public class DataTransfer {
    public static JSONObject createJson(long timestamp, List<String> dataLines, String wellName) {
        JSONObject json = new JSONObject();

        // Записываем значение времени в виде таймштампа
        json.put("time", timestamp / 1000);

        // Формируем массив глубин и температур
        JSONArray depthArray = new JSONArray();
        JSONArray tempArray = new JSONArray();
        for (String dataLine : dataLines) {
            String[] parts = dataLine.split(" ");
            if (parts.length == 2) {
                depthArray.put(Double.parseDouble(parts[0]));
                tempArray.put(Double.parseDouble(parts[1]));
            }
        }
        json.put("depth", depthArray);
        json.put("temp", tempArray);

        // Записываем значение wellName
        json.put("place", wellName);
        return json;
    }
    public static void sendJsonPostRequest(JSONObject json, String url, ReportController reportController) {
        try {
            // Создание объекта URL из строки URL
            URL apiUrl = new URL(url);

            // Открытие соединения HttpURLConnection
            HttpURLConnection connection = null;

            connection = (HttpURLConnection) apiUrl.openConnection();


            // Настройка соединения
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Отправка JSON-данных
            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                outputStream.writeBytes(json.toString());
                outputStream.flush();
            }

            // Получение и обработка ответа
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                // успешный ответ сервера
                System.out.println("JSON successfully sent");
                Platform.runLater(() -> {
                    reportController.addLog("JSON successfully sent" + "\n");
                });
            } else {
                // ошибка в ответе сервера
                System.out.println("Failed to send JSON. Response code: " + responseCode);
                Platform.runLater(() -> {
                    reportController.addLog("Failed to send JSON. Response code: " + responseCode + "\n");
                });
            }

            // Закрытие соединения
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
