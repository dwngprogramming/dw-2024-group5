USE dw;

DELIMITER $$

CREATE PROCEDURE IF NOT EXISTS insert_brand_dim()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE brand_name VARCHAR(255);
    
    -- Cursor để duyệt qua các giá trị distinct manufacturer từ staging.data_cleaning
    DECLARE cur CURSOR FOR 
        SELECT DISTINCT manufacturer FROM staging.data_cleaning;

    -- Handler để bắt điều kiện hết dòng (END OF CURSOR)
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    -- Mở cursor
    OPEN cur;

    read_loop: LOOP
        FETCH cur INTO brand_name;
        
        IF done THEN
            LEAVE read_loop;
        END IF;

        -- Insert vào brand_dim nếu manufacturer chưa tồn tại
        IF NOT EXISTS (
            SELECT 1 FROM brand_dim WHERE `name` = brand_name
        ) THEN
            INSERT INTO brand_dim (`name`) VALUES (brand_name);
        END IF;

    END LOOP;
	
    -- Đóng cursor
    CLOSE cur;
END$$

CREATE PROCEDURE IF NOT EXISTS insert_compatibility_dim()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE compatibility_text TEXT;
    DECLARE single_value VARCHAR(255);

    -- Cursor để duyệt qua tất cả giá trị compatibility từ staging.data_cleaning
    DECLARE cur CURSOR FOR 
        SELECT DISTINCT compatibility FROM staging.data_cleaning;

    -- Handler để bắt điều kiện hết dữ liệu
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    -- Mở cursor
    OPEN cur;

    read_loop: LOOP
        FETCH cur INTO compatibility_text;

        IF done THEN
            LEAVE read_loop;
        END IF;

        -- Bóc tách từng giá trị trong compatibility_text
        WHILE LENGTH(compatibility_text) > 0 DO
            SET single_value = TRIM(SUBSTRING_INDEX(compatibility_text, ',', 1));
            
            -- Chèn vào compatibility_dim nếu giá trị chưa tồn tại
            IF NOT EXISTS (
                SELECT 1 FROM dw.compatibility_dim WHERE `name` = single_value
            ) THEN
                INSERT INTO dw.compatibility_dim (`name`) VALUES (single_value);
            END IF;

            -- Cập nhật chuỗi để loại bỏ giá trị đã xử lý
            SET compatibility_text = TRIM(LEADING ',' FROM SUBSTRING(compatibility_text, LENGTH(single_value) + 2));
        END WHILE;
    END LOOP;

    -- Đóng cursor
    CLOSE cur;
END$$

-- Procedure thêm 
CREATE PROCEDURE IF NOT EXISTS insert_connection_dim()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE connection_text TEXT;
    DECLARE single_value VARCHAR(100);

    -- Cursor để duyệt qua tất cả giá trị connection từ staging.data_cleaning
    DECLARE cur CURSOR FOR 
        SELECT DISTINCT connection FROM staging.data_cleaning;

    -- Handler để bắt điều kiện hết dữ liệu
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    -- Mở cursor
    OPEN cur;

    read_loop: LOOP
        FETCH cur INTO connection_text;

        IF done THEN
            LEAVE read_loop;
        END IF;

        -- Bóc tách từng giá trị trong connection_text
        WHILE LENGTH(connection_text) > 0 DO
            SET single_value = TRIM(SUBSTRING_INDEX(connection_text, ',', 1));
            
            -- Chèn vào connection_dim nếu giá trị chưa tồn tại
            IF NOT EXISTS (
                SELECT 1 FROM dw.connection_dim WHERE `name` = single_value
            ) THEN
                INSERT INTO dw.connection_dim (`name`) VALUES (single_value);
            END IF;

            -- Cập nhật chuỗi để loại bỏ giá trị đã xử lý
            SET connection_text = TRIM(LEADING ',' FROM SUBSTRING(connection_text, LENGTH(single_value) + 2));
        END WHILE;
    END LOOP;

    -- Đóng cursor
    CLOSE cur;
END$$

DELIMITER ;

CALL insert_brand_dim();
CALL insert_compatibility_dim();
CALL insert_connection_dim();