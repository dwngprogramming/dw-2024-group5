package com.nlu.app;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/dw";
        String username = "root";
        String password = "230503";

        Jdbi jdbi = Jdbi.create(url, username, password);

        jdbi.useHandle(handle -> {
            // Chèn dữ liệu ngày vào bảng
            LocalDate startDate = LocalDate.of(2024, 1, 1); // Ngày bắt đầu
            LocalDate endDate = LocalDate.of(2025, 12, 31); // Ngày kết thúc

            if (isDateInRangeExist(handle, startDate, endDate)) {
                System.out.println("Khoảng thời gian này đã được thêm vào date_dim!");
                return;
            }

            System.out.println("Đang thêm dữ liệu vào bảng 'date_dim'...");

            WeekFields weekFields = WeekFields.of(Locale.getDefault()); // Tuần dựa theo locale hiện tại

            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                int year = date.getYear();
                int month = date.getMonthValue();
                int day = date.getDayOfMonth();
                int quarter = (month - 1) / 3 + 1; // Tính quý
                int weekOfYear = date.get(weekFields.weekOfYear()); // Tuần hiện tại trong năm
                String dayOfWeekEn = date.getDayOfWeek().toString(); // Tên ngày trong tuần (Monday, Tuesday...)
                boolean isWeekend = dayOfWeekEn.equals("SATURDAY") || dayOfWeekEn.equals("SUNDAY");

                String dayOfWeekVi;
                switch (dayOfWeekEn) {
                    case "MONDAY":
                        dayOfWeekVi = "Thứ Hai";
                        break;
                    case "TUESDAY":
                        dayOfWeekVi = "Thứ Ba";
                        break;
                    case "WEDNESDAY":
                        dayOfWeekVi = "Thứ Tư";
                        break;
                    case "THURSDAY":
                        dayOfWeekVi = "Thứ Năm";
                        break;
                    case "FRIDAY":
                        dayOfWeekVi = "Thứ Sáu";
                        break;
                    case "SATURDAY":
                        dayOfWeekVi = "Thứ Bảy";
                        break;
                    case "SUNDAY":
                        dayOfWeekVi = "Chủ Nhật";
                        break;
                    default:
                        throw new IllegalStateException("Ngày không hợp lệ: " + dayOfWeekEn);
                }

                // Chèn dữ liệu
                handle.createUpdate("INSERT INTO date_dim (date, year, quarter, month, day, week_of_year, day_of_week, is_weekend) " +
                                         "VALUES (:date, :year, :quarter, :month, :day, :week_of_year, :day_of_week, :is_weekend)")
                        .bind("date", date)
                        .bind("year", year)
                        .bind("quarter", quarter)
                        .bind("month", month)
                        .bind("day", day)
                        .bind("week_of_year", weekOfYear) // Tuần hiện tại trong năm
                        .bind("day_of_week", dayOfWeekVi) // Sử dụng ngày tiếng Việt
                        .bind("is_weekend", isWeekend)
                        .execute();
            }

            System.out.println("Dữ liệu ngày tháng đã được thêm thành công vào bảng 'date_dim'!");
        });
    }

    private static boolean isDateInRangeExist(Handle handle, LocalDate startDate, LocalDate endDate) {
        // Truy vấn để kiểm tra xem có ngày nào trong khoảng đã tồn tại
        String sql = "SELECT COUNT(*) FROM date_dim WHERE date BETWEEN :startDate AND :endDate";
        long count = handle.createQuery(sql)
                .bind("startDate", startDate)
                .bind("endDate", endDate)
                .mapTo(Long.class)
                .first();
        return count > 0; // Nếu có ít nhất một ngày trong khoảng, trả về true
    }
}
