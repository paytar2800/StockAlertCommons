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

import static com.paytar2800.stockalertcommons.api.APIParamConstants.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@DynamoDBDocument
public class SimplePriceAlertItem implements IAlertDBItem {

    @SerializedName(API_LOW_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_LOW_KEY)
    private Double lowPrice;

    @SerializedName(API_HIGH_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_HIGH_KEY)
    private Double highPrice;

    @SerializedName(API_RECUR_FACTOR_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_RECUR_PARAM_KEY)
    private Double recurFactor;

    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_LAST_TRIGGER_PRICE_PARAM_KEY)
    private Double lastTriggerPrice;

    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_LASTTRIGGERTIME_KEY)
    private transient Long triggerTime;

    @DynamoDBIgnore
    @Override
    public boolean isEmpty() {
        return isItemEmpty(lowPrice) && isItemEmpty(highPrice);
    }

    @DynamoDBIgnore
    @Override
    public String getDBKeyName() {
        return AlertDDBConstants.ALERT_SIMPLEPRICEALERT_KEY;
    }
}
