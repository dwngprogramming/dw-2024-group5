package com.nlu.app.run;

import com.nlu.app.dto.DataFile;
import com.nlu.app.dto.DataFileConfig;
import com.nlu.app.service.CsvService;
import com.nlu.app.service.DatabaseService;
import com.nlu.app.status.StatusType;
import com.nlu.app.util.DateFormatUtil;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class RunAll {
    public static void main(String[] args) throws CsvValidationException, IOException {
        // Setup biến môi trường
        ResourceBundle bundle = ResourceBundle.getBundle("config");
        String htmlCode = bundle.getString("crawl.html.code");
        String dataCode = bundle.getString("crawl.data.code");

        // Lấy ra ngày hiện tại
        LocalDate today = LocalDate.now();

        // Mockup các instances
        CsvService csvService = new CsvService();
        DatabaseService databaseService = new DatabaseService();

        // 1. Kiểm tra xem hôm nay file đã được crawl chưa
        boolean isCrawlToday = databaseService.isCrawlToday(today);
        if (isCrawlToday) {
            System.out.println("System crawled file today. Exited program...");
            return;
        }

        // 2. Nếu chưa crawl thì thực hiện setup tên file và địa chỉ lưu file trên phần cứng
        System.out.println("System haven't crawled file yet.");
        System.out.println("Setup HTML file path for crawling...");
        String[] htmlCsvInfo = setupFileCrawl(databaseService, htmlCode, today);
        String htmlCsvName = htmlCsvInfo[0];
        String htmlCsvPath = htmlCsvInfo[1];
        System.out.println("Setup completed. HTML file name: " + htmlCsvName);
        System.out.println("HTML file path: " + htmlCsvPath);
        System.out.println("Crawling HTML file right now. This will take a few minutes...");

        // 3. Thực hiện kiểm tra 
        boolean htmlCrawlSuccess = csvService.writeCrawlHtmlToCsvFile(htmlCsvPath);
        if (!htmlCrawlSuccess) {
            System.out.println("Crawl HTML CSV file failed.");
            System.out.println("Exited program...");
        } else {
            System.out.println("Setup data file path for crawling... ");
            String[] dataCsvInfo = setupFileCrawl(databaseService, dataCode, today);
            String dataCsvName = dataCsvInfo[0];
            String dataCsvPath = dataCsvInfo[1];
            System.out.println("Setup completed. Data file name: " + dataCsvName);
            System.out.println("Data file path: " + dataCsvPath);
            System.out.println("Crawling data file right now. This will take a few minutes...");
            int recordDataCount = csvService.csvHtmlToCsvData(htmlCsvPath, dataCsvPath);
            if (recordDataCount > 0) {
                DataFile dataFile = new DataFile();
                dataFile.setDataFileConfigId(2);
                dataFile.setFileName(dataCsvName);
                dataFile.setStoredDir(dataCsvPath);
                dataFile.setNumOfFileRow(recordDataCount);
                dataFile.setStatus(StatusType.CRAWLED_SUCCESS);

                DataFile logDataFile = databaseService.logCrawlFile(dataFile);
                System.out.println("Crawl data CSV file success. Check file in + .");
                System.out.println("Log file infomation: ");
                System.out.println("File name: " + logDataFile.getFileName());
                System.out.println("Stored dir: " + logDataFile.getStoredDir());
                System.out.println("Number of row: " + logDataFile.getNumOfFileRow());
                System.out.println("Status: " + logDataFile.getStatus());
                System.out.println("Exited program...");
            } else {
                System.out.println("Crawl data CSV file failed. Exited program...");
            }


        }
    }

    public static String[] setupFileCrawl(DatabaseService databaseService, String dataCode, LocalDate date) {
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