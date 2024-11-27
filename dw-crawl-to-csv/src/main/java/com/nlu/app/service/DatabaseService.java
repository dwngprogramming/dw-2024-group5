package com.nlu.app.service;

import com.nlu.app.dto.Log;
import com.nlu.app.dto.DataFileConfig;
import com.nlu.app.jdbi.JdbiDatabase;

import java.time.LocalDate;

public class DatabaseService {
    public final JdbiDatabase jdbiDatabase = new JdbiDatabase();

    public boolean isCrawlToday(LocalDate date) {
        return this.jdbiDatabase.getFileCrawlToday(date) > 0;
    }

    public DataFileConfig getDataFileConfig(String code) {
        return this.jdbiDatabase.getDataFileConfig(code);
    }

    public Log logCrawlFile(Log log) {
        int recentLogId = this.jdbiDatabase.logCrawlFile(log);
        return this.jdbiDatabase.getDataFileById(recentLogId);
    }
}
