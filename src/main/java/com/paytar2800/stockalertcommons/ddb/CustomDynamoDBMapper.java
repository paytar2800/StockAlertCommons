package com.paytar2800.stockalertcommons.ddb;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.AttributeTransformer;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGenerateStrategy;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperTableModel;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMappingException;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTransactionWriteExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.TransactionWriteRequest;
import com.amazonaws.services.dynamodbv2.datamodeling.UpdateExpressionGenerator;
import com.amazonaws.services.dynamodbv2.datamodeling.VersionAttributeConditionExpressionGenerator;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.ConditionCheck;
import com.amazonaws.services.dynamodbv2.model.Delete;
import com.amazonaws.services.dynamodbv2.model.Put;
import com.amazonaws.services.dynamodbv2.model.ReturnValuesOnConditionCheckFailure;
import com.amazonaws.services.dynamodbv2.model.TransactWriteItem;
import com.amazonaws.services.dynamodbv2.model.TransactWriteItemsRequest;
import com.amazonaws.services.dynamodbv2.model.Update;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import com.paytar2800.stockalertcommons.ddb.stock.model.StockDataItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.paytar2800.stockalertcommons.ddb.stock.StockDDBConstants.TABLE_ALERT_COUNT_KEY;

/**
 * Custom DB Mapper class created to add updateExpression in the code for transactionWrite
 */
public class CustomDynamoDBMapper extends DynamoDBMapper {

    AmazonDynamoDB dynamoDB;

    public CustomDynamoDBMapper(AmazonDynamoDB dynamoDB) {
        super(dynamoDB);
        this.dynamoDB = dynamoDB;
    }

