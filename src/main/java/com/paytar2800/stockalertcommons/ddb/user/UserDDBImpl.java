package com.paytar2800.stockalertcommons.ddb.user;


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.inject.Inject;
import com.paytar2800.stockalertcommons.ddb.DDBUtils;
import com.paytar2800.stockalertcommons.ddb.user.model.UserDataItem;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES;

public class UserDDBImpl implements UserDAO {

    private DynamoDBMapper dynamoDBMapper;

    @Inject
    public UserDDBImpl(DynamoDBMapper dynamoDBMapper){
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public UserDDBImpl(){
        this.dynamoDBMapper = DDBUtils.getDynamoDBMapper();
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

    public Optional<UserDataItem> getItemUsingUserId(@NonNull String userId) {
        return Optional.ofNullable(dynamoDBMapper.load(UserDataItem.class, userId));
    }

    public void deleteItem(@NonNull UserDataItem dataItem) {
        UserDataItem item = UserDataItem.builder().userId(dataItem.getUserId()).build();
        dynamoDBMapper.delete(item);
    }
}
