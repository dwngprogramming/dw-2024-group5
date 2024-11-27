package com.nlu.app.run;

import com.nlu.app.service.CsvService;
import com.nlu.app.service.DatabaseService;

import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Hello world!
 *
 */
public class RunTempToDim
{
    public static void main( String[] args ) {
        // Setup biến môi trường
        ResourceBundle bundle = ResourceBundle.getBundle("config");
        String dataCode = bundle.getString("crawl.data.code");

        // Lấy ra ngày hôm nay
        LocalDate today = LocalDate.now();

        // Mockup các instances
        CsvService csvService = new CsvService();
        DatabaseService databaseService = new DatabaseService();

        // 1. Kiểm tra xem file đã crawl chưa / Đã lưu dữ liệu từ file vào cp_daily chưa
        // 1.1. Lấy ra file name của hôm nay
        String fileNameToday = databaseService.getFileNameToday(dataCode);
        // 1.2. Kiểm tra file đã crawl thành công chưa, nếu chưa thì thoát khỏi app
        boolean isCrawlToday = databaseService.isCrawled(fileNameToday);
        if (!isCrawlToday) {
            System.out.println("You need to crawl " + fileNameToday + " first. Exiting...");
            return;
        }
        // 1.3. Kiểm tra file đã được đưa vào temp chưa, nếu chưa thì thoát khỏi app
        boolean isSavedToTemp = databaseService.isSavedToTemp(fileNameToday);
        if (!isSavedToTemp) {
            System.out.println("You need to save data from " + fileNameToday + " to table staging.cp_daily first. Exiting...");
            return;
        }

        // 2. Sau khi kiểm tra trạng thái của các file, thực hiện chuyển đổi data từ staging.cp_daily -> staging.dim_mouses

    }
}
