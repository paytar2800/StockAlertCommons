package com.paytar2800.stockalertcommons.ddb.stock;


import com.paytar2800.stockalertcommons.ddb.PaginatedItem;
import com.paytar2800.stockalertcommons.ddb.stock.model.StockDataItem;

public interface StockDAO {

    PaginatedItem<String, String> getTickersForPriority(
            String priority, String nextPageToken, Integer maxItemsPerPage);

    void updateStock(StockDataItem dataItem);
}
