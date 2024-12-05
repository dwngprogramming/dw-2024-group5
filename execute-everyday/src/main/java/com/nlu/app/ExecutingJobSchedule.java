package com.nlu.app;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class ExecutingJobSchedule {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm");
    // File này nằm trong resources/config.properties
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("config");

    public static void main( String[] args ) {
        // 1. Định nghĩa các file JAR cần gọi tuần tự
        List<String> jarFiles = Arrays.asList(
                "dw-crawl-to-csv-1.1.jar",
                "dw-file-to-database-1.1.jar",
                "dw-temp-to-clean-1.1.jar",
                "dw-clean-to-dim-1.1.jar"
        );

        // 2. Lấy ra Path của các folder từ môi trường
        String jarFolderPath = BUNDLE.getString("jars.path");
        String logFolderPath = BUNDLE.getString("logs.path");

        // Tạo tên file log với timestamp
        String logFileName = "scheduler-log-" + getTimestamp() + ".txt";
        File logFile = new File(logFolderPath + logFileName);

        try {
            if (!logFile.exists()) {
                logFile.createNewFile(); // Tạo file log mới nếu chưa tồn tại
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("Logging to: " + logFile.getAbsolutePath());

        // 3. Chạy tuần tự các file .jar
        for (String jar : jarFiles) {
            System.out.println("Running: " + jar);
            boolean success = runJar(jarFolderPath + jar, jar, logFile);
            if (!success) {
                System.out.println("Error running " + jar);
                break;
            }
        }
    }

    private static boolean runJar(String jarPath, String jarName, File logFile) {
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", jarPath);
        processBuilder.redirectErrorStream(true); // Gộp stderr vào stdout

        try (var logWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)))) {
            logWriter.println("[" + getTimestamp() + "] Starting JAR: " + jarName);
            logWriter.flush();

            String startLog = String.format("[%s] Starting JAR: %s%n", getTimestamp(), jarName);
            logWriter.write(startLog);
            System.out.println(startLog);

            // Start Process từ ProcessBuilder
            Process process = processBuilder.start();

            // Đọc và ghi log song song
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    logWriter.println(line);
                }
            }

            // Chờ quá trình kết thúc
            int exitCode = process.waitFor();
            logWriter.println("[" + getTimestamp() + "] Finished JAR: " + jarName + " with exit code " + exitCode);
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }
}