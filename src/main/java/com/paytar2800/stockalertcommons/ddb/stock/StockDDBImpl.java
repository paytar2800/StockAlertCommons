package com.paytar2800.stockalertcommons.ddb.stock;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.paytar2800.stockalertcommons.ddb.NextTokenSerializer;
import com.paytar2800.stockalertcommons.ddb.PaginatedItem;
import com.paytar2800.stockalertcommons.ddb.stock.model.StockDataItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES;

public class StockDDBImpl implements StockDAO {

    private DynamoDBMapper dynamoDBMapper;

    public StockDDBImpl(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    @Override
    public PaginatedItem<String, String> getTickersForPriority(
            String priority, String nextPageToken, Integer maxItemsPerPage) {

        Map<String, AttributeValue> eav = new HashMap<>();
        String partitonKey = ":val1";
        eav.put(partitonKey, new AttributeValue().withS(priority));

        DynamoDBQueryExpression<StockDataItem> queryExpression =
                new DynamoDBQueryExpression<StockDataItem>()
                        .withKeyConditionExpression(
                                StockDDBConstants.TABLE_UPDATE_PRIORITY_KEY + " = " + partitonKey)
                        .withExpressionAttributeValues(eav)
                        .withIndexName(StockDDBConstants.TABLE_UPDATE_PRIORITY_GSI_KEY)
                        .withProjectionExpression(StockDDBConstants.TABLE_TICKER_KEY)
                        .withConsistentRead(false)
                        .withExclusiveStartKey(unserializePaginationToken(nextPageToken))
                        .withLimit(maxItemsPerPage);

        QueryResultPage<StockDataItem> queryResultPage = dynamoDBMapper.queryPage(
                StockDataItem.class, queryExpression);

        String nextToken = serializePaginationToken(queryResultPage.getLastEvaluatedKey());

        List<StockDataItem> results = queryResultPage.getResults();

        List<String> tickerList = new ArrayList<>();

        results.forEach(stockDataItem -> tickerList.add(stockDataItem.getTicker()));

        return new PaginatedItem<>(tickerList, nextToken);
    }

    @Override
    public List<StockDataItem> getStockItemsForPriority(String priority, String exchange) {

        StockDataItem stockDataItem = new StockDataItem();
        stockDataItem.setPriority(priority);
        stockDataItem.setExchange(exchange);

        DynamoDBQueryExpression<StockDataItem> queryExpression =
                new DynamoDBQueryExpression<StockDataItem>()
                        .withHashKeyValues(stockDataItem)
                        .withIndexName(StockDDBConstants.TABLE_UPDATE_PRIORITY_GSI_KEY)
                        .withProjectionExpression(StockDDBConstants.TABLE_TICKER_KEY)
                        .withConsistentRead(false);

        PaginatedQueryList<StockDataItem> paginatedQueryList = dynamoDBMapper.query(StockDataItem.class, queryExpression);

        List<StockDataItem> items = new ArrayList<>();

        if (paginatedQueryList != null && !paginatedQueryList.isEmpty()) {
            paginatedQueryList.loadAllResults();
            items.addAll(paginatedQueryList);
        }

        return items;
    }

    @Override
    public void updateStock(StockDataItem dataItem) {
        DynamoDBMapperConfig dynamoDBMapperConfig = DynamoDBMapperConfig.builder()
                .withSaveBehavior(UPDATE_SKIP_NULL_ATTRIBUTES)
                .build();
        dynamoDBMapper.save(dataItem, dynamoDBMapperConfig);
    }

    private String serializePaginationToken(Map<String, AttributeValue> lastKeyMap) {
        NextTokenSerializer nextTokenSerializer = NextTokenSerializer.getInstance();
        return nextTokenSerializer.serializeLastEvaluatedKey(lastKeyMap);
    }

    private Map<String, AttributeValue> unserializePaginationToken(String token) {
        NextTokenSerializer nextTokenSerializer = NextTokenSerializer.getInstance();
        return nextTokenSerializer.deserializeExclusiveStartKey(token);
    }
}