    private static <K, V> boolean isNullOrEmpty(Map<K, V> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Determines if the mapping value can be auto-generated.
     */
    private static <T> boolean canGenerate(
            final DynamoDBMapperTableModel<T> model,
            final T object,
            final DynamoDBMapperConfig.SaveBehavior saveBehavior,
            final DynamoDBMapperFieldModel<T, Object> field
    ) {
        if (field.getGenerateStrategy() == null) {
            return false;
        } else if (field.getGenerateStrategy() == DynamoDBAutoGenerateStrategy.ALWAYS) {
            return true;
        } else if (field.get(object) != null) {
            return false;
        } else if (field.keyType() != null || field.indexed()) {
            return true;
        } else if (saveBehavior == DynamoDBMapperConfig.SaveBehavior.CLOBBER
                || saveBehavior == DynamoDBMapperConfig.SaveBehavior.UPDATE
                || saveBehavior == DynamoDBMapperConfig.SaveBehavior.PUT) {
            return true;
        } else if (anyKeyGeneratable(model, object, saveBehavior)) {
            return true;
        }
        return false;
    }

    /**
     * Determnes if any of the primary keys require auto-generation.
     */
    private static <T> boolean anyKeyGeneratable(
            final DynamoDBMapperTableModel<T> model,
            final T object,
            final DynamoDBMapperConfig.SaveBehavior saveBehavior
    ) {
        for (final DynamoDBMapperFieldModel<T, Object> field : model.keys()) {
            if (canGenerate(model, object, saveBehavior, field)) {
                return true;
            }
        }
        return false;
    }

    public void updateDBItem(UpdateItemRequest request) {
        dynamoDB.updateItem(request);
    }

    /*
     * Transaction write method for adding new alert in the alert table and incrementing counter for stock alert count in stock table.
     */
    public void transactionWriteForAlertAndStockTable(TransactionWriteRequest transactionWriteRequest,
                                                      DynamoDBMapperConfig config, boolean shouldIncreaseAlertCount) {
        if (transactionWriteRequest == null) {
            throw new SdkClientException("Input request is null or empty");
        }

        final DynamoDBMapperConfig finalConfig = mergeConfig(config);

        List<TransactionWriteRequest.TransactionWriteOperation> writeOperations =
                transactionWriteRequest.getTransactionWriteOperations();
        List<ValueUpdate> inMemoryUpdates = new LinkedList<ValueUpdate>();
        TransactWriteItemsRequest transactWriteItemsRequest = new TransactWriteItemsRequest();
        List<TransactWriteItem> transactWriteItems = new ArrayList<TransactWriteItem>();

        transactWriteItemsRequest.setClientRequestToken(transactionWriteRequest.getIdempotencyToken());

        for (TransactionWriteRequest.TransactionWriteOperation writeOperation : writeOperations) {
            TransactWriteItem writeItem = generateTransactWriteItem(writeOperation, inMemoryUpdates, finalConfig);
            if (writeOperation.getObject() instanceof StockDataItem && writeItem.getUpdate() != null) {
                String updateExpression = writeItem.getUpdate().getUpdateExpression();
                String identifier = getIdentifier(writeItem.getUpdate().getExpressionAttributeNames(), TABLE_ALERT_COUNT_KEY);
                if (updateExpression != null && identifier != null) {
                    updateExpression = updateExpression.replace(identifier + " = ",
                            identifier + " = " +
                                    " if_not_exists(" + identifier + ", :initial)" +
                                    (shouldIncreaseAlertCount ? " + " : "-"));
                    writeItem.getUpdate().setUpdateExpression(updateExpression);
                    writeItem.getUpdate().getExpressionAttributeValues().put(":initial", new AttributeValue().withN("0"));
                }
            }
            transactWriteItems.add(writeItem);
        }

        transactWriteItemsRequest.setTransactItems(transactWriteItems);

        dynamoDB.transactWriteItems(transactWriteItemsRequest);

        // Update the inMemory values for autogenerated attributeValues after successful completion of transaction
        for (ValueUpdate update : inMemoryUpdates) {
            update.apply();
        }
    }

    @Override
    public <T> void save(T object, DynamoDBSaveExpression saveExpression, DynamoDBMapperConfig config) {
        final DynamoDBMapperConfig finalConfig = mergeConfig(config);

        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) object.getClass();
        String tableName = getTableName(clazz, object, finalConfig);

        final DynamoDBMapperTableModel<T> model = getTableModel(clazz, finalConfig);

        /*
         * We use a putItem request instead of updateItem request either when
         * CLOBBER or PUT is configured, or part of the primary key of the object needs
         * to be auto-generated.
         */
        boolean usePut = (finalConfig.getSaveBehavior() == DynamoDBMapperConfig.SaveBehavior.CLOBBER
                || finalConfig.getSaveBehavior() == DynamoDBMapperConfig.SaveBehavior.PUT)
                || anyKeyGeneratable(model, object, finalConfig.getSaveBehavior());

        SaveObjectHandler saveObjectHandler;

        if (usePut) {
            saveObjectHandler = this.new SaveObjectHandler(clazz, object,
                    tableName, finalConfig, saveExpression) {

                @Override
                protected void onPrimaryKeyAttributeValue(String attributeName,
                                                          AttributeValue keyAttributeValue) {
                    /* Treat key values as common attribute value updates. */
                    getAttributeValueUpdates().put(attributeName,
                            new AttributeValueUpdate().withValue(keyAttributeValue)
                                    .withAction("PUT"));
                }

                /* Use default implementation of onNonKeyAttribute(...) */

                @Override
                protected void onNullNonKeyAttribute(String attributeName) {
                    /* When doing a force put, we can safely ignore the null-valued attributes. */
                    return;
                }

                @Override
                protected void executeLowLevelRequest() {
                    /* Send a putItem request */
                    doPutItem();
                }
            };
        } else {
            saveObjectHandler = this.new SaveObjectHandler(clazz, object,
                    tableName, finalConfig, saveExpression) {

                @Override
                protected void onPrimaryKeyAttributeValue(String attributeName,
                                                          AttributeValue keyAttributeValue) {
                    /* Put it in the key collection which is later used in the updateItem request. */
                    getPrimaryKeyAttributeValues().put(attributeName, keyAttributeValue);
                }


                @Override
                protected void onNonKeyAttribute(String attributeName,
                                                 AttributeValue currentValue) {
                    /* If it's a set attribute and the mapper is configured with APPEND_SET,
                     * we do an "ADD" update instead of the default "PUT".
                     */
                    if (getLocalSaveBehavior() == DynamoDBMapperConfig.SaveBehavior.APPEND_SET) {
                        if (currentValue.getBS() != null
                                || currentValue.getNS() != null
                                || currentValue.getSS() != null) {
                            getAttributeValueUpdates().put(
                                    attributeName,
                                    new AttributeValueUpdate().withValue(
                                            currentValue).withAction("ADD"));
                            return;
                        }
                    }
                    /* Otherwise, we do the default "PUT" update. */
                    super.onNonKeyAttribute(attributeName, currentValue);
                }

                @Override
                protected void onNullNonKeyAttribute(String attributeName) {
                    /*
                     * If UPDATE_SKIP_NULL_ATTRIBUTES or APPEND_SET is
                     * configured, we don't delete null value attributes.
                     */
                    if (getLocalSaveBehavior() == DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES
                            || getLocalSaveBehavior() == DynamoDBMapperConfig.SaveBehavior.APPEND_SET) {
                        return;
                    } else {
                        /* Delete attributes that are set as null in the object. */
                        getAttributeValueUpdates()
                                .put(attributeName,
                                        new AttributeValueUpdate()
                                                .withAction("DELETE"));
                    }
                }

                @Override
                protected void executeLowLevelRequest() {
                    UpdateItemResult updateItemResult = doUpdateItem();

                    // The UpdateItem request is specified to return ALL_NEW
                    // attributes of the affected item. So if the returned
                    // UpdateItemResult does not include any ReturnedAttributes,
                    // it indicates the UpdateItem failed silently (e.g. the
                    // key-only-put nightmare -
                    // https://forums.aws.amazon.com/thread.jspa?threadID=86798&tstart=25),
                    // in which case we should re-send a PutItem
                    // request instead.
                    if (updateItemResult.getAttributes() == null
                            || updateItemResult.getAttributes().isEmpty()) {
                        // Before we proceed with PutItem, we need to put all
                        // the key attributes (prepared for the
                        // UpdateItemRequest) into the AttributeValueUpdates
                        // collection.
                        for (String keyAttributeName : getPrimaryKeyAttributeValues().keySet()) {
                            getAttributeValueUpdates().put(keyAttributeName,
                                    new AttributeValueUpdate()
                                            .withValue(getPrimaryKeyAttributeValues().get(keyAttributeName))
                                            .withAction("PUT"));
                        }

                        doPutItem();
                    }
                }
            };
        }

        saveObjectHandler.execute();
    }

