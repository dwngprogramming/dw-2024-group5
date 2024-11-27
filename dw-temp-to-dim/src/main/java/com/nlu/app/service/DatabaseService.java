package com.nlu.app.service;

import com.nlu.app.dto.DataFile;
import com.nlu.app.dto.DataFileConfig;
import com.nlu.app.jdbi.JdbiDatabase;
import com.nlu.app.status.StatusType;
import com.nlu.app.util.DateFormatUtil;

import java.time.LocalDate;

public class DatabaseService {
    public final JdbiDatabase jdbiDatabase = new JdbiDatabase();

    public boolean isCrawled(String fileName) {
        return this.jdbiDatabase.getInLog(fileName, StatusType.PENDING_TO_SAVE_TEMP) > 0;
    }

    public DataFileConfig getDataFileConfig(String code) {
        return this.jdbiDatabase.getDataFileConfig(code);
    }

    public String getFileNameToday(String code) {
        DataFileConfig dataFileConfig = this.getDataFileConfig(code);
        String fileNameFormat = dataFileConfig.getFileNameFormat();
        String strDate = DateFormatUtil.formatDateByPointSeparator(LocalDate.now());
        return fileNameFormat.replace("dd.MM.yyyy", strDate);
    }

    public DataFile logCrawlFile(DataFile dataFile) {
        int recentLogId = this.jdbiDatabase.logCrawlFile(dataFile);
        return this.jdbiDatabase.getDataFileById(recentLogId);
    }

    // Nếu đang chờ clean data -> Đã lưu dữ liệu từ file vào cp_daily rồi
    public boolean isSavedToTemp(String fileNameToday) {
        return this.jdbiDatabase.getInLog(fileNameToday, StatusType.PENDING_TO_CLEAN_DATA) > 0;
    }
}
