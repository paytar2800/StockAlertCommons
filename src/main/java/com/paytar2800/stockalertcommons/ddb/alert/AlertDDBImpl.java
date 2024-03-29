package com.paytar2800.stockalertcommons.ddb.alert;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ConditionalOperator;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.amazonaws.services.dynamodbv2.model.TransactionCanceledException;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.paytar2800.stockalertcommons.StockUpdatePriority;
import com.paytar2800.stockalertcommons.ddb.CustomDynamoDBMapper;
import com.paytar2800.stockalertcommons.ddb.NextTokenSerializer;
import com.paytar2800.stockalertcommons.ddb.PaginatedItem;
import com.paytar2800.stockalertcommons.ddb.alert.model.AlertDataItem;
import com.paytar2800.stockalertcommons.ddb.alert.model.AlertDataItem_DeletedData;
import com.paytar2800.stockalertcommons.ddb.alert.model.IAlertDBItem;
import com.paytar2800.stockalertcommons.ddb.alert.model.UserWatchlistId;
import com.paytar2800.stockalertcommons.ddb.stock.model.StockDataItem;
import com.paytar2800.stockalertcommons.ddb.user.model.UserDataItem;
import com.paytar2800.stockalertcommons.exceptions.DDBException;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.paytar2800.stockalertcommons.ddb.stock.StockDDBConstants.STOCK_TABLE_NAME;
import static com.paytar2800.stockalertcommons.ddb.stock.StockDDBConstants.TABLE_ALERT_COUNT_KEY;
import static com.paytar2800.stockalertcommons.ddb.stock.StockDDBConstants.TABLE_STOCK_EXCHANGE_DEFAULT_VALUE;

public class AlertDDBImpl implements AlertDAO {

    private static final Logger logger = LogManager.getLogger(AlertDDBImpl.class);

    private static final int MAX_ITEMS_ALLOWED_PER_BATCH = 12;

    private CustomDynamoDBMapper customDynamoDBMapper;

    public AlertDDBImpl(CustomDynamoDBMapper customDynamoDBMapper) {
        this.customDynamoDBMapper = customDynamoDBMapper;
    }

    @Override
    public PaginatedItem<AlertDataItem, String> getAlertsForTicker(
            String ticker, String nextPageToken, Integer maxItemsPerPage) {

        Map<String, AttributeValue> eav = new HashMap<>();
        String partitonKey = ":val1";
        eav.put(partitonKey, new AttributeValue().withS(ticker));

        DynamoDBQueryExpression<AlertDataItem> queryExpression =
                new DynamoDBQueryExpression<AlertDataItem>()
                        .withKeyConditionExpression(
                                AlertDDBConstants.ALERT_TICKER_KEY + " = " + partitonKey)
                        .withExpressionAttributeValues(eav)
                        .withExclusiveStartKey(unserializePaginationToken(nextPageToken))
                        .withLimit(maxItemsPerPage);

        QueryResultPage<AlertDataItem> queryResultPage = customDynamoDBMapper.queryPage(
                AlertDataItem.class, queryExpression);

        String nextToken = serializePaginationToken(queryResultPage.getLastEvaluatedKey());

        List<AlertDataItem> results = queryResultPage.getResults();

        return new PaginatedItem<>(results, nextToken);
    }

    @Override
    public void updateAlertTriggerTimeOnly(AlertDataItem alertDataItem) {
        try {
            updateAlertItemTriggerTime(alertDataItem);
        }catch (ConditionalCheckFailedException e){
            //ignore this since alert does not exists as user might have deleted it.
        }
    }

    private String serializePaginationToken(Map<String, AttributeValue> lastKeyMap) {
        NextTokenSerializer nextTokenSerializer = NextTokenSerializer.getInstance();
        return nextTokenSerializer.serializeLastEvaluatedKey(lastKeyMap);
    }