    private String getIdentifier(Map<String, String> attributeNameMap, String attributeName) {
        if (attributeNameMap != null) {
            for (Map.Entry<String, String> entry : attributeNameMap.entrySet()) {
                if (entry.getValue().equals(attributeName)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private TransactWriteItem generateTransactWriteItem(TransactionWriteRequest.TransactionWriteOperation transactionWriteOperation,
                                                        List<ValueUpdate> inMemoryUpdates,
                                                        DynamoDBMapperConfig config) {
        Object objectToWrite = transactionWriteOperation.getObject();
        DynamoDBTransactionWriteExpression writeExpression = transactionWriteOperation.getDynamoDBTransactionWriteExpression();
        ReturnValuesOnConditionCheckFailure returnValuesOnConditionCheckFailure = transactionWriteOperation.getReturnValuesOnConditionCheckFailure();
        TransactionWriteRequest.TransactionWriteOperationType operationType = transactionWriteOperation.getTransactionWriteOperationType();
        Class<Object> clazz = (Class<Object>) objectToWrite.getClass();
        String tableName = getTableName(clazz, objectToWrite, config);
        Map<String, AttributeValue> attributeValues = new HashMap<String, AttributeValue>();
        final DynamoDBMapperTableModel<Object> model = getTableModel(clazz, config);
        VersionAttributeConditionExpressionGenerator versionAttributeConditionExpressionGenerator =
                new VersionAttributeConditionExpressionGenerator();

        for (final DynamoDBMapperFieldModel<Object, Object> field : model.fields()) {
            AttributeValue currentValue = null;
            if (field.versioned()) {
                if (writeExpression != null) {
                    throw new SdkClientException("A transactional write operation may not also specify a condition " +
                            "expression if a versioned attribute is present on the " +
                            "model of the item.");
                } else {
                    Object fieldValue = field.get(objectToWrite);
                    versionAttributeConditionExpressionGenerator
                            .appendVersionAttributeToConditionExpression(field,
                                    fieldValue);
                    currentValue = field.convert(field.generate(field.get(objectToWrite)));
                    inMemoryUpdates.add(new ValueUpdate(field, currentValue, objectToWrite));
                }
            } else if (canGenerate(model, objectToWrite, DynamoDBMapperConfig.SaveBehavior.CLOBBER, field)) {
                currentValue = field.convert(field.generate(field.get(objectToWrite)));
                inMemoryUpdates.add(new ValueUpdate(field, currentValue, objectToWrite));
            } else {
                currentValue = field.convert(field.get(objectToWrite));
            }
            if (currentValue == null && field.keyType() != null) {
                throw new DynamoDBMappingException(clazz.getSimpleName() + "[" + field.name() + "]; null or empty value for primary key");
            } else if (currentValue != null) {
                attributeValues.put(field.name(), currentValue);
            }
        }

        DynamoDBTransactionWriteExpression versionAttributeConditionExpression =
                versionAttributeConditionExpressionGenerator.getVersionAttributeConditionExpression();
        if (versionAttributeConditionExpression.getConditionExpression() != null) {
            writeExpression = versionAttributeConditionExpression;
        }
        AttributeTransformer.Parameters<?> parameters =
                toParameters(attributeValues, clazz, tableName, config);
        Map<String, AttributeValue> attributeValueMap = transformAttributes(parameters);

        TransactWriteItem transactWriteItem = new TransactWriteItem();

        switch (operationType) {
            case Put:
                transactWriteItem.setPut(generatePut(tableName, attributeValueMap, returnValuesOnConditionCheckFailure, writeExpression));
                break;
            case Update:
                transactWriteItem.setUpdate(
                        generateUpdate(model, tableName, attributeValueMap, returnValuesOnConditionCheckFailure, writeExpression));
                break;
            case ConditionCheck:
                transactWriteItem.setConditionCheck(
                        generateConditionCheck(model, tableName, objectToWrite, returnValuesOnConditionCheckFailure, writeExpression));
                break;
            case Delete:
                transactWriteItem.setDelete(
                        generateDelete(model, tableName, objectToWrite, returnValuesOnConditionCheckFailure, writeExpression));
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operationType: " + operationType + " for object: " + model.convertKey(objectToWrite) + " of type: " + clazz);
        }
        return transactWriteItem;
    }

    private Put generatePut(String tableName,
                            Map<String, AttributeValue> attributeValueMap,
                            ReturnValuesOnConditionCheckFailure returnValuesOnConditionCheckFailure,
                            DynamoDBTransactionWriteExpression writeExpression) {
        Put put = new Put();
        put.setItem(attributeValueMap);
        put.setTableName(tableName);
        if (returnValuesOnConditionCheckFailure != null) {
            put.setReturnValuesOnConditionCheckFailure(
                    returnValuesOnConditionCheckFailure.toString());
        }
        if (writeExpression != null) {
            if (writeExpression.getConditionExpression() != null) {
                put.setConditionExpression(writeExpression.getConditionExpression());
            }
            if (!isNullOrEmpty(writeExpression.getExpressionAttributeNames())) {
                put.setExpressionAttributeNames(writeExpression.getExpressionAttributeNames());
            }
            if (!isNullOrEmpty(writeExpression.getExpressionAttributeValues())) {
                put.setExpressionAttributeValues(writeExpression.getExpressionAttributeValues());
            }
        }
        return put;
    }

    private Update generateUpdate(DynamoDBMapperTableModel<Object> model,
                                  String tableName,
                                  Map<String, AttributeValue> attributeValueMap,
                                  ReturnValuesOnConditionCheckFailure returnValuesOnConditionCheckFailure,
                                  DynamoDBTransactionWriteExpression writeExpression) {
        Update update = new Update();
        Map<String, String> expressionAttributeNamesMap = new HashMap<String, String>();
        Map<String, AttributeValue> expressionsAttributeValuesMap = new HashMap<String, AttributeValue>();
        if (returnValuesOnConditionCheckFailure != null) {
            update.setReturnValuesOnConditionCheckFailure(
                    returnValuesOnConditionCheckFailure.toString());
        }
        if (writeExpression != null) {
            if (writeExpression.getConditionExpression() != null) {
                update.setConditionExpression(writeExpression.getConditionExpression());
            }
            if (!isNullOrEmpty(writeExpression.getExpressionAttributeNames())) {
                expressionAttributeNamesMap.putAll(writeExpression.getExpressionAttributeNames());
            }
            if (!isNullOrEmpty(writeExpression.getExpressionAttributeValues())) {
                expressionsAttributeValuesMap.putAll(writeExpression.getExpressionAttributeValues());
            }
        }
        Map<String, AttributeValue> keyAttributeValueMap = new HashMap<String, AttributeValue>();
        Map<String, AttributeValue> nonKeyNonNullAttributeValueMap = new HashMap<String, AttributeValue>();
        // These are the non-key attributes that are present in the model and not in the customer object,
        // meaning they're to be removed in this update
        List<String> nullValuedNonKeyAttributeNames = new ArrayList<String>();

        for (final DynamoDBMapperFieldModel<Object, Object> field : model.fields()) {
            if (field.keyType() != null) {
                keyAttributeValueMap.put(field.name(), attributeValueMap.get(field.name()));
            } else if (attributeValueMap.get(field.name()) != null) {
                nonKeyNonNullAttributeValueMap.put(field.name(), attributeValueMap.get(field.name()));
            } else {
                nullValuedNonKeyAttributeNames.add(field.name());
            }
        }

        update.setTableName(tableName);
        update.setUpdateExpression(new UpdateExpressionGenerator()
                .generateUpdateExpressionAndUpdateAttributeMaps(expressionAttributeNamesMap,
                        expressionsAttributeValuesMap,
                        nonKeyNonNullAttributeValueMap,
                        nullValuedNonKeyAttributeNames));
        update.setKey(keyAttributeValueMap);
        if (expressionAttributeNamesMap.size() > 0) {
            update.setExpressionAttributeNames(expressionAttributeNamesMap);
        }
        if (expressionsAttributeValuesMap.size() > 0) {
            update.setExpressionAttributeValues(expressionsAttributeValuesMap);
        }

        return update;
    }

    private ConditionCheck generateConditionCheck(DynamoDBMapperTableModel<Object> model,
                                                  String tableName,
                                                  Object objectToConditionCheck,
                                                  ReturnValuesOnConditionCheckFailure returnValuesOnConditionCheckFailure,
                                                  DynamoDBTransactionWriteExpression writeExpression) {
        ConditionCheck conditionCheck = new ConditionCheck();
        conditionCheck.setKey(model.convertKey(objectToConditionCheck));
        conditionCheck.setTableName(tableName);
        if (returnValuesOnConditionCheckFailure != null) {
            conditionCheck.setReturnValuesOnConditionCheckFailure(
                    returnValuesOnConditionCheckFailure.toString());
        }
        if (writeExpression != null) {
            conditionCheck.setConditionExpression(writeExpression.getConditionExpression());
            if (!isNullOrEmpty(writeExpression.getExpressionAttributeNames())) {
                conditionCheck.setExpressionAttributeNames(writeExpression.getExpressionAttributeNames());
            }
            if (!isNullOrEmpty(writeExpression.getExpressionAttributeValues())) {
                conditionCheck.setExpressionAttributeValues(writeExpression.getExpressionAttributeValues());
            }
        }
        return conditionCheck;
    }

    private Delete generateDelete(DynamoDBMapperTableModel<Object> model,
                                  String tableName,
                                  Object objectToDelete,
                                  ReturnValuesOnConditionCheckFailure returnValuesOnConditionCheckFailure,
                                  DynamoDBTransactionWriteExpression writeExpression) {


        Delete delete = new Delete();
        delete.setKey(model.convertKey(objectToDelete));
        delete.setTableName(tableName);
        if (returnValuesOnConditionCheckFailure != null) {
            delete.setReturnValuesOnConditionCheckFailure(
                    returnValuesOnConditionCheckFailure.toString());
        }
        if (writeExpression != null) {
            if (writeExpression.getConditionExpression() != null) {
                delete.setConditionExpression(writeExpression.getConditionExpression());
            }
            if (!isNullOrEmpty(writeExpression.getExpressionAttributeNames())) {
                delete.setExpressionAttributeNames(writeExpression.getExpressionAttributeNames());
            }
            if (!isNullOrEmpty(writeExpression.getExpressionAttributeValues())) {
                delete.setExpressionAttributeValues(writeExpression.getExpressionAttributeValues());
            }
        }
        return delete;
    }

    private <T> AttributeTransformer.Parameters<T> toParameters(
            final Map<String, AttributeValue> attributeValues,
            final Class<T> modelClass,
            final String tableName,
            final DynamoDBMapperConfig mapperConfig) {

        return toParameters(attributeValues, false, modelClass, tableName, mapperConfig);
    }

    private <T> AttributeTransformer.Parameters<T> toParameters(
            final Map<String, AttributeValue> attributeValues,
            final boolean partialUpdate,
            final Class<T> modelClass,
            final String tableName,
            final DynamoDBMapperConfig mapperConfig) {

        return new TransformerParameters<T>(
                getTableModel(modelClass, mapperConfig),
                attributeValues,
                partialUpdate,
                modelClass,
                mapperConfig,
                tableName);
    }

    private Map<String, AttributeValue> transformAttributes(
            final AttributeTransformer.Parameters<?> parameters) {
        return parameters.getAttributeValues();
    }

    /**
     * The one true implementation of AttributeTransformer.Parameters.
     */
    private static class TransformerParameters<T>
            implements AttributeTransformer.Parameters<T> {

        private final DynamoDBMapperTableModel<T> model;
        private final Map<String, AttributeValue> attributeValues;
        private final boolean partialUpdate;
        private final Class<T> modelClass;
        private final DynamoDBMapperConfig mapperConfig;
        private final String tableName;

        public TransformerParameters(
                final DynamoDBMapperTableModel<T> model,
                final Map<String, AttributeValue> attributeValues,
                final boolean partialUpdate,
                final Class<T> modelClass,
                final DynamoDBMapperConfig mapperConfig,
                final String tableName) {

            this.model = model;
            this.attributeValues =
                    Collections.unmodifiableMap(attributeValues);
            this.partialUpdate = partialUpdate;
            this.modelClass = modelClass;
            this.mapperConfig = mapperConfig;
            this.tableName = tableName;
        }

        @Override
        public Map<String, AttributeValue> getAttributeValues() {
            return attributeValues;
        }

        @Override
        public boolean isPartialUpdate() {
            return partialUpdate;
        }

        @Override
        public Class<T> getModelClass() {
            return modelClass;
        }

        @Override
        public DynamoDBMapperConfig getMapperConfig() {
            return mapperConfig;
        }

        @Override
        public String getTableName() {
            return tableName;
        }

        @Override
        public String getHashKeyName() {
            return model.hashKey().name();
        }

        @Override
        public String getRangeKeyName() {
            return model.rangeKeyIfExists() == null ? null : model.rangeKey().name();
        }
    }

    private static final class ValueUpdate {
        private final DynamoDBMapperFieldModel<Object, Object> field;
        private final AttributeValue newValue;
        private final Object target;

        private ValueUpdate(
                DynamoDBMapperFieldModel<Object, Object> field,
                AttributeValue newValue,
                Object target) {

            this.field = field;
            this.newValue = newValue;
            this.target = target;
        }

        private void apply() {
            field.set(target, field.unconvert(newValue));
        }
    }

}
