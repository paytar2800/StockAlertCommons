package com.paytar2800.stockalertcommons.ddb.watchlist;

public class WatchlistDDBConstants {

    public static final String WATCHLIST_TABLE_NAME = "WatchlistTable";
    public static final String TABLE_NAME = WATCHLIST_TABLE_NAME;

    public static final String WATCHLIST_METADATA_GSI = "MetadataIndex";

    public static final String TABLE_USER_ID_KEY = "UserId";
    public static final String TABLE_WATCHLIST_ID_KEY = "WId";
    public static final String TABLE_WATCHLIST_NAME_KEY = "Name";
    public static final String TABLE_DISPLAY_ORDER_KEY = "Ord";
    public static final String TABLE_TICKERS_KEY = "Ticks";
    public static final String TABLE_LAST_UPDATE_TIME_KEY = "LUT";

    public static final String KEY_USER_ID = TABLE_USER_ID_KEY;
    public static final String KEY_WATCHLIST_ID = TABLE_WATCHLIST_ID_KEY;
    public static final String KEY_WATCHLIST_NAME = TABLE_WATCHLIST_NAME_KEY;
    public static final String KEY_DISPLAY_ORDER = TABLE_DISPLAY_ORDER_KEY;
    public static final String KEY_TICKERS = TABLE_TICKERS_KEY;
    public static final String KEY_LAST_UPDATE_TIME = TABLE_LAST_UPDATE_TIME_KEY;
}
