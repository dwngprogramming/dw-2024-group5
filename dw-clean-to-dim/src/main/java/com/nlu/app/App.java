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

        // 4. Kiểm tra trạng thái cao nhất hiện tại trong control.logs của file csv
        Optional<Integer> optionalCurrentState = ds.currentState(today);
        int currentState = optionalCurrentState.orElse(0);

        if (currentState < 3) {
            // 4.1. Thông báo chưa crawl/chưa lưu vào staging.cp_daily
            System.out.println("Need to crawl file or Save to staging.cp_daily or Clean data first! Program exited.");
            return;
        }
        if (currentState > 3) {
            // 4.2. Thông báo đã đưa dữ liệu vào dw trong hôm nay rồi
            System.out.println("Data has been saved to database dw already! Program exited.");
            return;
        }

        // 2. Trạng thái hiện tại đã hợp lệ, tiến hành gọi procedure chuyển đổi data sang database dw
        System.out.println("Executing transform data from data_cleaning to dim table by procedure. This may take a few minutes...");
        boolean isCleanedData = ds.callTransformDataProcedure();
        if (!isCleanedData) {
            System.out.println("Transform data from data_cleaning to dim table failed! Program exited.");
            return;
        }

        // 3. Transform thành công thì ghi log chờ lưu vào dim trong DW.
        System.out.println("Transform data from data_cleaning to dim table success.");
        FileStatus fileStatus = ds.getFileStatus(today, StatusType.PENDING_TO_SAVE_DW);
        String storedDir = ds.getStoredDirFromStatus(fileStatus.getFileName(), StatusType.PENDING_TO_LOAD_INTO_STAGING);
        boolean logSuccess = ds.createLogStatus(fileStatus.getFileName(), storedDir, StatusType.PENDING_TO_SAVE_DATA_MART);
        if (!logSuccess) {
            System.out.println("Create log status failed! Program exited.");
        } else {
            System.out.println("Create log status success. Status now: " + StatusType.PENDING_TO_SAVE_DATA_MART);
        }
    }
}
