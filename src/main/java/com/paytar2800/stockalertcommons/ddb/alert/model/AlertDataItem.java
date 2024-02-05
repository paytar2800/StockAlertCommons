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

import static com.paytar2800.stockalertcommons.api.APIParamConstants.*;


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

    @SerializedName(API_RECUR_PRICEALERT_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_RECURRING_PRICEALERT_KEY)
    private RecurringPriceAlertItem recurringPriceAlertItem;

    @SerializedName(API_SIMPLEDAILYPERCENTALERT_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_SIMPLEDAILYPERCENTALERT_KEY)
    private SimpleDailyPercentAlertItem simpleDailyPercentAlertItem;

    @SerializedName(API_NETPERCENTCHANGEALERT_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_NETPERCENTCHANGEALERT_KEY)
    private NetPercentChangeAlertItem netPercentChangeAlertItem;

    @SerializedName(API_SIMPLEVOLUMEALERT_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_SIMPLEVOLUMEALERT_KEY)
    private SimpleVolumePercentAlertItem simpleVolumePercentAlertItem;

    @SerializedName(API_EARNING_ALERT_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_EARNINGS_ALERT_KEY)
    private EarningsAlertItem earningsAlertItem;

    @SerializedName(API_DIVIDEND_ALERT_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_DIVIDEND_ALERT_KEY)
    private DividendAlertItem dividendAlertItem;

    @SerializedName(API_FIFTY_DAY_AVG_ALERT_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_FIFTYDAYAVG_ALERT_KEY)
    private FiftyDayAvgAlertItem fiftyDayAvgAlertItem;

    @SerializedName(API_TWO_HUNDRED_DAY_AVG_ALERT_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_TWOHUNDREDDAYAVG_ALERT_KEY)
    private TwoHundredDayAvgAlertItem twoHundredDayAvgAlertItem;

    @SerializedName(API_PEG_RATIO_ALERT_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_PEGRATIO_ALERT_KEY)
    private PegRatioAlertItem pegRatioAlertItem;

    @SerializedName(API_SHORT_PERCENT_FLOAT_ALERT_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_SHORTPERCENTFLOAT_ALERT_KEY)
    private ShortPercentFloatAlertItem shortPercentFloatAlertItem;

    @SerializedName(API_SHORT_RATIO_ALERT_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_SHORTRATIO_ALERT_KEY)
    private ShortRatioAlertItem shortRatioAlertItem;

    @SerializedName(API_FORWARD_PE_ALERT_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_FORWARD_PE_ALERT_KEY)
    private ForwardPEAlertItem forwardPEAlertItem;

    @SerializedName(API_TRAILING_PE_ALERT_PARAM)
    @DynamoDBAttribute(attributeName = AlertDDBConstants.ALERT_TRAILING_PE_ALERT_KEY)
    private TrailingPEAlertItem trailingPEAlertItem;

    @SerializedName(API_ISNEW_ALERT_PARAM)
    private boolean isNewAlert;

    @DynamoDBIgnore
    public boolean isNewAlert() {
        return isNewAlert;
    }

    @SerializedName(API_EXCHANGE_PARAM)
    @DynamoDBIgnore
    public String exchange;

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

   /* @Expose(serialize = false, deserialize = false)
    @DynamoDBIndexHashKey(attributeName = AlertDDBConstants.TABLE_HAS_CHANGED_KEY,
            globalSecondaryIndexName = AlertDDBConstants.TABLE_HAS_CHANGED_GSI_KEY)
    private Boolean hasChanged;*/

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
