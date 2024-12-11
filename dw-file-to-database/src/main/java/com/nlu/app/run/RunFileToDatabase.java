package com.nlu.app.run;

import com.nlu.app.dto.FileStatus;
import com.nlu.app.service.CsvService;
import com.nlu.app.service.DatabaseService;
import com.nlu.app.status.StatusType;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class RunFileToDatabase {
    public static void main(String[] args) throws CsvValidationException, IOException {
        // Lấy ra ngày hôm nay
        LocalDate today = LocalDate.now();

        // Mockup các instance
        CsvService csvService = new CsvService();
        DatabaseService databaseService = new DatabaseService();

        // 1. Gọi tới hàm để lấy ra thông tin tên file và trạng thái của file
        List<FileStatus> files = databaseService.getFileStatus(today);
        if (files.isEmpty()) {
            System.out.println("Need to run crawl file application first! Exiting...");
            return;
        }

        // 2. Thực hiện việc lấy ra status cao nhất (Nằm trong hàm currentState)
        FileStatus currentFileStatus = databaseService.currentState(files);
        if (currentFileStatus == null) {
            System.out.println("No file to crawl. Exiting...");
            return;
        }

        if (!currentFileStatus.getStatus().equals(StatusType.PENDING_TO_SAVE_TEMP)) {
            System.out.println("Current status now is not matched with PENDING_TO_SAVE_TEMP. Program exited.");
            return;
        }

        // 3. Đã lấy ra status cao nhất, nên nếu equal => Có thể thực hiện save to temp ngay, ghi log RUNNING_LOAD_CP_DAILY
        String fileName = currentFileStatus.getFileName();
        boolean runningSuccess = databaseService.createLogStatus(fileName, StatusType.RUNNING_LOAD_CP_DAILY);
        if (!runningSuccess) {
            System.out.println("Error in RUNNING_LOAD_CP_DAILY. Exiting...");
            return;
        }

        // 4. Nếu thành công, lấy ra file CSV và thực hiện chuyển đổi dữ liệu từ file CSV sang bản staging.cp_daily
        // 4.1. Ghi log CONFIG_RETRIEVED sau khi lấy được cấu hình của file
        String csvPath = databaseService.getFileStoredDir(fileName);
        if (csvPath == null) {
            System.out.println("File not found! Exiting...");
            return;
        }
        boolean configSuccess = databaseService.createLogStatus(fileName, StatusType.CONFIG_RETRIEVED);
        if (!configSuccess) {
            System.out.println("Error in CONFIG_RETRIEVED. Exiting...");
            return;
        }

        // 4.2. Mảng với index 0 là tổng số row trong CSV, index 1 là số row insert thành công vào database
        int[] rowsInfo = csvService.csvDataToTempDatabase(csvPath);
        int rowsTotal = rowsInfo[0];
        int rowsInserted = rowsInfo[1];
        if (rowsInserted == 0) {
            System.out.println("Inserted to database fail. Exiting...");
            return;
        }
        System.out.println("Total rows: " + rowsTotal);
        System.out.println("Inserted rows: " + rowsInserted);

        // 5. Sau khi Insert thành công, thêm log vào bảng staging.logs
        boolean logSuccess = databaseService.createLogStatus(fileName, StatusType.PENDING_TO_CLEAN_DATA);
        if (logSuccess) {
            System.out.println("Log status changed to PENDING_TO_CLEAN_DATA");
            System.out.println("Exiting...");
        } else {
            System.out.println("Failed to change log status. Exiting...");
            databaseService.createLogStatus(fileName, StatusType.ERROR);
        }
    }
}
