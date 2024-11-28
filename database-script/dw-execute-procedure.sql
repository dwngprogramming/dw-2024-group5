USE dw;

DELIMITER $$

CREATE PROCEDURE IF NOT EXISTS transfer_data_to_mouses_dim()
BEGIN
    -- Khai báo biến cho vòng lặp đầu tiên
    DECLARE done_loop1 INT DEFAULT 0;
    DECLARE transform_product_name VARCHAR(500);
    DECLARE transform_image_url VARCHAR(2083);
    DECLARE transform_size VARCHAR(100);
    DECLARE transform_weight VARCHAR(10);
    DECLARE transform_resolution VARCHAR(150);
    DECLARE transform_sensor VARCHAR(300);
    DECLARE transform_buttons VARCHAR(300);
    DECLARE transform_connection_text VARCHAR(100);
    DECLARE transform_battery VARCHAR(300);
    DECLARE transform_compatibility_text VARCHAR(1000);
    DECLARE transform_utility TEXT;
    DECLARE transform_manufacturer VARCHAR(300);
    DECLARE transform_price DOUBLE;
    DECLARE transform_brand_id INT;
    DECLARE transform_mouse_id INT;
    DECLARE single_value VARCHAR(100);
    DECLARE transform_today_date_id INT;
    DECLARE record_count INT;

    -- Khai báo biến cho vòng lặp thứ hai
    DECLARE existing_product_name VARCHAR(500);

    -- Khai báo con trỏ cho vòng lặp đầu tiên (dữ liệu mới từ staging)
    DECLARE cur CURSOR FOR 
        SELECT product_name, image_url, size, weight, resolution, sensor, buttons, 
               `connection`, battery, compatibility, utility, manufacturer, price 
        FROM staging.data_cleaning;

    -- Khai báo con trỏ cho vòng lặp thứ hai (kiểm tra dữ liệu cũ)
    DECLARE check_cur CURSOR FOR 
        SELECT product_name FROM dw.mouses_dim;

    -- Khai báo handler chung cho cả hai vòng lặp
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done_loop1 = 1;

    -- Mở con trỏ cho vòng lặp đầu tiên
    OPEN cur;

    -- Vòng lặp đầu tiên: chuyển dữ liệu từ staging
    read_loop: LOOP
        -- Lấy từng dòng dữ liệu từ staging
        FETCH cur INTO transform_product_name, transform_image_url, transform_size, transform_weight, 
                     transform_resolution, transform_sensor, transform_buttons, transform_connection_text, 
                     transform_battery, transform_compatibility_text, transform_utility, transform_manufacturer, transform_price;

        IF done_loop1 THEN
            LEAVE read_loop;
        END IF;

        -- 1. Lấy ra transform_brand_id từ brand_dim và transform_today_date_id từ date_dim (Date của hôm nay)
        SELECT id INTO transform_brand_id FROM dw.brand_dim WHERE `name` = transform_manufacturer;
        SELECT id INTO transform_today_date_id FROM dw.date_dim WHERE date = CURRENT_DATE;

        -- 2. Kiểm tra xem sản phẩm đã tồn tại trong mouses_dim chưa
        SELECT COUNT(*) INTO record_count 
        FROM dw.mouses_dim 
        WHERE product_name = transform_product_name;

        IF record_count = 0 THEN  -- Nếu chưa tồn tại, tiến hành INSERT
            INSERT INTO dw.mouses_dim (brand_id, date_id, product_name, image_url, size, weight, resolution, sensor, buttons, 
                                       battery, utility, price, non_updated_count, is_expired)
            VALUES (transform_brand_id, transform_today_date_id, transform_product_name, transform_image_url, transform_size, 
                    transform_weight, transform_resolution, transform_sensor, transform_buttons, 
                    transform_battery, transform_utility, transform_price, 0, FALSE);

            SET transform_mouse_id = LAST_INSERT_ID();

            -- 3. Process transform_connection_text và insert vào mouse_connection_dim
            IF transform_connection_text IS NOT NULL THEN
                WHILE LENGTH(transform_connection_text) > 0 DO
                    SET single_value = TRIM(SUBSTRING_INDEX(transform_connection_text, ',', 1));
                    SET transform_connection_text = TRIM(LEADING ',' FROM SUBSTRING(transform_connection_text, LENGTH(single_value) + 2));
                    
                    -- Thêm vào bảng trung gian mouse_connection_dim
                    INSERT INTO dw.mouse_connection_dim (mouse_id, connection_id)
                    SELECT transform_mouse_id, conn.id 
                    FROM dw.connection_dim conn 
                    WHERE conn.`name` = single_value;
                END WHILE;
            END IF;

            -- 4. Process transform_compatibility_text và insert vào mouse_compatibility_dim
            IF transform_compatibility_text IS NOT NULL THEN
                WHILE LENGTH(transform_compatibility_text) > 0 DO
                    SET single_value = TRIM(SUBSTRING_INDEX(transform_compatibility_text, ',', 1));
                    SET transform_compatibility_text = TRIM(LEADING ',' FROM SUBSTRING(transform_compatibility_text, LENGTH(single_value) + 2));
                    
                    -- Thêm vào bảng trung gian mouse_compatibility_dim
                    INSERT INTO dw.mouse_compatibility_dim (mouse_id, compatibility_id)
                    SELECT transform_mouse_id, comp.id 
                    FROM dw.compatibility_dim comp
                    WHERE comp.`name` = single_value;
                END WHILE;
            END IF;
        END IF;  -- Kết thúc kiểm tra tồn tại
    END LOOP;
    CLOSE cur;

    -- Reset biến done_loop1 để sử dụng lại trong vòng lặp thứ hai
    SET done_loop1 = 0;

    -- Mở con trỏ cho vòng lặp thứ hai
    OPEN check_cur;

    -- Vòng lặp thứ hai: Kiểm tra tên sản phẩm cũ
    check_loop: LOOP
        FETCH check_cur INTO existing_product_name;

        IF done_loop1 THEN
            LEAVE check_loop;
        END IF;

        -- Kiểm tra sản phẩm có tồn tại trong staging hay không
        IF NOT EXISTS (
            SELECT 1 FROM staging.data_cleaning 
            WHERE product_name = existing_product_name
        ) THEN
            -- Tăng non_updated_count hoặc đặt is_expired nếu >= 5
            UPDATE dw.mouses_dim
            SET non_updated_count = non_updated_count + 1,
                is_expired = CASE 
                                WHEN non_updated_count + 1 >= 5 THEN TRUE 
                                ELSE FALSE 
                             END
            WHERE product_name = existing_product_name;
        END IF;
    END LOOP;
    CLOSE check_cur;

END$$

DELIMITER ;

CALL transfer_data_to_mouses_dim();