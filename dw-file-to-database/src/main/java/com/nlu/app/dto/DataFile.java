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
public class DataFile {
    @ColumnName("id")
    private int id;

    @ColumnName("data_file_config_id")
    private int dataFileConfigId;

    @ColumnName("file_name")
    private String fileName;

    @ColumnName("stored_dir")
    private String storedDir;

    @ColumnName("num_of_file_row")
    private Integer numOfFileRow;

    @ColumnName("date_record")
    private Timestamp dateRecord;

    @ColumnName("status")
    private String status;

    @JdbiConstructor
    public DataFile(@ColumnName("id") int id,
                    @ColumnName("data_file_config_id") int dataFileConfigId,
                    @ColumnName("file_name") String fileName,
                    @ColumnName("stored_dir") String storedDir,
                    @ColumnName("num_of_file_row") Integer numOfFileRow,
                    @ColumnName("date_record") Timestamp dateRecord,
                    @ColumnName("status") String status) {
        this.id = id;
        this.dataFileConfigId = dataFileConfigId;
        this.fileName = fileName;
        this.storedDir = storedDir;
        this.numOfFileRow = numOfFileRow;
        this.dateRecord = dateRecord;
        this.status = status;
    }
}