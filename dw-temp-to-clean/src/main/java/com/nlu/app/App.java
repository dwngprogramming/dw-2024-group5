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

        // Mockup service. Bước 2 và 3 nằm ở trong đây
        DatabaseService ds = new DatabaseService();

        // 4. Kiểm tra trạng thái hiện tại của file trong hôm nay
        // Số thứ tự của state nằm trong StatusType và mapStatus của Database Service
        Optional<Integer> optionalCurrentState = ds.currentState(today);
        int currentState = optionalCurrentState.orElse(0);

        if (currentState < 2) {
            // 4.1. Thông báo chưa crawl data vào file csv
            System.out.println("Need to crawl file/save to staging.cp_daily first! Program exited.");
            return;
        }
        if (currentState > 2) {
            // 4.2. Thông báo đã làm sạch dữ liệu từ staging.cp_daily sang staging.data_cleaning
            System.out.println("Data has been cleaned already! Program exited.");
            return;
        }

        // 5. Gọi đến procedure staging.data_cleaning() trong staging
        boolean isCleanedData = ds.callDataCleaningProcedure();
        if (!isCleanedData) {
            // 7.1. Thông báo lỗi khi preprocess data
            System.out.println("Preprocessing data from cp_daily to data_cleaning failed! Program exited.");
            return;
        }

        // 9. Thông báo làm sạch dữ liệu thành công
        System.out.println("Preprocessing data from cp_daily to data_cleaning success.");
        FileStatus fileStatus = ds.getFileStatus(today, StatusType.PENDING_TO_CLEAN_DATA);
        String storedDir = ds.getStoredDirFromStatus(fileStatus.getFileName(), StatusType.PENDING_TO_SAVE_TEMP);

        // 10. Ghi log thành PENDING_TO_SAVE_DW
        boolean logSuccess = ds.createLogStatus(fileStatus.getFileName(), storedDir, StatusType.PENDING_TO_SAVE_DW);
        if (!logSuccess) {
            // 10.1. Thông báo ghi log thành công
            System.out.println("Create log status success. Status now: " + StatusType.PENDING_TO_SAVE_DW);
        } else {
            // 10.2. Thông báo lỗi khi ghi log
            System.out.println("Create log status failed! Program exited.");
        }
    }
}
