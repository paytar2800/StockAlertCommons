package com.paytar2800.stockalertcommons.ddb.stock;

public class StockDDBConstants {
    public static final String STOCK_TABLE_NAME = "StockTable";
    public static final String TABLE_TICKER_KEY = "Ticker";
    public static final String TABLE_ALERT_COUNT_KEY = "AlertCount";
    public static final String TABLE_UPDATE_PRIORITY_KEY = "Priority";
    public static final String TABLE_STOCK_EXCHANGE_KEY = "Exchange";
    public static final String TABLE_UPDATE_PRIORITY_GSI_KEY = "PriorityIndex";
    public static final String TABLE_STOCK_EXCHANGE_DEFAULT_VALUE = "DEF";
    public static final String STOCK_DISABLED_VALUE = "DISABLED";
    public static final int MAX_DDB_SAVE_BATCH = 25;
}
