USE dw;

-- Bảng brand_dim
CREATE TABLE IF NOT EXISTS brand_dim (
	id INT AUTO_INCREMENT PRIMARY KEY,
	`name` VARCHAR(300) UNIQUE NOT NULL,
	`description` TEXT,
	created_date DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Bảng compatibility_dim
CREATE TABLE IF NOT EXISTS compatibility_dim (
	id INT AUTO_INCREMENT PRIMARY KEY,
	`name` VARCHAR(1000) UNIQUE NOT NULL,
	`description` TEXT,
	created_date DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Bảng connection_dim
CREATE TABLE IF NOT EXISTS connection_dim (
	id INT AUTO_INCREMENT PRIMARY KEY,
	`name` VARCHAR(100) UNIQUE NOT NULL,
	`description` TEXT,
	created_date DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dw.mouses_dim (
  id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	brand_id INT NOT NULL,
	date_id INT NOT NULL,
  product_name VARCHAR(500) UNIQUE,
  image_url VARCHAR(2083),
  size VARCHAR(100),
  weight VARCHAR(10),
  resolution VARCHAR(150),
  sensor VARCHAR(300),
  buttons VARCHAR(300),
  battery VARCHAR(300),
  utility TEXT,
  price DOUBLE,
	non_updated_count INT DEFAULT 0,
	is_expired TINYINT DEFAULT 0,
  FOREIGN KEY (brand_id) REFERENCES dw.brand_dim(id),
	FOREIGN KEY (date_id) REFERENCES dw.date_dim(id)
);

-- Bảng trung gian quan hệ n-n của chuột và connection
CREATE TABLE IF NOT EXISTS dw.mouse_connection_dim (
	id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
  mouse_id INT NOT NULL,
  connection_id INT NOT NULL,
  FOREIGN KEY (mouse_id) REFERENCES dw.mouses_dim(id),
  FOREIGN KEY (connection_id) REFERENCES dw.connection_dim(id)
);

-- Bảng trung gian quan hệ n-n của chuột và compatibility
CREATE TABLE IF NOT EXISTS dw.mouse_compatibility_dim (
	id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
  mouse_id INT NOT NULL,
  compatibility_id INT NOT NULL,
  FOREIGN KEY (mouse_id) REFERENCES dw.mouses_dim(id),
  FOREIGN KEY (compatibility_id) REFERENCES dw.compatibility_dim(id)
);

-- Bảng date_dim
CREATE TABLE IF NOT EXISTS date_dim (
	id INT AUTO_INCREMENT PRIMARY KEY,
	date DATE NOT NULL UNIQUE,
	`year` INT NOT NULL,
	`quarter` INT NOT NULL,
	`month` INT NOT NULL,
	`day` INT NOT NULL,
	week_of_year INT NOT NULL,
	day_of_week VARCHAR(10) NOT NULL,
	is_weekend BOOLEAN NOT NULL,
	is_holiday BOOLEAN DEFAULT FALSE
);