    private Map<String, AttributeValue> unserializePaginationToken(String token) {
        NextTokenSerializer nextTokenSerializer = NextTokenSerializer.getInstance();
        return nextTokenSerializer.deserializeExclusiveStartKey(token);
    }

    public void updateAlertItemTriggerTime(AlertDataItem alertDataItem) {

        UpdateItemRequest request = new UpdateItemRequest();

        request.setTableName(AlertDDBConstants.ALERT_TABLE_NAME);

        Map<String, AttributeValue> map = new HashMap<>();
        map.put(AlertDDBConstants.ALERT_TICKER_KEY, new AttributeValue(alertDataItem.getTicker()));
        map.put(AlertDDBConstants.ALERT_USERWATCHLISTID_KEY, new AttributeValue(alertDataItem.getUserWatchlistId().toString()));
        request.setKey(map);

        StringBuilder updateExpressionBuilder = new StringBuilder();

        Map<String, AttributeValue> attributeValues = new HashMap<>();

        appendAlertTrigger(alertDataItem.getSimplePriceAlertItem(), updateExpressionBuilder, attributeValues);
        appendAlertTrigger(alertDataItem.getSimpleDailyPercentAlertItem(), updateExpressionBuilder, attributeValues);
        appendAlertTrigger(alertDataItem.getNetPercentChangeAlertItem(), updateExpressionBuilder, attributeValues);
        appendAlertTrigger(alertDataItem.getSimpleVolumePercentAlertItem(), updateExpressionBuilder, attributeValues);

        if (!updateExpressionBuilder.toString().isEmpty()) {
            String updateExpression = "SET " + updateExpressionBuilder.toString();
            request.setUpdateExpression(updateExpression);
        }

        request.setExpressionAttributeValues(attributeValues);
        request.setConditionExpression("attribute_exists(" + AlertDDBConstants.ALERT_TICKER_KEY + ") AND " +
                "attribute_exists(" + AlertDDBConstants.ALERT_USERWATCHLISTID_KEY + ")");

        customDynamoDBMapper.updateDBItem(request);
    }

    private void appendAlertTrigger(IAlertDBItem alertDBItem, @NonNull StringBuilder updateExpressionBuilder,
                                    Map<String, AttributeValue> attributeUpdateValues) {

        if (alertDBItem == null || alertDBItem.getTriggerTime() == null) {
            return;
        }

        String DOT_SEPARATOR = ".";
        String EQUALS_STR = " = ";
        String COMMA_SEPARATOR = ",";
        String COLON_STR = ":";

        String attributeValueKey = COLON_STR + alertDBItem.getDBKeyName();

        if(!updateExpressionBuilder.toString().isEmpty()){
            updateExpressionBuilder.append(COMMA_SEPARATOR);
        }

        updateExpressionBuilder.append(alertDBItem.getDBKeyName())
                .append(DOT_SEPARATOR)
                .append(alertDBItem.getTriggerTimeDBKeyName())
                .append(EQUALS_STR)
                .append(attributeValueKey);

        attributeUpdateValues.put(attributeValueKey,
                new AttributeValue().withN(String.valueOf(alertDBItem.getTriggerTime())));
    }


    @Override
    public Optional<AlertDataItem> getAlert(AlertDataItem alertDataItem) {
        return Optional.ofNullable(customDynamoDBMapper.load(alertDataItem));
    }

