package com.nlu.app.jdbi;

import com.nlu.app.dto.DataFile;
import com.nlu.app.dto.DataFileConfig;
import com.nlu.app.dto.FileStatus;
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

    // Ghi lại log vào table data_files. Trả về id của log vừa lưu
    public int logCrawlFile(DataFile dataFile) {
        return controlJdbi.withHandle(handle ->
                handle.createUpdate("INSERT INTO control.logs (data_file_config_id, file_name, stored_dir, num_of_file_row, date_record, status) " +
                                "VALUES (:dataFileConfigId, :fileName, :storedDir, :numOfFileRow, :dateRecord, :status)")
                        .bindBean(dataFile)  // bind toàn bộ đối tượng DataFile
                        .executeAndReturnGeneratedKeys("id")  // Trả về id mới được tạo
                        .mapTo(int.class)  // ánh xạ kết quả thành kiểu int
                        .one()  // lấy giá trị duy nhất
        );
    }

    // Hàm lấy ra thông tin về log dựa trên id log (record trong table data_files)
    public DataFile getDataFileById(int dataFileId) {
        return controlJdbi.withHandle(handle ->
                handle
                        .registerRowMapper(DataFile.class, ConstructorMapper.of(DataFile.class))
                        .createQuery("SELECT * FROM control.logs WHERE id = :dataFileId")
                        .bind("dataFileId", dataFileId)
                        .mapTo(DataFile.class)
                        .findOne()
                        .orElse(null)
        );
    }

    // Hàm save 1 sản phẩm vào trong staging.cp_daily
    public int saveOneRowToCpDaily(String[] values) {
        return stagingJdbi.withHandle((handle) ->
                handle.createUpdate("INSERT INTO staging.cp_daily (product_name, image_url, size, weight, resolution, sensor, buttons, connection, battery, compatibility, utility, manufacturer, price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                        .bind(0, values[0])
                        .bind(1, values[1])
                        .bind(2, values[2])
                        .bind(3, values[3])
                        .bind(4, values[4])
                        .bind(5, values[5])
                        .bind(6, values[6])
                        .bind(7, values[7])
                        .bind(8, values[8])
                        .bind(9, values[9])
                        .bind(10, values[10])
                        .bind(11, values[11])
                        .bind(12, values[12])
                        .execute()
        );
    }

    // Hàm lấy ra stored_dir của file CSV
    public String getFileStoredDir(String fileName) {
        return controlJdbi.withHandle(handle ->
                handle.createQuery("SELECT stored_dir FROM control.logs " +
                                "WHERE file_name =:fileName AND status = :status")
                        .bind("fileName", fileName)
                        .bind("status", StatusType.PENDING_TO_LOAD_INTO_STAGING)
                        .mapTo(String.class)
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

    // Láy ra status của log từ file name
    public String getLogStatusByFileName(String fileName) {
        return controlJdbi.withHandle(handle ->
                handle.createQuery("SELECT status FROM control.logs WHERE file_name = :fileName")
                        .bind("fileName", fileName)
                        .mapTo(String.class)
                        .findOne()
                        .orElse(null)
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

    public void deleteAllRowsFromCpDaily() {
        this.stagingJdbi.withHandle((handle) ->
                handle.createUpdate("DELETE FROM staging.cp_daily")
                        .execute()
        );
    }
}
