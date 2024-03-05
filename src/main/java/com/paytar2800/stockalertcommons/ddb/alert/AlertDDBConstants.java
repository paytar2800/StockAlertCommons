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

    public static final String ALERT_PRICE_LADDER_ALERT_KEY = "ladr";
    public static final String ALERT_SIMPLEVOLUMEALERT_KEY = "Vol";
    public static final String ALERT_SIMPLEDAILYPERCENTALERT_KEY = "DailyPer";
    public static final String ALERT_NETPERCENTCHANGEALERT_KEY = "NetPer";
    public static final String ALERT_FORWARD_PE_ALERT_KEY = "ForwPE";
    public static final String ALERT_TRAILING_PE_ALERT_KEY = "TrailPE";
    public static final String ALERT_EARNINGS_ALERT_KEY = "EarnD";
    public static final String ALERT_DIVIDEND_ALERT_KEY = "ExDivD";
    public static final String ALERT_SHORTPERCENTFLOAT_ALERT_KEY = "StPerFl";
    public static final String ALERT_SHORTRATIO_ALERT_KEY = "StRatio";
    public static final String ALERT_PEGRATIO_ALERT_KEY = "PegRatio";
    public static final String ALERT_FIFTYDAYAVG_ALERT_KEY = "50dAvg";
    public static final String ALERT_TWOHUNDREDDAYAVG_ALERT_KEY = "200dAvg";

    public static final String ALERT_LASTTRIGGERTIME_KEY = "TT";
    public static final String ALERT_BASE_PRICE_KEY = "Ba";

    public static final String ALERT_LOW_KEY = "Lo";

    public static final String ALERT_HIGH_KEY = "Hi";

    public static final String ALERT_LADDER_STEP_PRICE_PARAM_KEY = "SP";
    public static final String ALERT_LAST_ACKNOWLEDGED_STEP_PARAM_KEY = "AS";
    public static final String ALERT_DAY__KEY = "D";
    public static final String ALERT_TENDAYVOLPERCENT_KEY = "10d";
    public static final String ALERT_THREEMONTHVOLPERCENT_KEY = "3M";
    public static final String TABLE_HAS_CHANGED_KEY = "Changed";
    public static final String TABLE_HAS_CHANGED_GSI_KEY = "ChangedIndex";
}
