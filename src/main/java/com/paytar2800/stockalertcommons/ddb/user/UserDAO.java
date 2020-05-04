package com.paytar2800.stockalertcommons.ddb.user;

import com.paytar2800.stockalertcommons.ddb.PaginatedItem;
import com.paytar2800.stockalertcommons.ddb.user.model.UserDataItem;
import lombok.NonNull;

import java.util.Optional;

public interface UserDAO {

    Optional<String> getUserIdForEmail(@NonNull String emailId);

    Optional<UserDataItem> getItemUsingUserId(@NonNull String userId);

    /*
     * Gets paginated item list of alerts for given ticker.
     */
    PaginatedItem<String, String> getLatestUpdatedUsers(String nextPageToken,
                                                              Integer maxItemsPerPage);

    void updateItem(@NonNull UserDataItem userDataItem);

    void deleteItem(@NonNull UserDataItem userDataItem);
}
