package com.paytar2800.stockalertcommons.ddb.userearnings.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.paytar2800.stockalertcommons.ddb.userearnings.UserEarningsDDBConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_CUSTOM_TICKERS_PARAM;
import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_HIDDEN_TICKERS_PARAM;
import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_LAST_UPDATE_TIME_PARAM;
import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_USER_ID_PARAM;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = UserEarningsDDBConstants.TABLE_NAME)
public class UserEarningsDataItem {
    @SerializedName(API_USER_ID_PARAM)
    @DynamoDBHashKey(attributeName = UserEarningsDDBConstants.TABLE_USER_ID_KEY)
    private String userId;

    @SerializedName(API_CUSTOM_TICKERS_PARAM)
    @DynamoDBAttribute(attributeName = UserEarningsDDBConstants.KEY_CUSTOM_TICKERS)
    private List<String> customTickers;

    @SerializedName(API_HIDDEN_TICKERS_PARAM)
    @DynamoDBAttribute(attributeName = UserEarningsDDBConstants.KEY_HIDDEN_TICKERS)
    private List<String> hiddenTickers;

    @SerializedName(API_LAST_UPDATE_TIME_PARAM)
    @DynamoDBAttribute(attributeName = UserEarningsDDBConstants.KEY_LAST_UPDATE_TIME)
    private Long lastUpdateTime;

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