    /**
     * Use this method to only put new alerts as this does transaction to Stock table for alert count as well
     * First we put the alert item on the condition that the item is not already present in the table
     * attribute_not_exists works well just with hash key as well, no need to give range key.
     * If it is present than fail the whole transaction.
     * If it is not present than insert and next update the alert count.
     * AlertCountUpdate requests just increments the counter by using expression.
     */
    @Override
    public void putNewAlert(AlertDataItem alertDataItem, boolean isNewAlert) {
        if (isNewAlert) {
            TransactionWriteRequest transactionWriteRequest = new TransactionWriteRequest();

            //Check first if alert item exists or not , if it exists then fail the transaction
            DynamoDBTransactionWriteExpression putConditionForAlert = new DynamoDBTransactionWriteExpression()
                    .withConditionExpression("attribute_not_exists(" + AlertDDBConstants.ALERT_TICKER_KEY + ")");
            transactionWriteRequest.addPut(alertDataItem, putConditionForAlert);

            //update the Stock's alert count in the Stock Data table.
            StockDataItem stockDataItem = StockDataItem.builder(alertDataItem.getTicker())
                    .alertCount(1L).priority(StockUpdatePriority.P1.name())
                    .exchange(alertDataItem.getExchange() == null ?
                            TABLE_STOCK_EXCHANGE_DEFAULT_VALUE : alertDataItem.getExchange())
                    .build();

            transactionWriteRequest.addUpdate(stockDataItem);

            DynamoDBMapperConfig dynamoDBMapperConfig = AlertDDBUtils.getDynamoDBMapperConfigForPartialUpdate();

            try {
                customDynamoDBMapper.transactionWriteForAlertAndStockTable(transactionWriteRequest, dynamoDBMapperConfig
                        , true);
            } catch (TransactionCanceledException e) {
                logger.error(String.format("putNewAlert Transaction failed for = %s,%s due to reason = %s"
                        , alertDataItem.getTicker(), alertDataItem.getUserWatchlistId(), e.getCancellationReasons()));
            }
        } else {
            updateAlert(alertDataItem);
        }
    }

    @Override
    public void updateAlert(AlertDataItem alertDataItem) {
        DynamoDBMapperConfig dynamoDBMapperConfig = AlertDDBUtils.getDynamoDBMapperConfigForPartialUpdate();

        DynamoDBSaveExpression saveExpression = new DynamoDBSaveExpression();
        Map<String, ExpectedAttributeValue> expectedAttributes = new HashMap<>();
        expectedAttributes.put(AlertDDBConstants.ALERT_TICKER_KEY,
                new ExpectedAttributeValue(new AttributeValue(alertDataItem.getTicker())));

        expectedAttributes.put(AlertDDBConstants.ALERT_USERWATCHLISTID_KEY,
                new ExpectedAttributeValue(new AttributeValue(alertDataItem.getUserWatchlistId().toString())));

        saveExpression.setExpected(expectedAttributes);
        saveExpression.setConditionalOperator(ConditionalOperator.AND);

        try {
            customDynamoDBMapper.save(alertDataItem, saveExpression, dynamoDBMapperConfig);
        } catch (ConditionalCheckFailedException e) {
            logger.error("Alert update failed because alert not exists for " + alertDataItem.toString());
            putNewAlert(alertDataItem, true);
        }
    }

    @Override
    public void updateBatchAlerts(List<AlertDataItem> alertDataItemList) {
        for (int i = 0; i < alertDataItemList.size(); i += MAX_ITEMS_ALLOWED_PER_BATCH) {
            int end = Math.min(i + MAX_ITEMS_ALLOWED_PER_BATCH, alertDataItemList.size());
            List<AlertDataItem> batchItems = new ArrayList<>(alertDataItemList.subList(i, end));
            List<DynamoDBMapper.FailedBatch> failedBatches = customDynamoDBMapper.batchWrite(batchItems,
                    new ArrayList<>(), AlertDDBUtils.getDynamoDBMapperConfigForPartialUpdate());
        }
    }

    @Override
    public void updateStock(StockDataItem stockDataItem) {
        DynamoDBMapperConfig dynamoDBMapperConfig = AlertDDBUtils.getDynamoDBMapperConfigForPartialUpdate();
        customDynamoDBMapper.save(stockDataItem, dynamoDBMapperConfig);
    }

