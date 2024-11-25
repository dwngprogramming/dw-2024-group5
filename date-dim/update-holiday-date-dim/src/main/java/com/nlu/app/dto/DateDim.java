package com.nlu.app.dto;

import com.nlu.app.util.DateCheckerUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor
public class DateDim {

    @ColumnName(value = "id")
    private int id;

    @ColumnName(value = "date")
    private LocalDate date;

    @ColumnName(value = "year")
    private Integer year;

    @ColumnName(value = "quarter")
    private Integer quarter;

    @ColumnName(value = "month")
    private Integer month;

    @ColumnName(value = "day")
    private Integer day;

    @ColumnName(value = "week_of_year")
    private Integer weekOfYear;

    @ColumnName(value = "day_of_week")
    private String dayOfWeek;

    @ColumnName(value = "is_weekend")
    private Boolean isWeekend;


    @ColumnName(value = "is_holiday")
    private boolean isHoliday;

    @JdbiConstructor
    public DateDim(@ColumnName(value = "id") int id,
                   @ColumnName(value = "date") LocalDate date,
                   @ColumnName(value = "year") Integer year,
                   @ColumnName(value = "quarter") Integer quarter,
                   @ColumnName(value = "month") Integer month,
                   @ColumnName(value = "day") Integer day,
                   @ColumnName(value = "week_of_year") Integer weekOfYear,
                   @ColumnName(value = "day_of_week") String dayOfWeek,
                   @ColumnName(value = "is_weekend") Boolean isWeekend,
                   @ColumnName(value = "is_holiday") boolean isHoliday) {
        this.id = id;
        this.date = date;
        this.year = year;
        this.quarter = quarter;
        this.month = month;
        this.day = day;
        this.weekOfYear = weekOfYear;
        this.dayOfWeek = dayOfWeek;
        this.isWeekend = isWeekend;
        this.isHoliday = isHoliday;
    }

    @Override
    public String toString() {
        return "Date: { " + "id: " + id + ", date: " + DateCheckerUtil.formattedFullDate(date) + ", isWeekend: " + isWeekend + ", isHoliday: " + isHoliday + " }";
    }
}