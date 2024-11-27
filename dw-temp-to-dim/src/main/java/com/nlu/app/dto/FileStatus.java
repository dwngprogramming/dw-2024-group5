package com.nlu.app.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

@Getter
@Setter
@NoArgsConstructor
public class FileStatus {
    private String fileName;
    private String status;

    @JdbiConstructor
    public FileStatus(@ColumnName("file_name") String fileName,
                      @ColumnName("status") String status) {
        this.fileName = fileName;
        this.status = status;
    }
}
