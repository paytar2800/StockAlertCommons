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

import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_BASE_PARAM;
import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_HIGH_PARAM;
import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_LOW_PARAM;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@DynamoDBDocument
public class NetPercentChangeAlertItem implements IAlertDBItem {

    @SerializedName(API_LOW_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_LOW_KEY)
    private Double lowPercent;

    @SerializedName(API_HIGH_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_HIGH_KEY)
    private Double highPercent;

    @SerializedName(API_BASE_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_BASE_PRICE_KEY)
    private Double basePrice;

    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_LASTTRIGGERTIME_KEY)
    private transient Long triggerTime;

    @DynamoDBIgnore
    @Override
    public boolean isEmpty() {
        return isItemEmpty(lowPercent) && isItemEmpty(highPercent);
    }

    @DynamoDBIgnore
    @Override
    public String getDBKeyName() {
        return AlertDDBConstants.ALERT_NETPERCENTCHANGEALERT_KEY;
    }
}
