CREATE TABLE `data_file_configs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` text,
  `file_name` varchar(1000) DEFAULT NULL,
  `location` varchar(1000) DEFAULT NULL,
  `format` varchar(255) DEFAULT NULL,
  `separator` varchar(255) DEFAULT NULL,
  `columns` text,
  `destination` varchar(1000) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
