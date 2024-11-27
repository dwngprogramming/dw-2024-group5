package com.nlu.app.status;

public class StatusType {
    public static final String PENDING_TO_SAVE_TEMP = "PENDING_TO_SAVE_TEMP";           // Chờ lưu vào staging.cp_daily
    public static final String PENDING_TO_CLEAN_DATA = "PENDING_TO_CLEAN_DATA";         // Chờ làm sạch data và lưu vào staging.data_cleaning
    public static final String PENDING_TO_SAVE_DIM = "PENDING_TO_SAVE_DIM";             // Chờ lưu vào dw.mouses_dim
    public static final String PENDING_TO_SAVE_DATA_MART = "PENDING_TO_SAVE_DATA_MART"; // Chờ lưu vào dw.mouses_data_mart
}
