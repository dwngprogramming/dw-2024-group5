package com.nlu.app;

import com.nlu.app.dto.FileStatus;
import com.nlu.app.service.DatabaseService;
import com.nlu.app.status.StatusType;
import com.nlu.app.util.EmailNotifierUtil;

import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class App {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("config");

    public static void main(String[] args) {
        // Ngày hiện tại
        LocalDate today = LocalDate.now();

        // Danh sách email nhận thông báo
        String[] emailNotifies = new String[]{"dwnq.coding@gmail.com"};
        String subject;
        String content;

        // Mockup service. Bước 2 và 3 nằm ở trong đây
        DatabaseService ds = new DatabaseService();

        // 4. Kiểm tra trạng thái cao nhất hiện tại trong control.logs của file csv
        Optional<Integer> optionalCurrentState = ds.currentState(today);
        int currentState = optionalCurrentState.orElse(0);

        if (currentState < 3) {
            // 4.1. Thông báo chưa crawl/chưa lưu vào staging.cp_daily & gửi email thông báo
            subject = BUNDLE.getString("ctd.subject.error");
            content = BUNDLE.getString("ctd.content.error.less-than-3");
            EmailNotifierUtil.sendEmail(emailNotifies, subject, content);
            System.out.println("Need to crawl file or Save to staging.cp_daily or Clean data first!");
            processEmailNotify();
            return;
        }
        if (currentState > 3) {
            // 4.2. Thông báo đã đưa dữ liệu vào dw trong hôm nay rồi & gửi email thông báo
            subject = BUNDLE.getString("ctd.subject.error");
            content = BUNDLE.getString("ctd.content.error.more-than-3");
            EmailNotifierUtil.sendEmail(emailNotifies, subject, content);
            System.out.println("Data has been saved to database dw already!");
            processEmailNotify();
            return;
        }

        // 5. Gọi đến procedure dw.transfer_data_to_mouses_dim() trong dw
        System.out.println("Executing transform data from data_cleaning to dim table by procedure. This may take a few minutes...");
        boolean isCleanedData = ds.callTransformDataProcedure();
        if (!isCleanedData) {
            subject = BUNDLE.getString("ctd.subject.error");
            content = BUNDLE.getString("ctd.content.error.more-than-3");
            EmailNotifierUtil.sendEmail(emailNotifies, subject, content);
            System.out.println("Transform data from data_cleaning to dim table failed! Program exited.");
            processEmailNotify();
            return;
        }

        // 9. Cập nhật trạng thái file trong bảng logs
        System.out.println("Transform data from data_cleaning to dim table success.");
        FileStatus fileStatus = ds.getFileStatus(today, StatusType.PENDING_TO_SAVE_DW);
        String storedDir = ds.getStoredDirFromStatus(fileStatus.getFileName(), StatusType.PENDING_TO_LOAD_INTO_STAGING);
        boolean logSuccess = ds.createLogStatus(fileStatus.getFileName(), storedDir, StatusType.PENDING_TO_SAVE_DATA_MART);
        if (logSuccess) {
            // 9.1. Thông báo thành công và ghi log mới PENDING_TO_SAVE_DATA_MART. Gửi email thông báo
            subject = BUNDLE.getString("ctd.subject.success");
            content = BUNDLE.getString("ctd.content.success.log-success");
            EmailNotifierUtil.sendEmail(emailNotifies, subject, content);
            System.out.println("Create log status success. Status now: " + StatusType.PENDING_TO_SAVE_DATA_MART);
            processEmailNotify();
        } else {
            // 9.2. Thông báo thất bại và không ghi log mới. Gửi email thông báo
            subject = BUNDLE.getString("ctd.subject.error");
            content = BUNDLE.getString("ctd.content.error.log-failed");
            EmailNotifierUtil.sendEmail(emailNotifies, subject, content);
            System.out.println("Create log status failed! Program exited.");
            processEmailNotify();
        }
    }

    private static void processEmailNotify() {
        System.out.println("Notify email is sent successfully. Program exited.");
    }
}
