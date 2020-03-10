package com.paytar2800.stockalertcommons.ddb.alert;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTransactionWriteExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.TransactionWriteRequest;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.TransactionCanceledException;
import com.paytar2800.stockalertcommons.ddb.CustomDynamoDBMapper;
import com.paytar2800.stockalertcommons.ddb.alert.model.AlertDataItem;
import com.paytar2800.stockalertcommons.ddb.stock.model.StockDataItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior.APPEND_SET;
import static com.amazonaws.services.dynamodbv2.model.Select.COUNT;
import static com.paytar2800.stockalertcommons.ddb.stock.StockDDBConstants.TABLE_ALERT_COUNT_KEY;

class AlertDDBUtils {

    static TransactionWriteRequest getRequestForAlertDeleteAndStockCountReduction(AlertDataItem alertDataItem,
                                                                                  long countOfAlert,
                                                                                  boolean shouldAddConditionToFailIfCountIsZero) {
        TransactionWriteRequest transactionWriteRequest = new TransactionWriteRequest();

        //Check first if alert item exists or not , if it does not exists then fail the transaction
        DynamoDBTransactionWriteExpression deleteConditionForAlert = new DynamoDBTransactionWriteExpression()
                .withConditionExpression("attribute_exists(" + AlertDDBConstants.ALERT_TICKER_KEY + ")");
        transactionWriteRequest.addDelete(alertDataItem, deleteConditionForAlert);


        //update the Stock's alert count in the Stock Data table.
        StockDataItem stockDataItem = StockDataItem.builder(alertDataItem.getTicker())
                .alertCount(countOfAlert).build();

        if (shouldAddConditionToFailIfCountIsZero) {
            Map<String, AttributeValue> attributeValueMap = new HashMap<>();
            attributeValueMap.put(":countVal", new AttributeValue().withN("1"));

            DynamoDBTransactionWriteExpression updateCondition = new DynamoDBTransactionWriteExpression()
                    .withConditionExpression(String.format("%s <> %s", TABLE_ALERT_COUNT_KEY, ":countVal"))
                    .withExpressionAttributeValues(attributeValueMap);

            transactionWriteRequest.addUpdate(stockDataItem, updateCondition);
        } else {
            transactionWriteRequest.addUpdate(stockDataItem);
        }

        return transactionWriteRequest;
    }

    static DynamoDBMapperConfig getDynamoDBMapperConfigForPartialUpdate() {
        return DynamoDBMapperConfig.builder()
                .withSaveBehavior(APPEND_SET)
                .build();
    }

    static boolean checkForConditionFailureAndIfCanDeleteStock(
            CustomDynamoDBMapper customDynamoDBMapper, AlertDataItem alertDataItem, TransactionCanceledException e) {

        return e.getCancellationReasons().size() > 1
                && e.getCancellationReasons().get(1).getCode().equals("ConditionalCheckFailed")
                && shouldDeleteStock(customDynamoDBMapper, alertDataItem.getTicker(), 1L);
    }

    private static boolean shouldDeleteStock(CustomDynamoDBMapper customDynamoDBMapper, String ticker, Long deleteCount) {
        StockDataItem tickerItem = new StockDataItem(ticker);
        StockDataItem dataItem = customDynamoDBMapper.load(tickerItem);
        return (dataItem.getAlertCount().equals(deleteCount));
    }

    static int getActualCountOfAlert(CustomDynamoDBMapper customDynamoDBMapper, String ticker) {
        Map<String, AttributeValue> eav = new HashMap<>();
        String secondaryKey = ":val1";
        eav.put(secondaryKey, new AttributeValue().withS(ticker));

        DynamoDBQueryExpression<AlertDataItem> queryExpression =
                new DynamoDBQueryExpression<AlertDataItem>()
                        .withKeyConditionExpression(
                                AlertDDBConstants.ALERT_TICKER_KEY + " = " + secondaryKey)
                        .withExpressionAttributeValues(eav)
                        .withSelect(COUNT)
                        .withConsistentRead(false);

        return customDynamoDBMapper.count(AlertDataItem.class, queryExpression);
    }

    static List<StockDataItem> getStockItemsForWhichCountReduced(List<AlertDataItem> deleteList) {
        List<StockDataItem> stockDataItemList = new ArrayList<>();

        for (AlertDataItem alertDataItem : deleteList) {
            stockDataItemList.add(new StockDataItem(alertDataItem.getTicker()));
        }

        return stockDataItemList;
    }

    static TransactionWriteRequest getTransactionWriteRequestForBatchAlertDelete(List<AlertDataItem> alertList) {
        TransactionWriteRequest request = new TransactionWriteRequest();

        for (AlertDataItem alertDataItem : alertList) {
            TransactionWriteRequest requestForSingleAlert = getRequestForAlertDeleteAndStockCountReduction(
                    alertDataItem, 1L, false);
            request.getTransactionWriteOperations().addAll(requestForSingleAlert.getTransactionWriteOperations());
        }

        return request;
    }
}
