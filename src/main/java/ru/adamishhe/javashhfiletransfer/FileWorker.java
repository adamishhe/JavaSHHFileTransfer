package ru.adamishhe.javashhfiletransfer;

import ru.adamishhe.javashhfiletransfer.controllers.ReportController;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileWorker {
    public static void work(String lasFilePath) {

        String dateString = null;
        long timestamp = 0;
        boolean foundDataSection = false;
        List<String> dataLines = new ArrayList<>();
        ReportController reportController = new ReportController();

        try (BufferedReader reader = new BufferedReader(new FileReader(lasFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("DATE")) {
                    // Извлекаем дату из строки "DATE"
                    Pattern pattern = Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}-\\d{2}-\\d{2}");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        dateString = matcher.group();
                        String patternString = "dd.MM.yyyy HH-mm-ss";
                        SimpleDateFormat dateFormat = new SimpleDateFormat(patternString);
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
                    dataLines.add(line);
                } else if (line.startsWith("~A")) {
                    foundDataSection = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Делаем что-то с извлеченной датой
        reportController.addLog("Date: " + timestamp + "\n");

        for (String dataLine : dataLines) {
            String[] fields = dataLine.split("\\s+"); // Делим строку по пробелам
            double depth = Double.parseDouble(fields[0]);
            double temperature = Double.parseDouble(fields[1]);

            // Делаем что-то с извлеченными значениями
            reportController.addLog("Depth: " + depth + ", Temperature: " + temperature + "\n");
        }
        reportController.addLog("-----------------------------------------\n");
    }
}
