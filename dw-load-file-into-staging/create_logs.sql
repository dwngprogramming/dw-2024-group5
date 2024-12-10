use control;

CREATE TABLE logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    df_config_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    row_count INT,
    status VARCHAR(50) NOT NULL,
    note VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL
);
-- Dòng dữ liệu 1: Ghi nhận hoàn thành việc thu thập dữ liệu
INSERT INTO logs (df_config_id, name, row_count, status, note, created_by)
VALUES (1, 'cp_daily_13.11.2024.csv', 168, 'SUCCESS_LOAD_INTO_STAGING', 'Data crawling completed successfully.', 'admin');

-- Dòng dữ liệu 2: Ghi nhận file sẵn sàng để nạp vào staging
INSERT INTO logs (df_config_id, name, status, note, created_by)
VALUES (1, 'cp_daily_13.11.2024.csv', 'PENDING_TO_LOAD_INTO_STAGING', 'File ready for loading into staging.', 'admin');
