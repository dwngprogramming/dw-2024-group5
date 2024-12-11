USE staging;

DELIMITER $$

CREATE PROCEDURE data_cleaning()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE daily_product_name VARCHAR(500);
    DECLARE daily_image_url VARCHAR(2083);
    DECLARE daily_size VARCHAR(100);
    DECLARE daily_weight VARCHAR(50);
    DECLARE daily_resolution VARCHAR(150);
    DECLARE daily_sensor VARCHAR(300);
    DECLARE daily_buttons VARCHAR(300);
    DECLARE daily_connection VARCHAR(100);
    DECLARE daily_battery VARCHAR(300);
    DECLARE daily_compatibility VARCHAR(1000);
    DECLARE daily_utility TEXT;
    DECLARE daily_manufacturer VARCHAR(300);
    DECLARE daily_price VARCHAR(50);
		
		-- Setup con trỏ
    DECLARE cursor_cp_daily CURSOR FOR
        SELECT product_name, image_url, size, weight, resolution, sensor, buttons, `connection`, 
               battery, compatibility, utility, manufacturer, price
        FROM staging.cp_daily;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
		
		-- 6. Xóa dữ liệu cũ trong bảng staging.data_cleaning
		IF EXISTS (SELECT 1 FROM staging.data_cleaning) THEN
				DELETE FROM staging.data_cleaning;
		END IF;

    OPEN cursor_cp_daily;

    read_loop: LOOP
        FETCH cursor_cp_daily INTO daily_product_name, daily_image_url, daily_size, daily_weight, 
                                     daily_resolution, daily_sensor, daily_buttons, daily_connection, 
                                     daily_battery, daily_compatibility, daily_utility, 
                                     daily_manufacturer, daily_price;

        IF done THEN
            LEAVE read_loop;
        END IF;
        -- 7. Loại bỏ hoặc thay thế các cột (field) muốn cấu trúc lại để đưa vào dw

		-- Bỏ qua các product_name là combo
        IF daily_product_name LIKE '%Combo%' OR daily_product_name LIKE '%combo%' THEN
            ITERATE read_loop;
        ELSE
						-- Preprocessing cho weight
            -- Xử lý daily_weight để chuẩn hóa thành "# g"
            IF daily_weight IS NOT NULL THEN
                -- Trường hợp chứa '±', '<', '>', '~' (ví dụ: 70±5 g, < 63 g)
                IF daily_weight LIKE '%±%' OR LOCATE('<', daily_weight) > 0 OR LOCATE('>', daily_weight) > 0 OR LOCATE('~', daily_weight) > 0 THEN
                    -- Trường hợp có ký tự '±'
                    IF daily_weight LIKE '%±%' THEN
                        SET daily_weight = CONCAT(
                            ROUND(TRIM(SUBSTRING_INDEX(daily_weight, '±', 1))), 
                            ' g'
                        );
                    -- Trường hợp có ký tự '<'
                    ELSEIF LOCATE('<', daily_weight) > 0 THEN
                        SET daily_weight = TRIM(REPLACE(daily_weight, '<', ''));
                    -- Trường hợp có ký tự '>'
                    ELSEIF LOCATE('>', daily_weight) > 0 THEN
                        SET daily_weight = TRIM(REPLACE(daily_weight, '>', ''));
                    -- Trường hợp có ký tự '~'
                    ELSEIF LOCATE('~', daily_weight) > 0 THEN
                        SET daily_weight = TRIM(REPLACE(daily_weight, '~', ''));
                    END IF;
                -- Trường hợp chứa 'kg'
                ELSEIF daily_weight LIKE '%kg%' THEN
                    SET daily_weight = CONCAT(
                        ROUND(TRIM(REPLACE(daily_weight, 'kg', '')) * 1000), 
                        ' g'
                    );
                -- Trường hợp chứa 'g'
                ELSEIF daily_weight REGEXP '[0-9]+(\\.[0-9]+)?[ ]?g' THEN
                    SET daily_weight = CONCAT(
                        ROUND(TRIM(SUBSTRING_INDEX(daily_weight, 'g', 1))), 
                        ' g'
                    );
                ELSE
                    SET daily_weight = NULL; -- Không hợp lệ thì chuyển thành NULL
                END IF;
            END IF;
        END IF;

        -- Preprocessing daily_price với trường hợp có giá
        IF LOCATE('đ', daily_price) > 0 THEN
            SET daily_price = REPLACE(REPLACE(REPLACE(daily_price, 'đ', ''), '.', ''), ' ', '');
        ELSEIF daily_price = '' OR daily_price LIKE '%Giá Liên Hệ%' THEN
            SET daily_price = -1; -- Nếu giá trị rỗng hoặc "Giá Liên Hệ", chuyển thành -1
        END IF;
				
				-- Preprocessing compatibility với chuột của Apple
				IF LOCATE(LOWER('MacBook'), LOWER(daily_compatibility)) > 0 
					OR LOCATE(LOWER('iMac'), LOWER(daily_compatibility)) > 0
					OR LOCATE(LOWER('iPad'), LOWER(daily_compatibility)) > 0 THEN
						SET daily_compatibility = 'MacOS';
				END IF;
				
				-- Preprocessing compatibility với các trường NULL
				IF daily_compatibility = 'None' THEN
						SET daily_compatibility = 'Windows';
				END IF;
				
				-- Preprocessing connection dựa trên tên (Nếu connection là None)
				IF daily_connection = 'None' THEN
						IF LOCATE(LOWER('không dây'), LOWER(daily_connection)) THEN
								SET daily_connection = 'USB Receiver';
						ELSE
								SET daily_connection = 'Dây USB';
						END IF;
				END IF;
				
				-- Preprocessing connection 2.4G to receiver
				IF LOCATE(LOWER('2.4G'), LOWER(daily_connection)) OR LOCATE(LOWER('2.4 GHz'), LOWER(daily_connection)) THEN
						SET daily_connection = 'USB Receiver';
				END IF;
				
				-- Kiểm tra và chuyển đổi các giá trị None hoặc chuỗi rỗng thành NULL
        IF daily_product_name IS NULL OR daily_product_name = '' OR daily_product_name = 'None' THEN
            SET daily_product_name = NULL;
        END IF;

        IF daily_image_url IS NULL OR daily_image_url = '' OR daily_image_url = 'None' THEN
            SET daily_image_url = NULL;
        END IF;

        IF daily_size IS NULL OR daily_size = '' OR daily_size = 'None' THEN
            SET daily_size = NULL;
        END IF;

        IF daily_resolution IS NULL OR daily_resolution = '' OR daily_resolution = 'None' THEN
            SET daily_resolution = NULL;
        END IF;

        IF daily_sensor IS NULL OR daily_sensor = '' OR daily_sensor = 'None' THEN
            SET daily_sensor = NULL;
        END IF;

        IF daily_buttons IS NULL OR daily_buttons = '' OR daily_buttons = 'None' THEN
            SET daily_buttons = NULL;
        END IF;

        IF daily_connection IS NULL OR daily_connection = '' OR daily_connection = 'None' THEN
            SET daily_connection = NULL;
        END IF;

        IF daily_battery IS NULL OR daily_battery = '' OR daily_battery = 'None' THEN
            SET daily_battery = NULL;
        END IF;

        IF daily_compatibility IS NULL OR daily_compatibility = '' OR daily_compatibility = 'None' THEN
            SET daily_compatibility = NULL;
        END IF;

        IF daily_utility IS NULL OR daily_utility = '' OR daily_utility = 'None' THEN
            SET daily_utility = NULL;
        END IF;

        IF daily_manufacturer IS NULL OR daily_manufacturer = '' OR daily_manufacturer = 'None' THEN
            SET daily_manufacturer = NULL;
        END IF;

        -- 8. Lưu dữ liệu đã được làm sạch
        INSERT INTO staging.data_cleaning (
            product_name, image_url, size, weight, resolution, sensor, 
            buttons, `connection`, battery, compatibility, utility, manufacturer, price
        )
        VALUES (
            daily_product_name, 
            daily_image_url, 
            daily_size, 
            daily_weight, 
            daily_resolution, 
            daily_sensor, 
            daily_buttons, 
            daily_connection, 
            daily_battery, 
            daily_compatibility, 
            daily_utility, 
            daily_manufacturer, 
            daily_price
        );
    END LOOP;

    CLOSE cursor_cp_daily;
END$$

DELIMITER ;

CALL data_cleaning();