package com.nlu.app.service;

import com.nlu.app.database.JdbiDatabase;
import com.nlu.app.dto.DateDim;
import com.nlu.app.util.DateCheckerUtil;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DateHolidayService {
    private final JdbiDatabase jdbiDatabase = new JdbiDatabase();

    public void updateRangeHolidays(String request) {
        String[] datesInput = request.split(" ");
        LocalDate startDate = DateCheckerUtil.parseDate(datesInput[0]);
        LocalDate endDate = DateCheckerUtil.parseDate(datesInput[1]);
        Boolean flag = Boolean.parseBoolean(datesInput[2]);

        // Lấy ra danh sách Holiday, với khoảng thời gian từ startDate -> endDate, và is_holiday = flag
        List<LocalDate> holidays = DateCheckerUtil.getHolidaysWithFlag(startDate, endDate);
        for (LocalDate holiday : holidays) {
            int affectedRow = jdbiDatabase.updateHolidayDateDim(holiday, flag);
            if (affectedRow > 0) {
                System.out.println("Date " + holiday + " is updated holiday successfully. Status for holiday " + holiday + " is: " + flag + "\n");
            }
        }
    }

    public void updateFullDateHoliday(String request) {
        String[] datesInput = request.split(" ");
        LocalDate date = DateCheckerUtil.parseDate(datesInput[0]);
        Boolean flag = Boolean.parseBoolean(datesInput[1]);

        if (date != null) {
            int affectedRow = jdbiDatabase.updateHolidayDateDim(date, flag);
            if (affectedRow > 0) {
                System.out.println("Date " + DateCheckerUtil.formattedFullDate(date) + " is updated holiday successfully. Status for holiday " + DateCheckerUtil.formattedFullDate(date) + " is: " + flag);
            }
        } else {
            System.out.println("Date is not exist. \n");
        }
    }

    public void updateAnnualHolidays(String request) {
        String[] datesInput = request.split(" ");
        MonthDay date = DateCheckerUtil.parseMonth(datesInput[0]);
        Boolean flag = Boolean.parseBoolean(datesInput[1]);

        if (date != null) {
            int affectedRow = jdbiDatabase.updateHolidayAnnualDim(date, flag);
            if (affectedRow > 0) {
                System.out.println("Annual date " + DateCheckerUtil.formattedAnnualDate(date) + " is updated successfully. Status for holiday " + DateCheckerUtil.formattedAnnualDate(date) + " is: " + flag + "\n");
            }
        }
    }

    public void getFullDateHoliday(String request) {
        LocalDate date = DateCheckerUtil.parseDate(request);

        if (date != null) {
            DateDim holiday = jdbiDatabase.getHolidayByDate(date);
            if (holiday != null) {
                System.out.println("Date information: " + holiday + "\n");
            } else {
                System.out.println("Invalid date input.\n");
            }
        }
    }

    public void getAnnualHoliday(String checkInput) {
        MonthDay date = DateCheckerUtil.parseMonth(checkInput);

        if (date != null) {
            List<DateDim> dateDims = jdbiDatabase.getHolidaysByAnnualDate(date);
            if (!dateDims.isEmpty()) {
                StringBuilder sb = new StringBuilder("Annual date " + DateCheckerUtil.formattedAnnualDate(date) + " has " + dateDims.size() + " dates in range: \n");
                for (DateDim holiday : dateDims) {
                    sb.append(holiday).append("\n");
                }
                System.out.println(sb);
            } else {
                System.out.println("Invalid date input.\n");
            }
        }
    }

    public void getRangeHolidays(String checkInput) {
        String[] datesInput = checkInput.split(" ");
        LocalDate startDate = DateCheckerUtil.parseDate(datesInput[0]);
        LocalDate endDate = DateCheckerUtil.parseDate(datesInput[1]);

        if (startDate != null && endDate != null) {
            List<DateDim> dateDims = jdbiDatabase.getHolidaysByRange(startDate, endDate);
            if (!dateDims.isEmpty()) {
                StringBuilder sb = new StringBuilder("Holidays in range " + DateCheckerUtil.formattedFullDate(startDate) + " to " + DateCheckerUtil.formattedFullDate(endDate) + " are: \n");
                for (DateDim dateDim : dateDims) {
                    sb.append(dateDim).append("\n");
                }
                System.out.println(sb);
            } else {
                System.out.println("Invalid date input.\n");
            }
        }
    }

    public void getAllHolidays() {
        List<Integer> years = jdbiDatabase.getAllYears();
        Map<Integer, List<DateDim>> holidays = new HashMap<>();

        for (Integer year : years) {
            holidays.put(year, jdbiDatabase.getHolidayByYear(year));
        }

        StringBuilder sb = new StringBuilder("All holidays (split by year): ");
        for (Map.Entry<Integer, List<DateDim>> entry : holidays.entrySet()) {
            sb.append("\nYear ").append(entry.getKey()).append(": \n");
            for (DateDim dateDim : entry.getValue()) {
                sb.append(dateDim).append("\n");
            }
        }
        System.out.println(sb);
    }
}
