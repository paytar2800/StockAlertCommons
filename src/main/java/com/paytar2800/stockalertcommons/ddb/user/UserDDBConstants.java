package com.paytar2800.stockalertcommons.ddb.user;

public class UserDDBConstants {

    public static final String TABLE_NAME = "UserTable";
    public static final String TABLE_EMAIL_KEY = "EmailId";
    public static final String TABLE_EMAIL_GSI_KEY = "EmailIdIndex";
    public static final String TABLE_USERID_KEY = "UserId";
    public static final String TABLE_ALERTSNOOZETIME_KEY = "AlertSnoozeTimeSeconds";
    public static final String TABLE_ISALERTENABLED_KEY = "IsAlertEnabled";
    public static final String TABLE_SUB_STATUS_KEY = "SubscriptionStatus";

    public static final int UNSUBSCRIBED_CODE = -1;
    public static final int PREMIUM1_CODE = 1;
    public static final int PREMIUM2_CODE = 2;
}
