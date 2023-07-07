package ru.adamishhe.javashhfiletransfer;

import org.json.JSONObject;
import ru.adamishhe.javashhfiletransfer.controllers.ReportController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileWorker {
    public static void work(InputStream fileInputStream, SimpleDateFormat dateFormat,
                                    ReportController reportController, String wellName, String serverAddress) {

        // Установка временной зоны в SimpleDateFormat
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Novosibirsk"));

        String dateString = null;
        long timestamp = 0;
        boolean foundDataSection = false;
        List<String> dataLines = new ArrayList<>();

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(fileInputStream))) {

            // Читаем содержимое файла
            String fileLine;

            while ((fileLine = fileReader.readLine()) != null) {
                if (fileLine.startsWith("DATE")) {
                    // Извлекаем дату из строки "DATE"
                    Pattern pattern = Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}-\\d{2}-\\d{2}");
                    Matcher matcher = pattern.matcher(fileLine);
                    if (matcher.find()) {
                        dateString = matcher.group();
                        try {
                            Date date = dateFormat.parse(dateString);
                            timestamp = date.getTime();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (foundDataSection) {
                    // Добавляем строки, находящиеся ниже ~A, в список dataLines
                    dataLines.add(fileLine);
                } else if (fileLine.startsWith("~A")) {
                    foundDataSection = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject json = DataTransfer.createJson(timestamp, dataLines, wellName);
        //System.out.println(json.toString());
        DataTransfer.sendJsonPostRequest(json, serverAddress, reportController);
    }
}
