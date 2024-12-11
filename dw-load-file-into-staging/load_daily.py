import pymysql
import os
import datetime
import configparser

# Lấy ngày hiện tại
today = datetime.datetime.now().strftime('%d.%m.%Y')

# Hàm lấy cấu hình cho tệp CSV từ cơ sở dữ liệu
def fetch_db_config(cursor):
    query = """
        SELECT file_name, `separator`, `columns`, destination, location, format
        FROM data_file_configs
    """
    cursor.execute(query)
    result = cursor.fetchone()
    #lưu dữ liệu config vào các biến để sử dụng
    if result:
        #đường dẫn đầy đủ đến file data cần load
        full_file_path = os.path.join(result[4], f"{result[0]}_{today}.{result[5]}").replace("\\", "/")
        #Tên đầy đủ của file data đã được sẵn sàng chờ load vào staging
        full_file_name = f"{result[0]}_{today}.{result[5]}"
        return {
            'file_path': full_file_path,
            'delimiter': result[1],
            'columns': result[2].split(','),
            'destination_table': result[3],
            'file_name': full_file_name
        }
    else:
        print("No configuration found in data_file_configs.")
        return None

# Hàm ghi log vào bảng logs trong cơ sở dữ liệu control
def log_to_control(cursor, status, file_name, message, row_count=None):
    query = """
        INSERT INTO logs (df_config_id, name, status, row_count, note, created_by)
        VALUES (%s, %s, %s, %s, %s, %s)
    """
    cursor.execute(query, (1, file_name, status, row_count, message, 'admin'))

# Hàm kiểm tra trạng thái file trong bảng logs
def check_pending_file(cursor, file_name):
    # kiểm tra xem file đã được load vào staging trước đó hay chưa
    query = """
        SELECT name FROM logs
        WHERE status = 'SUCCESS_LOAD_INTO_STAGING' AND name = %s
    """
    cursor.execute(query, (file_name,))
    result = cursor.fetchone()
    if result:
        print(f"The file {file_name} has already been loaded into staging successfully. No need to load again.")
        return None 

    # kiểm tra xem file đã có và sẵn sàng để load vào staging chưa
    query = """
        SELECT name FROM logs
        WHERE status = 'PENDING_TO_LOAD_INTO_STAGING' AND name = %s
    """
    cursor.execute(query, (file_name,))
    result = cursor.fetchone()
    return result[0] if result else None

# Hàm kết nối đến cơ sở dữ liệu MySQL
def connect_to_db(host, user, password, database):
    return pymysql.connect(host=host, user=user, password=password, database=database)

# Hàm tải dữ liệu từ CSV vào bảng staging sử dụng LOAD DATA INFILE
def load_csv_with_infile(cursor, file_path, table_name, delimiter, columns):
    
    try:
        columns_str = ', '.join(columns)
        query = f"""
            LOAD DATA INFILE '{file_path}'
            INTO TABLE {table_name}
            FIELDS TERMINATED BY '{delimiter}'
            LINES TERMINATED BY '\\n'
            IGNORE 1 ROWS
            ({columns_str});
        """

        print(query)
        cursor.execute(query)
        print(f"Data from {file_path} loaded successfully into {table_name}.")
    except Exception as e:
         # Ghi log lỗi nếu xảy ra
        log_to_control(cursor_control, 'ERROR', file_name, f"Error loading to {table_name}: {e}")
        raise RuntimeError(f"Failed to load data using LOAD DATA INFILE: {e}")

