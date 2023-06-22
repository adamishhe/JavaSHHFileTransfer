package ru.adamishhe.javashhfiletransfer;

import com.jcraft.jsch.*;
import ru.adamishhe.javashhfiletransfer.controllers.ReportController;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.Vector;

public class Downloader {
    public static void download(String localDirectory, Set<String> downloadedFiles, Session session) {
        ReportController reportController = new ReportController();
        try {
            String remoteDirectory = "/rpi_dts/temp_data/pool0";

            System.out.println("trying to open sftp channel");

            ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            System.out.println("sftp connected");

            // Получаем список файлов и директорий в указанной директории на удаленном сервере
            Vector<ChannelSftp.LsEntry> fileList = channelSftp.ls(remoteDirectory);

            // Итерируемся по списку файлов
            for (ChannelSftp.LsEntry file : fileList) {
                // Пропускаем текущую директорию, родительскую директорию и скачанные файлы.
                if (file.getFilename().equals(".") || file.getFilename().equals("..") || downloadedFiles.contains(file.getFilename())) {
                    continue;
                }
                // Формируем пути к файлам на удаленном и локальном серверах
                String remoteFilePath = remoteDirectory + "/" + file.getFilename();
                String localFilePath = localDirectory + "/" + file.getFilename();

                // Скачиваем файл с удаленного сервера
                channelSftp.get(remoteFilePath, localFilePath);
                downloadedFiles.add(file.getFilename());

                // Обрабатываем файл
                System.out.println("File downloaded: " + file.getFilename());
                reportController.addLog("File downloaded: " + file.getFilename() + "\n");

                System.out.println("trying to work with file");

                FileWorker.work(file.getFilename());
            }
            channelSftp.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