    //TODO remove transactions for delete alert and delete stock and instead have separate tasks
    /*
     * Use this to delete alert : transaction write will be done to reduce count in stock table for deleted stocks
     */
    @Override
    public void deleteAlert(AlertDataItem alertDataItem, boolean isRetry) {

        TransactionWriteRequest transactionWriteRequest = AlertDDBUtils.getRequestForAlertDeleteAndStockCountReduction(
                alertDataItem, 1, true);

        try {
            DynamoDBMapperConfig dynamoDBMapperConfig = AlertDDBUtils.getDynamoDBMapperConfigForPartialUpdate();

            customDynamoDBMapper.transactionWriteForAlertAndStockTable(
                    transactionWriteRequest, dynamoDBMapperConfig, false);

        } catch (TransactionCanceledException e) {

            if (AlertDDBUtils.checkForConditionFailureAndIfCanDeleteStock(customDynamoDBMapper, alertDataItem, e)) {

                int actualCount = AlertDDBUtils.getActualCountOfAlert(customDynamoDBMapper, alertDataItem.getTicker());

                if (actualCount == 1L) {
                    executeDeleteForAlertAndStock(alertDataItem);
                } else if (!isRetry) {
                    updateStock(StockDataItem.builder(alertDataItem.getTicker()).alertCount((long) actualCount).build());
                    deleteAlert(alertDataItem, true);
                }
            } else {
                logger.error(String.format("deleteAlert Transaction failed for = %s,%s due to reason = %s"
                        , alertDataItem.getTicker(), alertDataItem.getUserWatchlistId(), e.getCancellationReasons()));
            }
        }
    }

    //TODO remove transactions for delete alert and delete stock and instead have separate tasks
    private void executeDeleteForAlertAndStock(AlertDataItem alertDataItem) {
        TransactionWriteRequest transactionWriteRequest = new TransactionWriteRequest();

        //Check first if alert item exists or not , if it exists then fail the transaction
        DynamoDBTransactionWriteExpression deleteConditionForAlert = new DynamoDBTransactionWriteExpression()
                .withConditionExpression("attribute_exists(" + AlertDDBConstants.ALERT_TICKER_KEY + ")");
        transactionWriteRequest.addDelete(alertDataItem, deleteConditionForAlert);

        //Delete this item since count is zero
        StockDataItem stockDataItem = new StockDataItem(alertDataItem.getTicker());

        Map<String, AttributeValue> attributeValueMap = new HashMap<>();
        attributeValueMap.put(":countVal", new AttributeValue().withN("1"));

        DynamoDBTransactionWriteExpression deleteCondition = new DynamoDBTransactionWriteExpression()
                .withConditionExpression(String.format("%s = %s", TABLE_ALERT_COUNT_KEY, ":countVal"))
                .withExpressionAttributeValues(attributeValueMap);

        transactionWriteRequest.addDelete(stockDataItem, deleteCondition);

        try {
            customDynamoDBMapper.transactionWrite(transactionWriteRequest);
        } catch (TransactionCanceledException e) {
            logger.error(String.format("executeDeleteForAlertAndStock deletion failed for = %s,%s due to reason = %s"
                    , alertDataItem.getTicker(), alertDataItem.getUserWatchlistId(), e.getCancellationReasons()));
            throw e;
        }
    }

