package com.paytar2800.stockalertcommons.ddb.alert.model;


import com.paytar2800.stockalertcommons.ddb.alert.AlertDDBConstants;

public interface IAlertDBItem {
    boolean isEmpty();

    default boolean isItemEmpty(Double val) {
        return val == null || val <= 0L;
    }

    Long getTriggerTime();

    String getDBKeyName();

    default String getTriggerTimeDBKeyName() {
        return AlertDDBConstants.ALERT_LASTTRIGGERTIME_KEY;
    }
}
