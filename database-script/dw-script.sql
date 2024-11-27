CREATE DATABASE IF NOT EXISTS dw;
USE dw;

CREATE TABLE IF NOT EXISTS mouses_dim (
	id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	date_id INT NOT NULL,
	product_name VARCHAR(500),
	image_url VARCHAR(2083),
	size VARCHAR(300),
	weight DOUBLE,
	resolution VARCHAR(150),
	sensor VARCHAR(300),
	buttons VARCHAR(300),
	`connection` VARCHAR(300),
	battery VARCHAR(300),
	compatibility VARCHAR(300),
	utility TEXT,
	manufacturer VARCHAR(300),
	price DOUBLE
);

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