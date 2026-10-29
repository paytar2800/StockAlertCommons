package com.paytar2800.stockalertcommons.ddb.userearnings;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.paytar2800.stockalertcommons.ddb.userearnings.model.UserEarningsDataItem;

import java.util.ArrayList;
import java.util.Optional;

public class UserEarningsDDBImpl implements UserEarningsDAO {
    private final DynamoDBMapper dynamoDBMapper;

    public UserEarningsDDBImpl(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    @Override
    public Optional<UserEarningsDataItem> getUserEarnings(String userId) {
        return Optional.ofNullable(dynamoDBMapper.load(UserEarningsDataItem.class, userId));
    }

    @Override
    public void saveUserEarnings(UserEarningsDataItem item) {
        // The mapper skips null attributes, so null lists become empty ones to make a save a full replace
        if (item.getCustomTickers() == null) {
            item.setCustomTickers(new ArrayList<>());
        }
        if (item.getHiddenTickers() == null) {
            item.setHiddenTickers(new ArrayList<>());
        }
        item.setLastUpdateTime(System.currentTimeMillis());
        dynamoDBMapper.save(item);
    }

    @Override
    public void deleteUserEarnings(String userId) {
        UserEarningsDataItem item = UserEarningsDataItem.builder()
                .userId(userId)
                .build();
        dynamoDBMapper.delete(item);
    }
}
