package com.paytar2800.stockalertcommons.ddb.alert.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.google.gson.annotations.SerializedName;
import com.paytar2800.stockalertcommons.api.APIParamConstants;
import com.paytar2800.stockalertcommons.ddb.alert.AlertDDBConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.paytar2800.stockalertcommons.ddb.alert.AlertDDBConstants.ALERT_PRICE_VS_200D_AVG_ALERT_PARAM;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@DynamoDBDocument
public class PriceVsTwoHundredDaySMAAlertItem implements IAlertDBItem {

    @SerializedName(APIParamConstants.API_LOW_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_LOW_KEY)
    private Double low;

    @SerializedName(APIParamConstants.API_HIGH_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_HIGH_KEY)
    private Double high;

    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_LASTTRIGGERTIME_KEY)
    private transient Long triggerTime;

    @DynamoDBIgnore
    @Override
    public boolean isEmpty() {
        return isItemEmpty(low) && isItemEmpty(high);
    }

    @DynamoDBIgnore
    @Override
    public String getDBKeyName() {
        return ALERT_PRICE_VS_200D_AVG_ALERT_PARAM;
    }
}
