package com.paytar2800.stockalertcommons.ddb.alert.model;


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.paytar2800.stockalertcommons.ddb.alert.AlertDDBConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_ISNEW_ALERT_PARAM;
import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_NETPERCENTCHANGEALERT_PARAM;
import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_SIMPLEDAILYPERCENTALERT_PARAM;
import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_SIMPLEPRICEALERT_PARAM;
import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_SIMPLEVOLUMEALERT_PARAM;
import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_TICKER_PARAM;
import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_USERWATCHLISTID_PARAM;


/**
 * Alert Data table
 * Table name = AlertTable
 * Partition key = Ticker , Range Key = UserId + "/" + WatchListId
 * Alert items are stored as DynamoDBDocument for different alert type
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = AlertDDBConstants.ALERT_TABLE_NAME)
public class AlertDataItem implements IAlertDBItem {

    @SerializedName(API_TICKER_PARAM)
    @DynamoDBHashKey(attributeName = AlertDDBConstants.ALERT_TICKER_KEY)
    private String ticker;

    @SerializedName(API_USERWATCHLISTID_PARAM)
    @DynamoDBIndexHashKey(attributeName = AlertDDBConstants.ALERT_USERWATCHLISTID_KEY,
            globalSecondaryIndexName = AlertDDBConstants.ALERT_USERWATCHLIST_GSI_KEY)
    @DynamoDBTypeConverted(converter = UserWatchlistId.class)
    @DynamoDBRangeKey(attributeName = AlertDDBConstants.ALERT_USERWATCHLISTID_KEY)
    private UserWatchlistId userWatchlistId;

    @SerializedName(API_SIMPLEPRICEALERT_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_SIMPLEPRICEALERT_KEY)
    private SimplePriceAlertItem simplePriceAlertItem;

    @SerializedName(API_SIMPLEDAILYPERCENTALERT_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_SIMPLEDAILYPERCENTALERT_KEY)
    private SimpleDailyPercentAlertItem simpleDailyPercentAlertItem;

    @SerializedName(API_NETPERCENTCHANGEALERT_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_NETPERCENTCHANGEALERT_KEY)
    private NetPercentChangeAlertItem netPercentChangeAlertItem;

    @SerializedName(API_SIMPLEVOLUMEALERT_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_SIMPLEVOLUMEALERT_KEY)
    private SimpleVolumePercentAlertItem simpleVolumePercentAlertItem;

    @SerializedName(API_ISNEW_ALERT_PARAM)
    private boolean isNewAlert;

    @DynamoDBIgnore
    public boolean isNewAlert() {
        return isNewAlert;
    }

    public static AlertDataItemBuilder builder(String ticker, String userId, String watchListId) {
        return new AlertDataItemBuilder()
                .ticker(ticker)
                .userWatchlistId(new UserWatchlistId(userId, watchListId));
    }

    /*
     * Method to convert to Json for sendig as API response
     */
    @DynamoDBIgnore
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @DynamoDBIgnore
    private boolean isAlertEmpty(IAlertDBItem iAlertDBItem) {
        return iAlertDBItem == null || iAlertDBItem.isEmpty();
    }

    @DynamoDBIgnore
    @Override
    public boolean isEmpty() {
        return isAlertEmpty(simplePriceAlertItem)
                && isAlertEmpty(simpleDailyPercentAlertItem)
                && isAlertEmpty(simpleVolumePercentAlertItem)
                && isAlertEmpty(netPercentChangeAlertItem);
    }

    @DynamoDBIgnore
    @Override
    public Long getTriggerTime() {
        return null;
    }

    @DynamoDBIgnore
    @Override
    public String getDBKeyName() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlertDataItem that = (AlertDataItem) o;
        return ticker.equals(that.ticker) &&
                userWatchlistId.equals(that.userWatchlistId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticker, userWatchlistId);
    }

    @Override
    public String toString() {
        return "AlertDataItem{" +
                "ticker='" + ticker + '\'' +
                ", userWatchlistId=" + userWatchlistId +
                '}';
    }

}
