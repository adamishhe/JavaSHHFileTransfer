package ru.adamishhe.javashhfiletransfer;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import javafx.application.Platform;
import ru.adamishhe.javashhfiletransfer.controllers.ReportController;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class FileChecker {
    public static void checkNewFiles(String localDirectory, Session session, Date targetDateTime,
                                     ReportController reportController, SimpleDateFormat dateFormat,
                                     ChannelSftp sftpChannel, String wellName, String serverAddress) {
        try {
            // Создаем канал для выполнения команд
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            // Формируем команду для поиска файлов
            String command = "find /rpi_dts/temp_data/pool0 -type f -name '*averaged*'";

            // Выполняем команду
            channel.setCommand(command);
            channel.connect();

            Date currentDate = new Date();

            String currentTime = dateFormat.format(currentDate);

            Platform.runLater(() -> {
                reportController.addLog( currentTime + "\n");
            });

            // Получаем вывод команды
            InputStream inputStream = channel.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            Vector<String> fileNames = new Vector<>();

            // Читаем названия найденных файлов
            while ((line = reader.readLine()) != null) {
                fileNames.add(line);
            }

            // Закрываем канал
            channel.disconnect();

            // Читаем содержимое найденных файлов
            for (String fileName : fileNames) {
                // Парсим дату из названия файла
                String fileDateTimeString = extractDateTimeFromFileName(fileName);
                Date fileDateTime = dateFormat.parse(fileDateTimeString);

                // Проверяем, что дата в названии файла позднее целевой даты
                if (fileDateTime.after(targetDateTime)) {

                    String fileNameWithoutPath = getFileNameWithoutPath(fileName);

                    Platform.runLater(() -> {
                        reportController.addLog(fileNameWithoutPath + "\n");
                    });
                    // Создаем новый канал для чтения файлов
                    sftpChannel = (ChannelSftp) session.openChannel("sftp");
                    sftpChannel.connect();

                    InputStream fileInputStream = sftpChannel.get(fileName);

                    FileWorker.work(fileInputStream, dateFormat, reportController, wellName, serverAddress);
                    sftpChannel.disconnect();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getFileNameWithoutPath(String fileName) {
        int lastSeparatorIndex = fileName.lastIndexOf("/");
        if (lastSeparatorIndex != -1) {
            return fileName.substring(lastSeparatorIndex + 1);
        } else {
            return fileName;
        }
    }

    private static String extractDateTimeFromFileName(String fileName) {
        int startIndex = fileName.lastIndexOf("/");
        int endIndex = fileName.lastIndexOf(" ");
        return fileName.substring(startIndex + 1, endIndex);
    }
}
