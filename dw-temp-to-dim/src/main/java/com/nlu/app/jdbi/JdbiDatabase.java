package com.nlu.app.jdbi;

import com.nlu.app.dto.DataFile;
import com.nlu.app.dto.DataFileConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;

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

    // Hàm lấy ra id của file log dựa trên tên file & trạng thái
    public int getInLog(String fileName, String status) {
        return controlJdbi.withHandle(handle ->
                handle.createQuery("SELECT id FROM control.data_files WHERE file_name = :fileName AND status = :status")
                        .bind("fileName", fileName)
                        .bind("status", status)
                        .mapTo(Integer.class)
                        .findOne()
                        .orElse(-1) // Trả về kết quả của orElse
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
                handle.createUpdate("INSERT INTO control.data_files (data_file_config_id, file_name, stored_dir, num_of_file_row, date_record, status) " +
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
                        .createQuery("SELECT * FROM control.data_files WHERE id = :dataFileId")
                        .bind("dataFileId", dataFileId)
                        .mapTo(DataFile.class)
                        .findOne()
                        .orElse(null)
        );
    }

    public void saveOneRowToCpDaily(String[] values) {
        stagingJdbi.useHandle((handle) -> {
            handle.execute("INSERT INTO staging.cp_daily (product_name, image_url, size, weight, resolution, sensor, buttons, connection, battery, compatibility, utility, manufacturer, price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7], values[8], values[9], values[10], values[11], values[12]});
        });
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

    // Hàm lấy kết nối JDBI tới CSDL Staging
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
}
