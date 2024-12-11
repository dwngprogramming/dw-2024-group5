package com.nlu.app.status;

public class StatusType {
    public static final String PENDING_TO_LOAD_INTO_STAGING = "PENDING_TO_LOAD_INTO_STAGING";           // State number: 1. Chờ lưu vào staging.cp_daily
    public static final String SUCCESS_LOAD_INTO_STAGING = "SUCCESS_LOAD_INTO_STAGING";         // State number: 2. Chờ làm sạch data và lưu vào staging.data_cleaning
    public static final String PENDING_TO_SAVE_DW = "PENDING_TO_SAVE_DW";             // State number: 3. Chờ lưu vào dw.mouses_dim
    public static final String PENDING_TO_SAVE_DATA_MART = "PENDING_TO_SAVE_DATA_MART"; // State number: 4. Chờ lưu vào dw.mouses_data_mart
}
