CREATE DATABASE IF NOT EXISTS staging;
USE staging;

CREATE TABLE IF NOT EXISTS cp_daily (
	id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	product_name VARCHAR(500),
	image_url VARCHAR(2083),
	size VARCHAR(100),
	weight VARCHAR(50),
	resolution VARCHAR(150),
	sensor VARCHAR(300),
	buttons VARCHAR(300),
	`connection` VARCHAR(100),
	battery VARCHAR(300),
	compatibility VARCHAR(1000),
	utility TEXT,
	manufacturer VARCHAR(300),
	price VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS data_cleaning (
	id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	product_name VARCHAR(500),
	image_url VARCHAR(2083),
	size VARCHAR(100),
	weight VARCHAR(10),
	resolution VARCHAR(150),
	sensor VARCHAR(300),
	buttons VARCHAR(300),
	`connection` VARCHAR(100),
	battery VARCHAR(300),
	compatibility VARCHAR(1000),
	utility TEXT,
	manufacturer VARCHAR(300),
	price DOUBLE
);