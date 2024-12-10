package com.nlu.app.service;

import com.nlu.app.dto.DataFile;
import com.nlu.app.dto.DataFileConfig;
import com.nlu.app.dto.FileStatus;
import com.nlu.app.jdbi.JdbiDatabase;
import com.nlu.app.status.StatusType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DatabaseService {
    public final JdbiDatabase jdbiDatabase = new JdbiDatabase();
    public final ResourceBundle bundle = ResourceBundle.getBundle("config");
    public final Map<Integer, String> statusMap = Map.of(
            1, StatusType.PENDING_TO_LOAD_INTO_STAGING,
            2, StatusType.SUCCESS_LOAD_INTO_STAGING,
            3, StatusType.PENDING_TO_SAVE_DW,
            4, StatusType.PENDING_TO_SAVE_DATA_MART); // <code, dataFileConfig>

    // Điều này dể so sánh dễ hơn, không cần gọi đến entrySet() quá nhiều lần
    public final Map<String, Integer> statusReverseMap = statusMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    public List<FileStatus> getFileStatus(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String strDate = date.format(formatter);
        String fileFormat = this.jdbiDatabase.getDataFileConfig(bundle.getString("crawl.data.code")).getFileNameFormat();
        String fileName = fileFormat.replace("dd.MM.yyyy", strDate);
        return this.jdbiDatabase.getFileStatusByFileName(fileName);
    }

    public DataFileConfig getDataFileConfig(String code) {
        return this.jdbiDatabase.getDataFileConfig(code);
    }

    public DataFile logCrawlFile(DataFile dataFile) {
        int recentLogId = this.jdbiDatabase.logCrawlFile(dataFile);
        return this.jdbiDatabase.getDataFileById(recentLogId);
    }

    public boolean saveOneRowToCpDaily(String[] values) {
        return jdbiDatabase.saveOneRowToCpDaily(values) > 0;
    }

    public String getFileStoredDir(String fileName) {
        return jdbiDatabase.getFileStoredDir(fileName);
    }

    public boolean createLogStatus(String fileName, String tempSaveSuccess) {
        return jdbiDatabase.createLogStatus(fileName, tempSaveSuccess) > 0;
    }

    public boolean isSavedToTemp(String fileName) {
        return !jdbiDatabase.getLogStatusByFileName(fileName).equals(StatusType.PENDING_TO_LOAD_INTO_STAGING);
    }

    public void deleteAllRowsFromCpDaily() {
        jdbiDatabase.deleteAllRowsFromCpDaily();
    }

    public FileStatus currentState(List<FileStatus> fileStatusList) {
        if (fileStatusList.isEmpty()) return null;

        // Tìm trạng thái có key cao nhất từ statusMap
        return fileStatusList.stream()
                .max(Comparator.comparingInt(fileStatus ->
                        statusReverseMap.getOrDefault(fileStatus.getStatus(), 0) // Lấy key từ statusReverseMap và sử dụng functional của Comparator lấy ra max
                ))
                .orElse(null);
    }

}
