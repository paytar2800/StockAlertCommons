package com.paytar2800.stockalertcommons.ddb.alert.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.google.gson.annotations.SerializedName;
import com.paytar2800.stockalertcommons.ddb.alert.AlertDDBConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_TENDAY_PARAM;
import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_THREEMONTH_PARAM;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@DynamoDBDocument
public class SimpleVolumePercentAlertItem implements IAlertDBItem{

    @SerializedName(API_TENDAY_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_TENDAYVOLPERCENT_KEY)
    private Double tenDayVolPercent;

    @SerializedName(API_THREEMONTH_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_THREEMONTHVOLPERCENT_KEY)
    private Double threeMonthVolPercent;

    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_LASTTRIGGERTIME_KEY)
    private transient Long lastTriggeredTime;

    @DynamoDBIgnore
    @Override
    public boolean isEmpty() {
        return isItemEmpty(tenDayVolPercent) && isItemEmpty(threeMonthVolPercent);
    }

    @Override
    public Long getTriggerTime() {
        return lastTriggeredTime;
    }

    @DynamoDBIgnore
    @Override
    public String getDBKeyName() {
        return AlertDDBConstants.ALERT_SIMPLEVOLUMEALERT_KEY;
    }
}
