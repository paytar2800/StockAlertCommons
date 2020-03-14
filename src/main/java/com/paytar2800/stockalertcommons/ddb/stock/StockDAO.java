package com.paytar2800.stockalertcommons.ddb.stock;


import com.paytar2800.stockalertcommons.ddb.PaginatedItem;
import com.paytar2800.stockalertcommons.ddb.stock.model.StockDataItem;

import java.util.List;

public interface StockDAO {

    PaginatedItem<String, String> getTickersForPriority(
            String priority, String nextPageToken, Integer maxItemsPerPage);

    List<StockDataItem> getStockItemsForPriority(String priority, String exchange);

    void updateStock(StockDataItem dataItem);
}
