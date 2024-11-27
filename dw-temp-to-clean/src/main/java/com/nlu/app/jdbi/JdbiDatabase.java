package com.nlu.app.jdbi;

import com.nlu.app.dto.DataFileConfig;
import com.nlu.app.dto.FileStatus;
import com.nlu.app.dto.Log;
import com.nlu.app.status.StatusType;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;

import java.util.List;
import java.util.ResourceBundle;

public class JdbiDatabase {
    private final ResourceBundle bundle = ResourceBundle.getBundle("config");
    private final String username;
    private final String password;
    private Jdbi controlJdbi;
    private Jdbi stagingJdbi;

    public JdbiDatabase() {
        this.username = this.bundle.getString("database.username");
        this.password = this.bundle.getString("database.password");
        this.controlJdbi = this.getControl();
        this.stagingJdbi = this.getStaging();
    }

    // Láy ra status của log từ file name và status
    public FileStatus getFileStatus(String fileName, String status) {
        return controlJdbi.withHandle(handle ->
                handle.registerRowMapper(FileStatus.class, ConstructorMapper.of(FileStatus.class))
                        .createQuery("SELECT * FROM control.logs WHERE file_name = :fileName AND status = :status")
                        .bind("fileName", fileName)
                        .bind("status", status)
                        .mapTo(FileStatus.class)
                        .findOne()
                        .orElse(null)
        );
    }

    // Hàm lấy ra thông tin status của file trong control.data_files. Bao gồm fileName & status
    public List<FileStatus> getFileStatusByFileName(String fileName) {
        return controlJdbi.withHandle(handle ->
                handle
                        .registerRowMapper(ConstructorMapper.factory(FileStatus.class))
                        .createQuery("SELECT file_name, status FROM control.logs WHERE file_name = :fileName")
                        .bind("fileName", fileName)
                        .mapTo(FileStatus.class)
                        .list()
        );
    }

    // Hàm lấy ra thông tin về file config trong bảng data_file_configs
    public DataFileConfig getDataFileConfig(String code) {
        return controlJdbi.withHandle(handle ->
                handle
                        .registerRowMapper(ConstructorMapper.factory(DataFileConfig.class))
                        .createQuery("SELECT * FROM control.data_file_configs WHERE code = :code")
                        .bind("code", code)
                        .mapTo(DataFileConfig.class)
                        .findOne()
                        .orElse(null)
        );
    }

    // Hàm lấy ra thông tin về log dựa trên id log (record trong table data_files)
    public Log getDataFileById(int dataFileId) {
        return controlJdbi.withHandle(handle ->
                handle
                        .registerRowMapper(Log.class, ConstructorMapper.of(Log.class))
                        .createQuery("SELECT * FROM control.logs WHERE id = :dataFileId")
                        .bind("dataFileId", dataFileId)
                        .mapTo(Log.class)
                        .findOne()
                        .orElse(null)
        );
    }

    public int createLogStatus(String fileName, String tempSaveSuccess) {
        return controlJdbi.withHandle(handle ->
                handle.createUpdate("INSERT INTO control.logs (data_file_config_id, file_name, status) VALUES (:dfci, :fileName, :status)")
                        .bind("dfci", 2)
                        .bind("fileName", fileName)
                        .bind("status", tempSaveSuccess)
                        .execute()
        );
    }

    // Hàm lấy kết nối JDBI tới CSDL Control
    private Jdbi getControl() {
        if (this.controlJdbi == null) {
            String url = this.bundle.getString("database.control");
            HikariDataSource dataSource = this.setupHikariDataSource(url);
            this.controlJdbi = Jdbi.create(dataSource);
        }

        return this.controlJdbi;
    }

    // Hàm lấy kết nối JDBI tới CSDL Stagingz
    private Jdbi getStaging() {
        if (this.stagingJdbi == null) {
            String url = this.bundle.getString("database.staging");
            HikariDataSource dataSource = this.setupHikariDataSource(url);
            this.stagingJdbi = Jdbi.create(dataSource);
        }

        return this.stagingJdbi;
    }

    // Hàm tạo kết nối JDBI tới CSDL
    private HikariDataSource setupHikariDataSource(String databaseUrl) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseUrl);
        config.setUsername(this.username);
        config.setPassword(this.password);
        config.setMaximumPoolSize(10);
        return new HikariDataSource(config);
    }

    public int callDataCleaningProcedure() {
        try {
            return this.controlJdbi.withHandle((handle) ->
                    handle.createUpdate("CALL data_cleaning()")
                            .execute()
            );
        } catch (Exception e) {
            // Xử lý lỗi
            e.printStackTrace();
            return -1; // Hoặc một mã lỗi tùy ý
        }
    }
}
