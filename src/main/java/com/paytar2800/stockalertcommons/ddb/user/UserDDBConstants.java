package com.paytar2800.stockalertcommons.ddb.user;

public class UserDDBConstants {

    public static final String TABLE_NAME = "UserTable";
    public static final String TABLE_EMAIL_KEY = "Email";
    public static final String TABLE_EMAIL_GSI_KEY = "EmailIndex";
    public static final String TABLE_USERID_KEY = "UserId";
    public static final String TABLE_ALERTSNOOZETIME_KEY = "SnoozeTime";
    public static final String TABLE_ISALERTENABLED_KEY = "AlertEnabled";
    public static final String TABLE_ISEXTENDEDHOURSENABLED_KEY = "ExtendHourEnabled";
    public static final String TABLE_SUB_STATUS_KEY = "SubStatus";
    public static final String TABLE_DEVICE_TOKEN_KEY = "DeviceToken";
    public static final String TABLE_HAS_CHANGED_KEY = "Changed";
    public static final String TABLE_HAS_CHANGED_GSI_KEY = "ChangedIndex";

    public static final int UNSUBSCRIBED_CODE = -1;
    public static final int PREMIUM_SUBSCRIBED_CODE = 1;
}
