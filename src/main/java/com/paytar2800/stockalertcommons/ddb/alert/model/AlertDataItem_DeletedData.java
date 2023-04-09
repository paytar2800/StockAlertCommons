package com.paytar2800.stockalertcommons.ddb.alert.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.paytar2800.stockalertcommons.ddb.alert.AlertDDBConstants;

import java.lang.reflect.Field;

@DynamoDBTable(tableName = AlertDDBConstants.ALERT_DELETED_DATA_TABLE_NAME)
public class AlertDataItem_DeletedData extends AlertDataItem{

    public AlertDataItem_DeletedData(){}

    public AlertDataItem_DeletedData(AlertDataItem alertDataItem) {
        Field[] fields = alertDataItem.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                field.set(this, field.get(alertDataItem));
            } catch (IllegalAccessException e) {
                // handle exception
            }
        }
    }
}
