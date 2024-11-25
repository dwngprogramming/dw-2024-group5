package com.nlu.app.database;

import com.nlu.app.dto.DateDim;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.ResourceBundle;

@Getter @Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JdbiDatabase {
    ResourceBundle bundle = ResourceBundle.getBundle("config");
    Jdbi jdbi = Jdbi.create(bundle.getString("db.url"), bundle.getString("db.username"), bundle.getString("db.password"));

    public int updateHolidayDateDim(LocalDate date, Boolean flag) {
        return jdbi.withHandle(handle -> {
            String sql = "UPDATE date_dim SET is_holiday = :flag WHERE date = :date";
            return handle.createUpdate(sql)
                    .bind("flag", flag)
                    .bind("date", date)
                    .execute();
        });
    }

    public int updateHolidayAnnualDim(MonthDay annualDate, Boolean flag) {
        return jdbi.withHandle(handle -> {
            String sql = "UPDATE date_dim SET is_holiday = :flag WHERE (day = :day AND month = :month)";
            return handle.createUpdate(sql)
                    .bind("day", annualDate.getDayOfMonth())
                    .bind("month", annualDate.getMonthValue())
                    .bind("flag", flag)
                    .execute();
        });
    }

    public DateDim getHolidayByDate(LocalDate date) {
        return jdbi.withHandle(handle -> {
            String sql = "SELECT * FROM date_dim WHERE date = :date";

            return handle.registerRowMapper(DateDim.class, ConstructorMapper.of(DateDim.class))
                    .createQuery(sql)
                    .bind("date", date)
                    .mapTo(DateDim.class)
                    .one();
        });
    }

    public List<DateDim> getHolidaysByRange(LocalDate startDate, LocalDate endDate) {
        return jdbi.withHandle(handle -> {
            String sql = "SELECT * FROM date_dim WHERE date BETWEEN :startDate AND :endDate";

            return handle.registerRowMapper(DateDim.class, ConstructorMapper.of(DateDim.class))
                    .createQuery(sql)
                    .bind("startDate", startDate)
                    .bind("endDate", endDate)
                    .mapTo(DateDim.class)
                    .list();
        });
    }

    public List<DateDim> getHolidaysByAnnualDate(MonthDay date) {
        return jdbi.withHandle(handle -> {
            String sql = "SELECT * FROM date_dim WHERE day = :day AND month = :month";

            return handle.registerRowMapper(DateDim.class, ConstructorMapper.of(DateDim.class))
                    .createQuery(sql)
                    .bind("day", date.getDayOfMonth())
                    .bind("month", date.getMonthValue())
                    .mapTo(DateDim.class)
                    .list();
        });
    }

    public List<Integer> getAllYears() {
        return jdbi.withHandle(handle -> {
            String sql = "SELECT DISTINCT year FROM date_dim ORDER BY year";
            return handle.createQuery(sql)
                    .mapTo(Integer.class)
                    .list();
        });
    }

    public List<DateDim> getHolidayByYear(Integer year) {
        return jdbi.withHandle(handle -> {
            String sql = "SELECT * FROM date_dim WHERE year = :year AND is_holiday = true";

            return handle.registerRowMapper(DateDim.class, ConstructorMapper.of(DateDim.class))
                    .createQuery(sql)
                    .bind("year", year)
                    .mapTo(DateDim.class)
                    .list();
        });
    }
}