    /*
     * Use this to delete Watchlist : transaction write will be done to reduce count in stock table for deleted stocks
     * Later we will query the stock table to get the stocks and check for zero count stocks and delete those
     */
    @Override
    public void deleteWatchlist(UserWatchlistId userWatchlistId) {
        Map<String, AttributeValue> eav = new HashMap<>();
        String secondaryKey = ":val1";
        eav.put(secondaryKey, new AttributeValue().withS(userWatchlistId.toString()));

        DynamoDBQueryExpression<AlertDataItem> queryExpression =
                new DynamoDBQueryExpression<AlertDataItem>()
                        .withKeyConditionExpression(
                                AlertDDBConstants.ALERT_USERWATCHLISTID_KEY + " = " + secondaryKey)
                        .withExpressionAttributeValues(eav)
                        .withIndexName(AlertDDBConstants.ALERT_USERWATCHLIST_GSI_KEY)
                        .withProjectionExpression(
                                String.format("%s,%s", AlertDDBConstants.ALERT_TICKER_KEY, AlertDDBConstants.ALERT_USERWATCHLISTID_KEY))
                        .withConsistentRead(false);

        PaginatedQueryList<AlertDataItem> list = customDynamoDBMapper.query(AlertDataItem.class, queryExpression);

        if (list != null && !list.isEmpty()) {
            list.loadAllResults();

            List<AlertDataItem> deleteList = new ArrayList<>(list);

            List<AlertDataItem> batchDeleteList = new ArrayList<>();

            for (int i = 0; i < deleteList.size(); i++) {

                batchDeleteList.add(deleteList.get(i));

                //Either we are at the end of the list or the count is equal to max allowed fetch
                if (i == deleteList.size() - 1 || batchDeleteList.size() >= MAX_ITEMS_ALLOWED_PER_BATCH) {

                    performAlertDeletionTasks(batchDeleteList);

                    batchDeleteList.clear();
                }
            }
        }
    }

    private void performAlertDeletionTasks(List<AlertDataItem> deleteList) {
        deleteAlertItemsAndReduceCountInStockTable(deleteList);
        List<StockDataItem> stockDataItemList = AlertDDBUtils.getStockItemsForWhichCountReduced(deleteList);
        loadStockItemsAndDeleteStockWithZeroAlertCount(stockDataItemList);
    }

    //TODO remove transactions for delete alert and reduce count and instead have separate tasks
    private void deleteAlertItemsAndReduceCountInStockTable(List<AlertDataItem> deleteList) {
        TransactionWriteRequest transactionWriteRequest =
                AlertDDBUtils.getTransactionWriteRequestForBatchAlertDelete(deleteList);

        DynamoDBMapperConfig dynamoDBMapperConfig =
                AlertDDBUtils.getDynamoDBMapperConfigForPartialUpdate();

        customDynamoDBMapper.transactionWriteForAlertAndStockTable(
                transactionWriteRequest, dynamoDBMapperConfig, false);
    }

    /*
     * This will first load all the stock items for which count reduced.
     * Then we will iterate to find stocks for which count is zero
     * Finally we will delete those using batch delete request.
     */
    private void loadStockItemsAndDeleteStockWithZeroAlertCount(List<StockDataItem> stockDataItemList) {

        //TODO Load items partially instead of full stock item

        Map<String, List<Object>> dataItemMap = customDynamoDBMapper.batchLoad(stockDataItemList);

        List<Object> dataItemList = dataItemMap.get(STOCK_TABLE_NAME);

        if (dataItemList == null || dataItemList.isEmpty()) {
            throw new DDBException("dataItem list is empty");
        }

        List<StockDataItem> deleteStockList = new ArrayList<>();

        for (Object item : dataItemList) {
            if (item instanceof StockDataItem) {
                StockDataItem data = (StockDataItem) item;
                if (data.getAlertCount() == 0L) {
                    long actualCount = AlertDDBUtils.getActualCountOfAlert(customDynamoDBMapper, data.getTicker());
                    if (actualCount == 0) {
                        deleteStockList.add(new StockDataItem(data.getTicker()));
                    } else {
                        updateStock(StockDataItem.builder(data.getTicker()).alertCount(actualCount).build());
                    }
                }
            }
        }

        if (!deleteStockList.isEmpty()) {
            List<DynamoDBMapper.FailedBatch> failedBatches = customDynamoDBMapper.batchDelete(deleteStockList);
            if (failedBatches != null && !failedBatches.isEmpty()) {
                throw new DDBException("Batch failed for " + failedBatches);
            }
        }
    }

