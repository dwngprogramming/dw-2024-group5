CREATE DATABASE IF NOT EXISTS control;
USE control;

CREATE TABLE IF NOT EXISTS `logs` (
	id INT PRIMARY KEY AUTO_INCREMENT,
	data_file_config_id INT NOT NULL,
	file_name VARCHAR(200) NOT NULL,
	stored_dir VARCHAR(255),
	num_of_file_row INT,
	date_record TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	`status` VARCHAR(50)
);


CREATE TABLE IF NOT EXISTS data_file_configs (
	id INT PRIMARY KEY AUTO_INCREMENT,
	description TEXT,
	file_name_format VARCHAR(1000),  			 -- source file name
	`code` VARCHAR(20) UNIQUE,			 -- code of file, e.g. crawl.html (file crawl HTML), crawl.data (file data extract from HTML)
	location VARCHAR(1000),          -- Location of the file (e.g., folder or system location)
	format VARCHAR(255),             -- File format (e.g., "csv", "txt")
	`separator` VARCHAR(255),        -- Field separator (e.g., ",", ";")
	`columns` TEXT,                  -- Columns in the file (e.g., "product_name,image_url,...")
	destination VARCHAR(1000),       -- Destination table (e.g., "staging.cp_daily")
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- Creation timestamp
	updated_at TIMESTAMP DEFAULT NULL,                      -- Update timestamp
	created_by VARCHAR(255),                        -- User who created the entry
	updated_by VARCHAR(255) DEFAULT NULL                    -- User who last updated the entry
);

ALTER TABLE `logs`
ADD CONSTRAINT fk_data_file_config_id
FOREIGN KEY (data_file_config_id) REFERENCES data_file_configs(id)
ON DELETE CASCADE
ON UPDATE CASCADE;


-- HTML CSV File
INSERT INTO data_file_configs (description, file_name_format, `code`, location, format, `separator`, `columns`, destination, created_by) 
VALUES ('Configuration for daily crawl HTML file', 'cp_daily_html_dd.MM.yyyy', 'crawl.html', 'D:\\DW\\html', 'csv', ',', 'nameHtml,imgHtml,infoHtml,priceHtml', NULL, 'Pham Minh Dung');

-- Data CSV File
INSERT INTO data_file_configs (description, file_name_format, `code`, location, format, `separator`, `columns`, destination, created_by) 
VALUES ('Configuration for daily crawl data file', 'cp_daily_data_dd.MM.yyyy', 'crawl.data', 'D:\\DW\\data', 'csv', ',', 'id,product_name,image_url,size,weight,resolution,sensor,buttons,connection,battery,compatibility,utility,manufacturer,price', 
NULL, 'Pham Minh Dung');