package com.nlu.app.service;

import com.nlu.app.dto.FileStatus;
import com.nlu.app.jdbi.JdbiDatabase;
import com.nlu.app.status.StatusType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DatabaseService {
    public final JdbiDatabase jdbiDatabase = new JdbiDatabase();
    public final ResourceBundle bundle = ResourceBundle.getBundle("config");
    public final Map<Integer, String> statusMap = Map.of(
            1, StatusType.PENDING_TO_SAVE_TEMP,
            2, StatusType.PENDING_TO_CLEAN_DATA,
            3, StatusType.PENDING_TO_SAVE_DW,
            4, StatusType.PENDING_TO_SAVE_DATA_MART); // <code, dataFileConfig>

    // Điều này dể so sánh dễ hơn, không cần gọi đến entrySet() quá nhiều lần
    public final Map<String, Integer> statusReverseMap = statusMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    public List<FileStatus> getListFileStatus(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String strDate = date.format(formatter);
        String fileFormat = this.jdbiDatabase.getDataFileConfig(bundle.getString("crawl.data.code")).getFileNameFormat();
        String fileName = fileFormat.replace("dd.MM.yyyy", strDate);
        return this.jdbiDatabase.getFileStatusByFileName(fileName);
    }

    public FileStatus getFileStatus(LocalDate date, String status) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String strDate = date.format(formatter);
        String fileFormat = this.jdbiDatabase.getDataFileConfig(bundle.getString("crawl.data.code")).getFileNameFormat();
        String fileName = fileFormat.replace("dd.MM.yyyy", strDate);
        return this.jdbiDatabase.getFileStatus(fileName, status);
    }

    public Optional<Integer> currentState(LocalDate date) {
        List<FileStatus> fileStatusList = this.getListFileStatus(date);
        if (fileStatusList.isEmpty()) return Optional.empty();

        return fileStatusList.stream()
                .map(fileStatus -> statusReverseMap.getOrDefault(fileStatus.getStatus(), 0))
                .max(Integer::compare);
    }

    public boolean callDataCleaningProcedure() {
        return jdbiDatabase.callDataCleaningProcedure() > 0;
    }

    public boolean createLogStatus(String fileName, String status) {
        return jdbiDatabase.createLogStatus(fileName, status) > 0;
    }
}