    @Override
    public void deleteUser(String userId) {
        Map<String, AttributeValue> eav = new HashMap<>();
        String secondaryKey = ":val1";
        eav.put(secondaryKey, new AttributeValue().withS(userId + UserWatchlistId.getSeparator()));

        DynamoDBScanExpression queryExpression =
                new DynamoDBScanExpression()
                        .withFilterExpression(String.format("begins_with(%s , %s)",
                                AlertDDBConstants.ALERT_USERWATCHLISTID_KEY, secondaryKey))
                        .withExpressionAttributeValues(eav)
                        .withIndexName(AlertDDBConstants.ALERT_USERWATCHLIST_GSI_KEY)
                        .withProjectionExpression(AlertDDBConstants.ALERT_TICKER_KEY + "," + AlertDDBConstants.ALERT_USERWATCHLISTID_KEY)
                        .withConsistentRead(false);

        PaginatedScanList<AlertDataItem> paginatedScanList = customDynamoDBMapper.scan(AlertDataItem.class, queryExpression);

        if (!paginatedScanList.isEmpty()) {

            List<AlertDataItem> batchDeleteList = new ArrayList<>();

            for (AlertDataItem alertDataItem : paginatedScanList) {
                batchDeleteList.add(alertDataItem);
                //the count is equal to max allowed fetch
                if (batchDeleteList.size() >= MAX_ITEMS_ALLOWED_PER_BATCH) {
                    copyBatchAlertDataToDeletedDataTable(batchDeleteList);
                    performAlertDeletionForUser(batchDeleteList);
                    batchDeleteList.clear();
                }
            }

            //this means we are at the end of the list
            if(!batchDeleteList.isEmpty()) {
                copyBatchAlertDataToDeletedDataTable(batchDeleteList);
                performAlertDeletionForUser(batchDeleteList);
            }
        }
    }

    @Override
    public List<AlertDataItem> getAlertsForUser(String userId) {
        Map<String, AttributeValue> eav = new HashMap<>();
        String secondaryKey = ":val1";
        eav.put(secondaryKey, new AttributeValue().withS(userId + UserWatchlistId.getSeparator()));

        DynamoDBScanExpression queryExpression =
                new DynamoDBScanExpression()
                        .withFilterExpression(String.format("begins_with(%s , %s)",
                                AlertDDBConstants.ALERT_USERWATCHLISTID_KEY, secondaryKey))
                        .withExpressionAttributeValues(eav)
                        .withIndexName(AlertDDBConstants.ALERT_USERWATCHLIST_GSI_KEY)
                        .withProjectionExpression(AlertDDBConstants.ALERT_TICKER_KEY + "," + AlertDDBConstants.ALERT_USERWATCHLISTID_KEY)
                        .withConsistentRead(false);

        PaginatedScanList<AlertDataItem> paginatedScanList = customDynamoDBMapper.scan(AlertDataItem.class, queryExpression);

        List<AlertDataItem> allItems = new ArrayList<>();
        for (AlertDataItem item : paginatedScanList) {
            allItems.add(item);
        }

        return allItems;
    }

    /*
     * This method is different from deleteAlertItem because same stockitem can be repeated multiple times
     */
    private void performAlertDeletionForUser(List<AlertDataItem> alertList) {
        HashMap<String, Long> stockDataCountMap = new HashMap<>();

        for (AlertDataItem alertDataItem : alertList) {
            Long count = stockDataCountMap.get(alertDataItem.getTicker());
            if (count == null) {
                count = 1L;
            } else {
                count = count + 1L;
            }
            stockDataCountMap.put(alertDataItem.getTicker(), count);
        }

        List<DynamoDBMapper.FailedBatch> failedBatches = customDynamoDBMapper.batchDelete(alertList);

        if (failedBatches.isEmpty()) {
            updateStockDataCount(stockDataCountMap);
        }

        if (!failedBatches.isEmpty()) {
            throw new DDBException("User deletion failed for with failed batch = " + failedBatches);
        }
    }

