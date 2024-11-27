package com.nlu.app.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
public class DataFileConfig {
    @ColumnName("id")
    private int id;

    @ColumnName("description")
    private String description;

    @ColumnName("file_name_format")
    private String fileNameFormat;

    @ColumnName("code")
    private String code;

    @ColumnName("location")
    private String location;

    @ColumnName("format")
    private String format;

    @ColumnName("separator")
    private String separator;

    @ColumnName("columns")
    private String columns;

    @ColumnName("destination")
    private String destination;

    @ColumnName("created_at")
    private Timestamp createdAt;

    @ColumnName("updated_at")
    private Timestamp updatedAt;

    @ColumnName("created_by")
    private String createdBy;

    @ColumnName("updated_by")
    private String updatedBy;

    @JdbiConstructor
    public DataFileConfig(@ColumnName("id") int id,
                          @ColumnName("description") String description,
                          @ColumnName("file_name_format") String fileNameFormat,
                          @ColumnName("code") String code,
                          @ColumnName("location") String location,
                          @ColumnName("format") String format,
                          @ColumnName("separator") String separator,
                          @ColumnName("columns") String columns,
                          @ColumnName("destination") String destination,
                          @ColumnName("created_at") Timestamp createdAt,
                          @ColumnName("updated_at") Timestamp updatedAt,
                          @ColumnName("created_by") String createdBy,
                          @ColumnName("updated_by") String updatedBy) {
        this.id = id;
        this.description = description;
        this.fileNameFormat = fileNameFormat;
        this.code = code;
        this.location = location;
        this.format = format;
        this.separator = separator;
        this.columns = columns;
        this.destination = destination;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }
}