package com.paytar2800.stockalertcommons.ddb.alert;

import com.paytar2800.stockalertcommons.ddb.PaginatedItem;
import com.paytar2800.stockalertcommons.ddb.alert.model.AlertDataItem;
import com.paytar2800.stockalertcommons.ddb.alert.model.UserWatchlistId;
import com.paytar2800.stockalertcommons.ddb.stock.model.StockDataItem;

import java.util.List;
import java.util.Optional;

public interface AlertDAO {


    Optional<AlertDataItem> getAlert(AlertDataItem alertDataItem);

    /**
     * Use this method to only put new alerts as this does transaction to Stock table for alert count as well
     */
    void putNewAlert(AlertDataItem alertDataItem, boolean isNewAlert);

    /*
     * Use this to update existing alerts
     */
    void updateAlert(AlertDataItem alertDataItem);

    /**
     * Use this method with care since its not been tested fully
     */
    void updateBatchAlerts(List<AlertDataItem> alertDataItemList);

    void updateStock(StockDataItem stockDataItem);

    /*
     * Use this to deleteAlert
     */
    void deleteAlert(AlertDataItem alertDataItem, boolean isRetry);

    /*
     * Use this to deleteWatchlist for a given userId
     */
    void deleteWatchlist(UserWatchlistId userWatchlistId);

    /*
     * Use this to deleteUser for a given userId
     */
    void deleteUser(String userId);


    /*
     * Gets paginated item list of alerts for given ticker.
     */
    PaginatedItem<AlertDataItem, String> getAlertsForTicker(
            String ticker, String nextPageToken, Integer maxItemsPerPage);

    /*
     * Update alert trigger time for next time
     */
    void updateAlertTriggerTimeOnly(AlertDataItem alertDataItem);


    /*PaginatedItem<AlertDataItem, String> getLatestUpdatedUsers(String nextPageToken,
                                                               Integer maxItemsPerPage);*/

    void copyBatchAlertDataToDeletedDataTable(List<AlertDataItem> alertDataItems);


    /**
     * Use this method with care since its not been tested fully
     */
    List<AlertDataItem> getAlertsForUser(String userId);

}
