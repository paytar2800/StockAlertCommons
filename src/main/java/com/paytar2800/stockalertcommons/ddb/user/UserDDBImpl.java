package com.paytar2800.stockalertcommons.ddb.user;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.paytar2800.stockalertcommons.ddb.NextTokenSerializer;
import com.paytar2800.stockalertcommons.ddb.PaginatedItem;
import com.paytar2800.stockalertcommons.ddb.StockUtils;
import com.paytar2800.stockalertcommons.ddb.alert.model.AlertDataItem;
import com.paytar2800.stockalertcommons.ddb.user.model.UserDataItem;
import com.paytar2800.stockalertcommons.ddb.user.model.UserDataItem_DeletedData;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES;

public class UserDDBImpl implements UserDAO {

    private DynamoDBMapper dynamoDBMapper;

    public UserDDBImpl(DynamoDBMapper dynamoDBMapper){
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public void putItem(@NonNull UserDataItem userDataItem) {
        dynamoDBMapper.save(userDataItem);
    }


    //Only updates non null attributes.
    public void updateItem(@NonNull UserDataItem userDataItem) {
        DynamoDBMapperConfig dynamoDBMapperConfig = DynamoDBMapperConfig.builder()
                .withSaveBehavior(UPDATE_SKIP_NULL_ATTRIBUTES)
                .build();
        dynamoDBMapper.save(userDataItem, dynamoDBMapperConfig);
    }

    /*
       This overwrites items instead of updating non-null attributes only.
     */
    public List<DynamoDBMapper.FailedBatch> batchPutItems(@NonNull List<UserDataItem> userDataItemList) {
        return dynamoDBMapper.batchSave(userDataItemList);
    }


    //getItem using emailId as GSI key
    public Optional<UserDataItem> getItemUsingEmail(@NonNull String emailId) {

        Map<String, AttributeValue> eav = new HashMap<>();
        String secondaryKey = ":val1";
        eav.put(secondaryKey, new AttributeValue().withS(emailId));

        DynamoDBQueryExpression<UserDataItem> queryExpression =
                new DynamoDBQueryExpression<UserDataItem>()
                        .withKeyConditionExpression(
                                UserDDBConstants.TABLE_EMAIL_KEY + " = " + secondaryKey)
                        .withExpressionAttributeValues(eav)
                        .withIndexName(UserDDBConstants.TABLE_EMAIL_GSI_KEY)
                        .withConsistentRead(false);

        PaginatedQueryList<UserDataItem> resultList = dynamoDBMapper.query(UserDataItem.class, queryExpression);

        if (resultList != null && !resultList.isEmpty()) {
            return Optional.ofNullable(resultList.get(0));
        }

        return Optional.empty();
    }

    @Override
    public Optional<String> getUserIdForEmail(@NonNull String emailId) {
        Map<String, AttributeValue> eav = new HashMap<>();
        String secondaryKey = ":val1";
        eav.put(secondaryKey, new AttributeValue().withS(emailId));

        DynamoDBQueryExpression<UserDataItem> queryExpression =
                new DynamoDBQueryExpression<UserDataItem>()
                        .withKeyConditionExpression(
                                UserDDBConstants.TABLE_EMAIL_KEY + " = " + secondaryKey)
                        .withExpressionAttributeValues(eav)
                        .withIndexName(UserDDBConstants.TABLE_EMAIL_GSI_KEY)
                        .withConsistentRead(false);

        PaginatedQueryList<UserDataItem> resultList = dynamoDBMapper.query(UserDataItem.class, queryExpression);

        if (resultList != null && !resultList.isEmpty()) {
            return Optional.ofNullable(resultList.get(0).getUserId());
        }

        return Optional.empty();
    }

    public Optional<UserDataItem> getItemUsingUserId(@NonNull String userId) {
        return Optional.ofNullable(dynamoDBMapper.load(UserDataItem.class, userId));
    }

    @Override
    public PaginatedItem<String, String> getLatestUpdatedUsers(String nextPageToken,
                                                                     Integer maxItemsPerPage) {

        Map<String, AttributeValue> eav = new HashMap<>();
        String partitonKey = ":val1";
        eav.put(partitonKey, new AttributeValue().withN("1"));

        DynamoDBQueryExpression<UserDataItem> queryExpression =
                new DynamoDBQueryExpression<UserDataItem>()
                        .withKeyConditionExpression(
                                UserDDBConstants.TABLE_HAS_CHANGED_KEY + " = " + partitonKey)
                        .withExpressionAttributeValues(eav)
                        .withIndexName(UserDDBConstants.TABLE_HAS_CHANGED_GSI_KEY)
                        .withExclusiveStartKey(unserializePaginationToken(nextPageToken))
                        .withProjectionExpression(UserDDBConstants.TABLE_USERID_KEY)
                        .withConsistentRead(false)
                        .withLimit(maxItemsPerPage);

        QueryResultPage<UserDataItem> queryResultPage = dynamoDBMapper.queryPage(
                UserDataItem.class, queryExpression);

        String nextToken = serializePaginationToken(queryResultPage.getLastEvaluatedKey());

        List<UserDataItem> results = queryResultPage.getResults();

        List<String> userIdList = new ArrayList<>();

        if(results != null){
            results.forEach(userDataItem -> userIdList.add(userDataItem.getUserId()));
        }

        return new PaginatedItem<>(userIdList, nextToken);
    }

    public void deleteItem(@NonNull UserDataItem dataItem) {
        UserDataItem item = UserDataItem.builder().userId(dataItem.getUserId()).build();
        //copy userdata first.
        copyUserDataToDeletedDataTable(item);
        dynamoDBMapper.delete(item);
    }

    @Override
    public void copyUserDataToDeletedDataTable(UserDataItem userDataItem) {
        UserDataItem_DeletedData userDataItemDeletedData = new UserDataItem_DeletedData(
                dynamoDBMapper.load(userDataItem));
        userDataItemDeletedData.setDeleteDate(StockUtils.getTodayDate());
        dynamoDBMapper.save(userDataItemDeletedData);
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