# Hàm so sánh dữ liệu và cập nhật bảng cp_daily
def update_cp_daily(cursor_staging, cursor_control, columns, table_name, temp_table_name, file_name):
    try:
        # Tạo câu lệnh SET và WHERE động với xử lý NULL
        set_clause = ", ".join([f"cd.{col} = cdt.{col}" for col in columns if col != 'id'])
        where_clause = " OR ".join([f"IFNULL(cd.{col}, '') != IFNULL(cdt.{col}, '')" for col in columns if col != 'id'])

        # Câu truy vấn UPDATE để cập nhật các bản ghi hiện có
        query_update = f"""
            UPDATE {table_name} cd
            JOIN {temp_table_name} cdt ON cd.id = cdt.id
            SET {set_clause}, cd.load_time = NOW()
            WHERE {where_clause};
        """
        cursor_staging.execute(query_update)
        updated_rows = cursor_staging.rowcount

        # Tạo danh sách cột để chèn dữ liệu mới
        insert_columns = ", ".join(columns + ['load_time'])
        select_columns = ", ".join([f"cdt.{col}" for col in columns]) + ", NOW()"

        # Câu truy vấn INSERT để thêm các bản ghi mới
        query_insert = f"""
            INSERT INTO {table_name} ({insert_columns})
            SELECT {select_columns}
            FROM {temp_table_name} cdt
            LEFT JOIN {table_name} cd ON cdt.id = cd.id
            WHERE cd.id IS NULL;
        """
        cursor_staging.execute(query_insert)
        inserted_rows = cursor_staging.rowcount

        #  Xóa dữ liệu trong bảng tạm
        cursor_staging.execute(f"TRUNCATE TABLE {temp_table_name};")

        # Ghi log thành công
        log_to_control(cursor_control, 'SUCCESS_LOAD_INTO_STAGING', file_name, 
                       f"{updated_rows} rows updated, {inserted_rows} rows inserted.")

        print(f"Update completed: {updated_rows} rows updated, {inserted_rows} rows inserted.")
    except Exception as e:
        # Ghi log lỗi nếu xảy ra
        log_to_control(cursor_control, 'ERROR', file_name, f"Error updating {table_name}: {e}")
        raise RuntimeError(f"Error updating {table_name}: {e}")

def main():
    # Đọc file config.properties
    config = configparser.ConfigParser()
    config.read('config.properties')
    db_host = config['database']['host']
    db_user = config['database']['user']
    db_password = config['database']['password']
    db_control = config['database']['control_db']
    db_staging = config['database']['staging_db']

    # Kết nối đến database control và staging
    conn_control = connect_to_db(db_host, db_user, db_password, db_control)
    conn_staging = connect_to_db(db_host, db_user, db_password, db_staging)
    cursor_control = conn_control.cursor()
    cursor_staging = conn_staging.cursor()

    try:
        # Ghi log trạng thái bắt đầu
        log_to_control(cursor_control, 'RUNNING_LOAD_CP_DAILY', 'N/A', 'Starting update process.')
        conn_control.commit()

        #  Tải dữ liệu từ CSV vào bảng tạm
        config = fetch_db_config(cursor_control)
        
        if not config:
            print("Configuration not found. Process terminated.")
            log_to_control(cursor_control, 'ERROR', 'Configuration not found.')
            conn_control.commit()
            conn_control.close()
            return

        file_path = config['file_path']
        delimiter = config['delimiter']
        columns = config['columns']
        destination_table = config['destination_table']
        temp_table_name = f"{destination_table}_temp"
        
        # Kiểm tra xem file có trạng thái "PENDING_TO_LOAD_INTO_STAGING" trong bảng logs không
        file_name = os.path.basename(file_path)

        print(file_name)
        pending_file = check_pending_file(cursor_control, file_name)
        if not pending_file:
            print("No pending file for today. Process terminated.")
            log_to_control(cursor_control, 'ERROR', file_name,'No pending file found for today.')
            conn_control.commit()
            conn_control.close()
            return

        # Ghi log cho trạng thái lấy cấu hình thành công
        log_to_control(cursor_control, 'CONFIG_RETRIEVED',file_name, 'Configuration successfully retrieved.')
        conn_control.commit()
        # load file csv vào table cp_daily_temp
        load_csv_with_infile(cursor_staging, file_path, temp_table_name, delimiter, columns)
        conn_staging.commit()

        # So sánh và cập nhật bảng chính cp_daily
        update_cp_daily(cursor_staging, cursor_control, columns, destination_table, temp_table_name, file_name)
        conn_staging.commit()
        conn_staging.commit()
    except Exception as e:
        print(f"Error: {e}")
    finally:
        conn_control.commit()
        conn_control.close()
        conn_staging.close()

if __name__ == "__main__":
    main()
