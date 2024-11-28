package com.nlu.app.status;

public class StatusType {
    public static final String PENDING_TO_SAVE_TEMP = "PENDING_TO_SAVE_TEMP";           // State number: 1. Chờ lưu vào staging.cp_daily
    public static final String PENDING_TO_CLEAN_DATA = "PENDING_TO_CLEAN_DATA";         // State number: 2. Chờ làm sạch data và lưu vào staging.data_cleaning
    public static final String PENDING_TO_SAVE_DW = "PENDING_TO_SAVE_DW";             // State number: 3. Chờ lưu vào dw.mouses_dim
    public static final String PENDING_TO_SAVE_DATA_MART = "PENDING_TO_SAVE_DATA_MART"; // State number: 4. Chờ lưu vào dw.mouses_data_mart
}
