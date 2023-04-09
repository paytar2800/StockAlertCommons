package com.paytar2800.stockalertcommons.ddb.alert;

public class AlertDDBConstants {

    /**
     * AlertTable constants
     */
    public static final String ALERT_TABLE_NAME = "AlertTable";
    public static final String ALERT_DELETED_DATA_TABLE_NAME = "AlertTable_deletedData";
    public static final String ALERT_TICKER_KEY = "Ticker";
    public static final String ALERT_USERWATCHLISTID_KEY = "UserWatchlist";
    public static final String ALERT_USERWATCHLIST_GSI_KEY = "UserWatchlistIndex";
    public static final String ALERT_SIMPLEPRICEALERT_KEY = "Price";
    public static final String ALERT_SIMPLEVOLUMEALERT_KEY = "Vol";
    public static final String ALERT_SIMPLEDAILYPERCENTALERT_KEY = "DailyPer";
    public static final String ALERT_NETPERCENTCHANGEALERT_KEY = "NetPer";
    public static final String ALERT_LASTTRIGGERTIME_KEY = "TT";
    public static final String ALERT_BASE_PRICE_KEY = "Ba";
    public static final String ALERT_LOW_PRICE_KEY = "Lo";
    public static final String ALERT_HIGH_PRICE_KEY = "Hi";
    public static final String ALERT_LOW_PERCENT_KEY = "Lo";
    public static final String ALERT_HIGH_PERCENT_KEY = "Hi";
    public static final String ALERT_TENDAYVOLPERCENT_KEY = "10d";
    public static final String ALERT_THREEMONTHVOLPERCENT_KEY = "3M";
    public static final String TABLE_HAS_CHANGED_KEY = "Changed";
    public static final String TABLE_HAS_CHANGED_GSI_KEY = "ChangedIndex";
}
