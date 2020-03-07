package com.paytar2800.stockalertcommons.ddb.alert;

public class AlertDDBConstants {

    /**
     * AlertTable constants
     */
    public static final String ALERT_TABLE_NAME = "AlertTable";
    public static final String ALERT_TICKER_KEY = "Ticker";
    public static final String ALERT_USERWATCHLISTID_KEY = "UserWatchlistId";
    public static final String ALERT_USERWATCHLIST_GSI_KEY = "UserWatchlistIdIndex";
    public static final String ALERT_SIMPLEPRICEALERT_KEY = "Price";
    public static final String ALERT_SIMPLEVOLUMEALERT_KEY = "Volume";
    public static final String ALERT_SIMPLEDAILYPERCENTALERT_KEY = "DailyPercent";
    public static final String ALERT_NETPERCENTCHANGEALERT_KEY = "NetPercent";
    public static final String ALERT_LASTTRIGGERTIME_KEY = "TT";
    public static final String ALERT_BASE_PRICE_KEY = "base";
    public static final String ALERT_LOW_PRICE_KEY = "low";
    public static final String ALERT_HIGH_PRICE_KEY = "high";
    public static final String ALERT_LOW_PERCENT_KEY = "low";
    public static final String ALERT_HIGH_PERCENT_KEY = "high";
    public static final String ALERT_TENDAYVOLPERCENT_KEY = "10d";
    public static final String ALERT_THREEMONTHVOLPERCENT_KEY = "3M";
}
