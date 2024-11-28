package com.nlu.app;

import com.nlu.app.dto.FileStatus;
import com.nlu.app.service.DatabaseService;
import com.nlu.app.status.StatusType;

import java.time.LocalDate;
import java.util.Optional;

public class App {
    public static void main( String[] args ) {
        // Ngày hiện tại
        LocalDate today = LocalDate.now();

        // Mockup service
        DatabaseService ds = new DatabaseService();

        // 1. Kiểm tra trạng thái của file trong log
        // 1.1. Tìm trạng thái hiện tại của file trong hôm nay (thực thi trong hàm current state).
        // Số thứ tự của state nằm trong StatusType và mapStatus của Database Service
        Optional<Integer> optionalCurrentState = ds.currentState(today);
        int currentState = optionalCurrentState.orElse(0);

        // 1.2. Kiểm tra các trạng thái không hợp lệ cho bước này
        if (currentState < 2) {
            System.out.println("Need to crawl file/save to staging.cp_daily first! Program exited.");
            return;
        }
        if (currentState > 2) {
            System.out.println("Data has been cleaned already! Program exited.");
            return;
        }

        // 2. Trạng thái hiện tại đã hợp lệ, tiến hành gọi procedure làm sạch data
        boolean isCleanedData = ds.callDataCleaningProcedure();
        if (!isCleanedData) {
            System.out.println("Preprocessing data from cp_daily to data_cleaning failed! Program exited.");
            return;
        }

        // 3. Clean thành công thì ghi log chờ lưu vào dim trong DW.
        System.out.println("Preprocessing data from cp_daily to data_cleaning success.");
        FileStatus fileStatus = ds.getFileStatus(today, StatusType.PENDING_TO_CLEAN_DATA);
        String storedDir = ds.getStoredDirFromStatus(fileStatus.getFileName(), StatusType.PENDING_TO_SAVE_TEMP);
        boolean logSuccess = ds.createLogStatus(fileStatus.getFileName(), storedDir, StatusType.PENDING_TO_SAVE_DW);
        if (!logSuccess) {
            System.out.println("Create log status failed! Program exited.");
        } else {
            System.out.println("Create log status success. Status now: " + StatusType.PENDING_TO_SAVE_DW);
        }
    }
}
