package com.nlu.app.run;

import com.nlu.app.dto.DataFileConfig;
import com.nlu.app.dto.Log;
import com.nlu.app.service.CsvService;
import com.nlu.app.service.DatabaseService;
import com.nlu.app.service.EmailService;
import com.nlu.app.status.StatusType;
import com.nlu.app.util.DateFormatUtil;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class RunAll {

  private static final String ERROR_SUBJECT = "Lỗi khi crawl data";
  private static final String ERROR_CRAWL_DATE = "Dữ liệu đã được crawl trong ngày hôm nay";
  private static final String SUCCESS = "Dữ liệu đã được crawl thành công";

  public static void main(String[] args) {
    EmailService emailService = new EmailService();
    try {
      // 1. Setup biến môi trường
      // Lấy cấu hình từ file "config.properties" sử dụng ResourceBundle.
      // - "crawl.html.code": Mã định danh cho việc crawl HTML.
      // - "crawl.data.code": Mã định danh cho việc crawl dữ liệu.
      ResourceBundle bundle = ResourceBundle.getBundle("config");
      String htmlCode = bundle.getString("crawl.html.code"); // Mã HTML
      String dataCode = bundle.getString("crawl.data.code"); // Mã dữ liệu

      // Lấy ra ngày hiện tại
      LocalDate today = LocalDate.now();

      // Mockup các instances
      CsvService csvService = new CsvService();

      // 2. Khởi tạo kết nối đến database
      DatabaseService databaseService = new DatabaseService();

      // 3. Kiểm tra xem hôm nay đã crawl chưa
      boolean isCrawlToday = databaseService.isCrawlToday(today);
      if (isCrawlToday) {
        // Gửi mail thông báo đã crawl hôm nay
        sendEmailNotification(emailService, ERROR_CRAWL_DATE, "System crawled file today.");
        System.out.println("System crawled file today. Exited program...");
        return;
      }

      // 4. Nếu chưa crawl thì thực hiện setup tên file và địa chỉ lưu file trên phần cứng
      System.out.println("System haven't crawled file yet.");
      System.out.println("Setup HTML file path for crawling...");

      // 4.1 Lấy cấu hình html của file cần crawl  từ data_file_configs
      String[] htmlCsvInfo = setupFileCrawl(databaseService, htmlCode, today);
      String htmlCsvName = htmlCsvInfo[0];
      String htmlCsvPath = htmlCsvInfo[1];
      System.out.println("Setup completed. HTML file name: " + htmlCsvName);
      System.out.println("HTML file path: " + htmlCsvPath);
      System.out.println("Crawling HTML file right now. This will take a few minutes...");

      // 5. Bắt đầu crawl data
      boolean htmlCrawlSuccess = csvService.writeCrawlHtmlToCsvFile(htmlCsvPath);

      // 6. Kiểm tra trạng thái crawl data
      if (!htmlCrawlSuccess) {
        // Gửi mail thông báo crawl thất bại
        sendEmailNotification(emailService, ERROR_SUBJECT, "Crawl data CSV file failed.");
        System.out.println("Exited program...");
      } else {

        // 7. Tiến hành phân tách dữ liệu từ html và lưu vào file
        System.out.println("Setup data file path for crawling... ");

        // 7.1 Lấy cấu hình csv của file cần crawl  từ data_file_configs
        String[] dataCsvInfo = setupFileCrawl(databaseService, dataCode, today);
        String dataCsvName = dataCsvInfo[0];
        String dataCsvPath = dataCsvInfo[1];
        System.out.println("Setup completed. Data file name: " + dataCsvName);
        System.out.println("Data file path: " + dataCsvPath);
        System.out.println("Crawling data file right now. This will take a few minutes...");

        // 7.2 Phân tích dữ liệu product info từ html thành các dữ liệu sản phẩm
        int recordDataCount = csvService.csvHtmlToCsvData(htmlCsvPath, dataCsvPath);

        // 8. Kiểm tra số lượng sản phẩm được lưu vào file
        if (recordDataCount > 0) {
          //  9. Tiến hành ghi Log vào db control
          Log log = new Log();
          log.setDataFileConfigId(2);
          log.setFileName(dataCsvName);
          log.setStoredDir(dataCsvPath);
          log.setNumOfFileRow(recordDataCount);
          log.setStatus(StatusType.PENDING_TO_LOAD_INTO_STAGING);

          Log afterLog = databaseService.logCrawlFile(log);

          // 10. Gửi mail thông báo thành công
          sendEmailNotification(emailService, SUCCESS, "Crawl data success.");

          System.out.println("Crawl data CSV file success. Check file in + .");
          System.out.println("Log file infomation: ");
          System.out.println("File name: " + afterLog.getFileName());
          System.out.println("Stored dir: " + afterLog.getStoredDir());
          System.out.println("Number of row: " + afterLog.getNumOfFileRow());
          System.out.println("Status: " + afterLog.getStatus());
          System.out.println("Exited program...");
        } else {
          // Gửi mail thông báo crawl thất bại
          sendEmailNotification(emailService, ERROR_SUBJECT, "Crawl data CSV file failed.");
          System.out.println("Crawl data CSV file failed. Exited program...");
        }

      }
    } catch (Exception e) {
      sendEmailNotification(emailService, ERROR_SUBJECT, e.getMessage());
    }
  }

  public static void sendEmailNotification(EmailService emailService, String subject, String body) {
    emailService.sendEmail(subject, body);
  }

  public static String[] setupFileCrawl(DatabaseService databaseService, String dataCode,
      LocalDate date) {
    DataFileConfig dataFileConfig = databaseService.getDataFileConfig(dataCode);
    String fileNameFormat = dataFileConfig.getFileNameFormat();
    String locationBase = dataFileConfig.getLocation();
    String format = dataFileConfig.getFormat();
    String strDate = DateFormatUtil.formatDateByPointSeparator(date);
    String fileName = fileNameFormat.replace("dd.MM.yyyy", strDate);
    String filePath = locationBase + "\\" + fileName + "." + format;
    return new String[]{fileName, filePath};
  }
}
