package com.paytar2800.stockalertcommons.ddb.watchlist.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.paytar2800.stockalertcommons.ddb.watchlist.WatchlistDDBConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_DISPLAY_ORDER_PARAM;
import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_LAST_UPDATE_TIME_PARAM;
import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_TICKERS_PARAM;
import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_USER_ID_PARAM;
import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_WATCHLIST_ID_PARAM;
import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_WATCHLIST_NAME_PARAM;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = WatchlistDDBConstants.TABLE_NAME)
public class WatchlistDataItem {

    @SerializedName(API_USER_ID_PARAM)
    @DynamoDBHashKey(attributeName = WatchlistDDBConstants.TABLE_USER_ID_KEY)
    @DynamoDBIndexHashKey(attributeName = WatchlistDDBConstants.TABLE_USER_ID_KEY,
            globalSecondaryIndexName = WatchlistDDBConstants.WATCHLIST_METADATA_GSI)
    private String userId;

    @SerializedName(API_WATCHLIST_ID_PARAM)
    @DynamoDBRangeKey(attributeName = WatchlistDDBConstants.TABLE_WATCHLIST_ID_KEY)
    @DynamoDBIndexRangeKey(attributeName = WatchlistDDBConstants.TABLE_WATCHLIST_ID_KEY,
            globalSecondaryIndexName = WatchlistDDBConstants.WATCHLIST_METADATA_GSI)
    private String watchlistId;

    @SerializedName(API_WATCHLIST_NAME_PARAM)
    @DynamoDBAttribute(attributeName = WatchlistDDBConstants.KEY_WATCHLIST_NAME)
    private String watchlistName;

    @SerializedName(API_DISPLAY_ORDER_PARAM)
    @DynamoDBAttribute(attributeName = WatchlistDDBConstants.KEY_DISPLAY_ORDER)
    private Integer displayOrder;

    @SerializedName(API_TICKERS_PARAM)
    @DynamoDBAttribute(attributeName = WatchlistDDBConstants.KEY_TICKERS)
    private List<String> tickers;

    @SerializedName(API_LAST_UPDATE_TIME_PARAM)
    @DynamoDBAttribute(attributeName = WatchlistDDBConstants.KEY_LAST_UPDATE_TIME)
    private Long lastUpdateTime;

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
