CREATE DATABASE IF NOT EXISTS control;
USE control;

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


use control;
DROP TABLE IF EXISTS data_file_configs;

-- Create table to store configuration settings for data files
CREATE TABLE IF NOT EXISTS data_file_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description TEXT,
    file_name VARCHAR(1000),       -- source file name
    location VARCHAR(1000),          -- Location of the file (e.g., folder or system location)
    format VARCHAR(255),             -- File format (e.g., "csv", "txt")
    `separator` VARCHAR(255),        -- Field separator (e.g., ",", ";")
    `columns` TEXT,                  -- Columns in the file (e.g., "product_name,image_url,...")
    destination VARCHAR(1000),       -- Destination table (e.g., "staging.cp_daily")
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- Creation timestamp
    updated_at TIMESTAMP NULL,                      -- Update timestamp
    created_by VARCHAR(255),                        -- User who created the entry
    updated_by VARCHAR(255)                         -- User who last updated the entry
);

-- Insert configuration settings into data_file_configs for the cp_daily CSV file
INSERT INTO data_file_configs (
    description,
    file_name,
    location,
    format,
    `separator`,
    `columns`,
    destination,
    created_at,
    created_by
) VALUES (
    'Configuration for loading cp_daily CSV data',
    'cp_daily',
    'C:/ProgramData/MySQL/MySQL Server 8.2/Uploads',
    'csv',
    ';',
    'product_name, image_url, dimensions, weight, resolution, sensor, buttons, connection, battery, compatibility, utility, manufacturer, price',
    'staging.cp_daily',
    CURRENT_TIMESTAMP,
    'admin'
);
