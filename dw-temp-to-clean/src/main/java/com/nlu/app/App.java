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

    public static void main( String[] args ) {
        // Ngày hiện tại
        LocalDate today = LocalDate.now();

        // Mockup service. Bước 2 và 3 nằm ở trong đây
        DatabaseService ds = new DatabaseService();

        // Danh sách email nhận thông báo
        String[] emailNotifies = new String[]{"dwnq.coding@gmail.com"};
        String subject;
        String content;

        // 4. Kiểm tra trạng thái hiện tại của file trong hôm nay
        // Số thứ tự của state nằm trong StatusType và mapStatus của Database Service
        Optional<Integer> optionalCurrentState = ds.currentState(today);
        int currentState = optionalCurrentState.orElse(0);

        if (currentState < 2) {
            // 4.1. Thông báo chưa crawl data vào file csv & gửi email thông báo
            subject = BUNDLE.getString("ttc.subject.error");
            content = BUNDLE.getString("ttc.content.error.less-than-2");
            EmailNotifierUtil.sendEmail(emailNotifies, subject, content);
            System.out.println("Need to crawl file/save to staging.cp_daily first! Program exited.");
            processEmailNotify();
            return;
        }
        if (currentState > 2) {
            // 4.2. Thông báo đã làm sạch dữ liệu từ staging.cp_daily sang staging.data_cleaning & gửi email thông báo
            subject = BUNDLE.getString("ttc.subject.error");
            content = BUNDLE.getString("ttc.content.error.more-than-2");
            EmailNotifierUtil.sendEmail(emailNotifies, subject, content);
            System.out.println("Data has been cleaned already! Program exited.");
            processEmailNotify();
            return;
        }

        // 5. Gọi đến procedure staging.data_cleaning() trong staging
        boolean isCleanedData = ds.callDataCleaningProcedure();
        if (!isCleanedData) {
            // 7.1. Thông báo lỗi khi preprocess data
            subject = BUNDLE.getString("ttc.subject.error");
            content = BUNDLE.getString("ttc.content.error.clean-failed");
            EmailNotifierUtil.sendEmail(emailNotifies, subject, content);
            System.out.println("Preprocessing data from cp_daily to data_cleaning failed! Program exited.");
            processEmailNotify();
            return;
        }

        // 9. Thông báo làm sạch dữ liệu thành công
        System.out.println("Preprocessing data from cp_daily to data_cleaning success.");
        FileStatus fileStatus = ds.getFileStatus(today, StatusType.PENDING_TO_CLEAN_DATA);
        String storedDir = ds.getStoredDirFromStatus(fileStatus.getFileName(), StatusType.PENDING_TO_SAVE_TEMP);

        // 10. Ghi log thành PENDING_TO_SAVE_DW
        boolean logSuccess = ds.createLogStatus(fileStatus.getFileName(), storedDir, StatusType.PENDING_TO_SAVE_DW);
        if (logSuccess) {
            // 10.1. Thông báo ghi log thành công & gửi email thông báo]
            subject = BUNDLE.getString("ttc.subject.success");
            content = BUNDLE.getString("ttc.content.success.log-success");
            EmailNotifierUtil.sendEmail(emailNotifies, subject, content);
            System.out.println("Create log status success. Status now: " + StatusType.PENDING_TO_SAVE_DW);
            processEmailNotify();
        } else {
            // 10.2. Thông báo lỗi khi ghi log & gửi email thông báo
            subject = BUNDLE.getString("ttc.subject.error");
            content = BUNDLE.getString("ttc.content.error.log-failed");
            EmailNotifierUtil.sendEmail(emailNotifies, subject, content);
            System.out.println("Create log status failed! Program exited.");
            processEmailNotify();
        }
    }

    private static void processEmailNotify() {
        System.out.println("Notify email is sent successfully. Program exited.");
    }
}
