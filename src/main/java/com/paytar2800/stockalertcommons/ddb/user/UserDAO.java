package com.paytar2800.stockalertcommons.ddb.user;

import com.paytar2800.stockalertcommons.ddb.user.model.UserDataItem;
import lombok.NonNull;

import java.util.Optional;

public interface UserDAO {

    Optional<UserDataItem> getItemUsingEmail(@NonNull String emailId);

    Optional<UserDataItem> getItemUsingUserId(@NonNull String userId);

    void updateItem(@NonNull UserDataItem userDataItem);

    void deleteItem(@NonNull UserDataItem userDataItem);
}
