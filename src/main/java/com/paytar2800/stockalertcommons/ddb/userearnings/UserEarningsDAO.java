package com.paytar2800.stockalertcommons.ddb.userearnings;

import com.paytar2800.stockalertcommons.ddb.userearnings.model.UserEarningsDataItem;

import java.util.Optional;

public interface UserEarningsDAO {
    Optional<UserEarningsDataItem> getUserEarnings(String userId);

    void saveUserEarnings(UserEarningsDataItem item);

    void deleteUserEarnings(String userId);
}
