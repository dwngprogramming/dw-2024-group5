package com.nlu.app.util;

import java.time.LocalDate;
import java.util.ResourceBundle;

public class CsvUtil {
    public static final ResourceBundle bundle = ResourceBundle.getBundle("config");

    public static String getCsvPathToday(String fileNameFormat, String fileType, String location) {
        String strDate = DateFormatUtil.formatTodayByPointSeparator();
        return fileNameFormat + strDate + fileType;
    }

    public static String getCsvPath(String fileNameFormat, String fileType, String location, LocalDate date) {
        return "";
    }
}