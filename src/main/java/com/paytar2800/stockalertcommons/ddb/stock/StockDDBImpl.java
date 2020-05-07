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
    public PaginatedItem<StockDataItem, String> getStockDataItemsForPriority(String priority,
                                                                             List<String> projectionAttributes,
                                                                             String nextPageToken, Integer maxItemsPerPage) {

        Map<String, AttributeValue> eav = new HashMap<>();
        String partitonKey = ":val1";
        eav.put(partitonKey, new AttributeValue().withS(priority));

        StringBuilder projectionExpression = new StringBuilder();

        for (int i = 0; i < projectionAttributes.size(); i++) {
            projectionExpression.append(projectionAttributes.get(i));
            if (i != projectionAttributes.size() - 1) {
                projectionExpression.append(",");
            }
        }

        DynamoDBQueryExpression<StockDataItem> queryExpression =
                new DynamoDBQueryExpression<StockDataItem>()
                        .withKeyConditionExpression(
                                StockDDBConstants.TABLE_UPDATE_PRIORITY_KEY + " = " + partitonKey)
                        .withExpressionAttributeValues(eav)
                        .withIndexName(StockDDBConstants.TABLE_UPDATE_PRIORITY_GSI_KEY)
                        .withProjectionExpression(projectionExpression.toString())
                        .withConsistentRead(false)
                        .withExclusiveStartKey(unserializePaginationToken(nextPageToken))
                        .withLimit(maxItemsPerPage);

        QueryResultPage<StockDataItem> queryResultPage = dynamoDBMapper.queryPage(
                StockDataItem.class, queryExpression);

        String nextToken = serializePaginationToken(queryResultPage.getLastEvaluatedKey());

        List<StockDataItem> results = queryResultPage.getResults();

        return new PaginatedItem<>(results, nextToken);
    }

    @Override
    public PaginatedItem<String, String> getTickersForPriority(
            String priority, String nextPageToken, Integer maxItemsPerPage) {

        List<String> attributeList = new ArrayList<>();
        attributeList.add(StockDDBConstants.TABLE_TICKER_KEY);

        PaginatedItem<StockDataItem, String> paginatedItem  = getStockDataItemsForPriority(priority,
                attributeList, nextPageToken, maxItemsPerPage);

        List<StockDataItem> results = paginatedItem.getCurrentItemList();

        String nextToken = paginatedItem.getToken();

        List<String> tickerList = new ArrayList<>();

        results.forEach(stockDataItem -> tickerList.add(stockDataItem.getTicker()));

        return new PaginatedItem<>(tickerList, nextToken);
    }


    @Override
    public List<StockDataItem> getStockItemsForPriority(String priority, String exchange) {
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":priority", new AttributeValue().withS(priority));
        eav.put(":exchange", new AttributeValue().withS(exchange));

        DynamoDBQueryExpression<StockDataItem> queryExpression =
                new DynamoDBQueryExpression<StockDataItem>()
                        .withKeyConditionExpression(StockDDBConstants.TABLE_UPDATE_PRIORITY_KEY + " = :priority and " +
                                StockDDBConstants.TABLE_STOCK_EXCHANGE_KEY + " = :exchange")
                        .withExpressionAttributeValues(eav)
                        .withIndexName(StockDDBConstants.TABLE_UPDATE_PRIORITY_GSI_KEY)
                        .withProjectionExpression(StockDDBConstants.TABLE_TICKER_KEY)
                        .withConsistentRead(false);

        PaginatedQueryList<StockDataItem> paginatedQueryList = dynamoDBMapper.query
                (StockDataItem.class, queryExpression);

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