    private void updateStockDataCount(HashMap<String, Long> stockDataCountMap) {
        List<StockDataItem> stockDataItemList = new ArrayList<>();

        TransactionWriteRequest transactionWriteRequest = new TransactionWriteRequest();

        for (Map.Entry<String, Long> entry : stockDataCountMap.entrySet()) {
            StockDataItem dataItem = StockDataItem.builder(entry.getKey()).alertCount(entry.getValue()).build();
            transactionWriteRequest.addUpdate(dataItem);
            stockDataItemList.add(dataItem);
        }

        DynamoDBMapperConfig dynamoDBMapperConfig = AlertDDBUtils.getDynamoDBMapperConfigForPartialUpdate();

        //Reduce the alert count for all the stocks using transaction since updateExpression is not available easily.
        //TODO Remove the transaction from here and write batchUpdate with update expression
        try {
            customDynamoDBMapper.transactionWriteForAlertAndStockTable(transactionWriteRequest, dynamoDBMapperConfig,
                    false);
        } catch (TransactionCanceledException e) {
            logger.error("error in deleting batch for reason = " + e.getCancellationReasons());
            throw new DDBException("User deltion Error in transaction for reason = " + e.getCancellationReasons());
        }

        loadStockItemsAndDeleteStockWithZeroAlertCount(stockDataItemList);
    }

    @Override
    public void copyBatchAlertDataToDeletedDataTable(List<AlertDataItem> alertDataItems) {
        List<AlertDataItem> loadedFullAlertItems = batchLoadAlertDataItems(alertDataItems);
        batchWriteAlertDataItems_deletedData(convertToDeletedDataItems(loadedFullAlertItems));
    }

    private List<AlertDataItem_DeletedData> convertToDeletedDataItems(List<AlertDataItem> loadedFullAlertItems) {
        List<AlertDataItem_DeletedData> items = new ArrayList<>();
        for(AlertDataItem alertDataItem : loadedFullAlertItems) {
            items.add(new AlertDataItem_DeletedData(alertDataItem));
        }

        return items;
    }

    public void batchWriteAlertDataItems_deletedData(List<AlertDataItem_DeletedData> alertDataItems) {
        // Divide the items into batches and write each batch
        for (int i = 0; i < alertDataItems.size(); i += MAX_ITEMS_ALLOWED_PER_BATCH) {
            int end = Math.min(i + MAX_ITEMS_ALLOWED_PER_BATCH, alertDataItems.size());
            List<AlertDataItem_DeletedData> batchItems = alertDataItems.subList(i, end);
            List<DynamoDBMapper.FailedBatch> failedBatch = customDynamoDBMapper.batchSave(batchItems);
        }
    }

    public List<AlertDataItem> batchLoadAlertDataItems(List<AlertDataItem> alertDataItemKeys) {
        List<AlertDataItem> alertDataItems = new ArrayList<>();

        // Define the batch load request
        Map<Class<?>, List<KeyPair>> batchLoadMap = new HashMap<>();
        List<KeyPair> alertDataKeys = new ArrayList<>();

        for (AlertDataItem alert : alertDataItemKeys) {
            alertDataKeys.add(new KeyPair().withHashKey(alert.getTicker()).withRangeKey(alert.getUserWatchlistId()));
        }

        batchLoadMap.put(AlertDataItem.class, alertDataKeys);
        Map<String, List<Object>> batchLoadResult = customDynamoDBMapper.batchLoad(batchLoadMap);

        // Process the batch load result
        List<Object> alertDataItemsFromDb = batchLoadResult.get(AlertDDBConstants.ALERT_TABLE_NAME);
        if (alertDataItemsFromDb != null) {
            for (Object item : alertDataItemsFromDb) {
                AlertDataItem alertDataItem = (AlertDataItem) item;
                alertDataItems.add(alertDataItem);
            }
        }

        return alertDataItems;
    }

}
