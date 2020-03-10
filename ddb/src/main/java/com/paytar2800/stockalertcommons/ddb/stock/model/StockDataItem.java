package com.paytar2800.stockalertcommons.ddb.stock.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.paytar2800.stockalertcommons.ddb.stock.StockDDBConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Stock Table to store tickers used in the backend engine for checking alerts
 * Alert count will be updated on update in Alert table.
 * Update priority will be updated on how regular we have to check prices
 * Primary Key = Ticker Secondary Key  = UpdatePriority
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = StockDDBConstants.STOCK_TABLE_NAME)
public class StockDataItem {

    @DynamoDBHashKey(attributeName = StockDDBConstants.TABLE_TICKER_KEY)
    private String ticker;

    @DynamoDBAttribute(attributeName = StockDDBConstants.TABLE_ALERT_COUNT_KEY)
    private Long alertCount;

    @DynamoDBIndexHashKey(attributeName = StockDDBConstants.TABLE_UPDATE_PRIORITY_KEY,
            globalSecondaryIndexName = StockDDBConstants.TABLE_UPDATE_PRIORITY_GSI_KEY)
    @DynamoDBAttribute(attributeName = StockDDBConstants.TABLE_UPDATE_PRIORITY_KEY)
    private String priority;

    public StockDataItem(String ticker) {
        this.ticker = ticker;
    }

    public static StockDataItemBuilder builder(String ticker) {
        return new StockDataItemBuilder().ticker(ticker);
    }

}
