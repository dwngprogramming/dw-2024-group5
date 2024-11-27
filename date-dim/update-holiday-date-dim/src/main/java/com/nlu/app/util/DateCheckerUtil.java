package com.nlu.app.util;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class DateCheckerUtil {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter dateEachYearFormatter = DateTimeFormatter.ofPattern("dd-MM");
    private static final LocalDate startDate = LocalDate.parse("01-01-2024", dateFormatter);
    private static final LocalDate endDate = LocalDate.parse("31-12-2025", dateFormatter);

    // Phương thức kiểm tra nhập liệu của người dùng
    public static boolean isValidUpdateHoliday(String input) {
        if (input.matches("^\\d{2}-\\d{2}-\\d{4} (true|false)$")) {
            // Lấy ra danh sách chuỗi
            String[] parts = input.split(" ");
            LocalDate date = parseDate(parts[0]);

            if (date == null) {
                System.out.println("Date is not exist.");
                return false;
            } else if (date.isBefore(startDate) || date.isAfter(endDate)) {
                System.out.println("Invalid date. Date must be between 01-01-2024 and 31-12-2025.");
                return false;
            } else {
                return true;
            }
        } else if (input.matches("^\\d{2}-\\d{2}-\\d{4} \\d{2}-\\d{2}-\\d{4} (true|false)$")) {
            // Kiểm tra phạm vi ngày
            String[] dates = input.split(" ");
            LocalDate start = parseDate(dates[0]);
            LocalDate end = parseDate(dates[1]);

            if (start == null || end == null || start.isBefore(startDate) || end.isAfter(endDate) || start.isAfter(end)) {
                System.out.println("Invalid date range. Dates must be between 01-01-2024 and 31-12-2025, and start date must be before end date.");
                return false;
            } else {
                return true;
            }
        } else if (input.matches("^\\d{2}-\\d{2} (true|false)$")) {
            // Kiểm tra ngày lễ mỗi năm
            MonthDay date = parseMonth(input);
            if (date != null) {
                System.out.println("Invalid date format. Format must be dd-MM.");
                return false;
            } else {
                return true;
            }
        } else {
            System.out.println("Invalid input format. 3 valid format (NOT CONTAIN ''): " +
                    "\n'dd-MM-yyy flag'. Ex: '01-01-2024 true' is valid." +
                    "\n'dd-MM-yyyy dd-MM-yyyy flag'. Ex: '01-01-2024 05-01-2024 true' is valid. " +
                    "\n'dd-MM flag'. Ex: '01-01 true' is valid");
            return false;
        }
    }

    public static boolean isValidCheckHoliday(String input) {
        if (input.matches("^\\d{2}-\\d{2}-\\d{4}$")) {
            // Lấy ra danh sách chuỗi
            LocalDate date = parseDate(input);

            if (date == null) {
                System.out.println("Date is not exist.");
                return false;
            } else if (date.isBefore(startDate) || date.isAfter(endDate)) {
                System.out.println("Invalid date. Date must be between 01-01-2024 and 31-12-2025.");
                return false;
            } else {
                return true;
            }
        } else if (input.matches("^\\d{2}-\\d{2}-\\d{4} \\d{2}-\\d{2}-\\d{4}$")) {
            // Kiểm tra phạm vi ngày
            String[] dates = input.split(" ");
            LocalDate start = parseDate(dates[0]);
            LocalDate end = parseDate(dates[1]);

            if (start == null || end == null || start.isBefore(startDate) || end.isAfter(endDate) || start.isAfter(end)) {
                System.out.println("Invalid date range. Dates must be between 01-01-2024 and 31-12-2025, and start date must be before end date.");
                return false;
            } else {
                return true;
            }
        } else if (input.matches("^\\d{2}-\\d{2}$")) {
            // Kiểm tra ngày lễ mỗi năm
            MonthDay date = parseMonth(input);
            if (date == null) {
                System.out.println("Invalid date format. Format must be dd-MM.");
                return false;
            } else {
                return true;
            }
        } else {
            System.out.println("Invalid input format. 3 valid format (NOT CONTAIN ''): " +
                    "\n'dd-MM-yyyy'. Ex: '01-01-2024' is valid." +
                    "\n'dd-MM-yyyy dd-MM-yyyy'. Ex: '01-01-2024 05-01-2024' is valid. " +
                    "\n'dd-MM'. Ex: '01-01' is valid");
            return false;
        }
    }

    // Phương thức kiểm tra và phân tích ngày đầy đủ
    public static LocalDate parseDate(String dateStr) {
        try {
            // Kiểm tra ngày hợp lệ
            LocalDate date = LocalDate.parse(dateStr, dateFormatter);

            // Nếu có lỗi về ngày, sẽ ném Exception.
            if (date.getDayOfMonth() != Integer.parseInt(dateStr.substring(0, 2)) || date.getMonthValue() != Integer.parseInt(dateStr.substring(3, 5))) {
                throw new DateTimeParseException("Invalid date", dateStr, 0);
            }
            return date; // Trả về ngày hợp lệ
        } catch (DateTimeParseException e) {
            return null; // Nếu có lỗi (ví dụ: ngày không tồn tại hoặc không hợp lệ)
        }
    }

    // Phương thức kiểm tra và phân tích ngày tháng
    public static MonthDay parseMonth(String monthStr) {
        try {
            // MonthDay trả về --MM-dd
            MonthDay date = MonthDay.parse(monthStr, dateEachYearFormatter);
            String converted = date.format(dateEachYearFormatter);

            // Nếu có lỗi về ngày, sẽ ném Exception.
            if (!converted.equals(monthStr)) {
                throw new DateTimeParseException("Invalid date", monthStr, 0);
            }
            return date;
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static List<LocalDate> getHolidaysWithFlag(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> holidays = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            holidays.add(date);
        }
        return holidays;
    }

    public static String formattedFullDate(LocalDate date) {
        return date.format(dateFormatter);
    }

    public static String formattedAnnualDate(MonthDay date) {
        return date.format(dateEachYearFormatter);
    }
}
