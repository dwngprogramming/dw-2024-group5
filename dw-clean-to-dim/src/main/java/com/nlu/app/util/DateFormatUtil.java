package com.nlu.app.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateFormatUtil {
    public static String formatTodayByPointSeparator() {
        LocalDate today = LocalDate.now();
        return formatDateByPointSeparator(today);
    }

    public static String formatDateByPointSeparator(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return date.format(formatter);
    }
}
