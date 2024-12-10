CREATE DATABASE if not exists staging;
USE staging;

SET GLOBAL local_infile = 1;

SHOW VARIABLES LIKE 'local_infile';

GRANT FILE ON *.* TO 'root'@'localhost';
FLUSH PRIVILEGES;

SHOW VARIABLES LIKE 'secure_file_priv';

CREATE TABLE IF NOT EXISTS cp_daily_temp (
    id INT AUTO_INCREMENT PRIMARY KEY, 
    product_name TEXT,
    image_url TEXT,
    dimensions VARCHAR(255),
    weight VARCHAR(255),
    resolution VARCHAR(255),
    sensor TEXT,
    buttons TEXT,
    connection TEXT,
    battery VARCHAR(255),
    compatibility VARCHAR(255),
    utility TEXT,
    manufacturer VARCHAR(255),
    price VARCHAR(255) NULL,
);

CREATE TABLE IF NOT EXISTS cp_daily (
    id INT AUTO_INCREMENT PRIMARY KEY, 
    product_name TEXT,
    image_url TEXT,
    dimensions VARCHAR(255),
    weight VARCHAR(255),
    resolution VARCHAR(255),
    sensor TEXT,
    buttons TEXT,
    connection TEXT,
    battery VARCHAR(255),
    compatibility VARCHAR(255),
    utility TEXT,
    manufacturer VARCHAR(255),
    price VARCHAR(255) NULL,
    load_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

USE control;

-- Clear existing tables if they exist
DROP TABLE IF EXISTS logs;
DROP TABLE IF EXISTS data_file_configs;

-- Create table to store configuration settings for data files
CREATE TABLE IF NOT EXISTS data_file_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description TEXT,
    source_path VARCHAR(1000),       -- Path to the source file
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

-- Insert an entry into data_file with the initial configuration for loading status
-- INSERT INTO data_file (
--     df_config_id,
--     name,
--     row_count,
--     status,
--     note,
--     created_at,
--     created_by
-- ) VALUES (
--     LAST_INSERT_ID(), -- Assumes the last inserted data_file_configs entry is relevant
--     'cp_daily_28.10.2024.csv',
--     0,  -- Initial row count (updated after loading)
--     'PENDING',  -- Initial status
--     'Pending load into cp_daily table',
--     CURRENT_TIMESTAMP,
--     'admin'
-- );
-- Create table to store details for each data file loaded
-- CREATE TABLE IF NOT EXISTS data_file (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     df_config_id BIGINT NOT NULL,           -- Foreign key to reference data_file_configs
--     name VARCHAR(1000),                     -- File name
--     row_count INT,                          -- Number of rows in the file
--     status VARCHAR(1000),                   -- Status of the file load (e.g., "SUCCESS", "FAILED")
--     note TEXT,                              -- Any additional notes or error messages
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Creation timestamp
--     updated_at TIMESTAMP NULL,                          -- Update timestamp
--     created_by VARCHAR(255),                              -- User who created the entry
--     updated_by VARCHAR(255),                              -- User who last updated the entry
--     FOREIGN KEY (df_config_id) REFERENCES data_file_configs(id) -- Link to data_file_configs table
-- );