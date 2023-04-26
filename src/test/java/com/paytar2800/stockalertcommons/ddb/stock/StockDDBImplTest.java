package com.paytar2800.stockalertcommons.ddb.stock;

import com.paytar2800.stockalertcommons.ddb.PaginatedItem;
import com.paytar2800.stockalertcommons.ddb.stock.model.StockDataItem;
import com.paytar2800.stockalertcommons.ddb.util.LocalDDBServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class StockDDBImplTest {

    private StockDAO stockDAO;
    private List<StockDataItem> stockDataItemList;

    @Before
    public void setUp() throws Exception {
        LocalDDBServer.startServer();
        LocalDDBServer.createTable(StockDataItem.class);
        stockDAO = new StockDDBImpl(LocalDDBServer.getDynamoDBMapper());
        stockDataItemList = StockData.getSampleData();
    }

    @After
    public void tearDown() throws Exception {
        LocalDDBServer.stopServer();
        stockDAO = null;
    }

    private void addItemsToDB(){
        stockDataItemList.forEach(LocalDDBServer::saveItemToDB);
    }

    private List<StockDataItem> getStockDataItemList(String priority, String exchange){
        return stockDataItemList.stream()
                .filter(stockDataItem -> stockDataItem.getExchange().equals(exchange)
                        && stockDataItem.getPriority().equals(priority))
                .collect(Collectors.toList());
    }

    private List<StockDataItem> getStockDataItemList(String priority){
        return stockDataItemList.stream()
                .filter(stockDataItem -> stockDataItem.getPriority().equals(priority))
                .collect(Collectors.toList());
    }

    private void compareStockDataItems(List<StockDataItem> expected, List<StockDataItem> actual){
        assertEquals(expected.size(), actual.size());
        expected.forEach(expectedItem -> assertTrue(actual.contains(expectedItem)));
    }

    private void checkIfTickersMatch(List<String> expected, List<StockDataItem> actual){
        assertEquals(expected.size(), actual.size());
        actual.forEach(stockDataItem -> assertTrue(expected.contains(stockDataItem.getTicker())));
    }

    @Test
    public void getTickersForPriority() {
        addItemsToDB();
        String priority = "P1";
        PaginatedItem<String, String> dataItems = stockDAO.getTickersForPriority(priority,null,null);
        List<String> tickerList = dataItems.getCurrentItemList();

        checkIfTickersMatch(tickerList, getStockDataItemList(priority));
    }

    @Test
    public void getTickersForPriorityWIthExtraAttributes() {
        addItemsToDB();
        String priority = "P1";
        List<String> projectionList = new ArrayList<>();
        projectionList.add(StockDDBConstants.TABLE_TICKER_KEY);
        projectionList.add(StockDDBConstants.TABLE_STOCK_EXCHANGE_KEY);
        //projectionList.add(StockDDBConstants.TABLE_ALERT_COUNT_KEY);

        PaginatedItem<StockDataItem, String> dataItems = stockDAO.getStockDataItemsForPriority(priority,
                projectionList,null,null);

        List<StockDataItem> stockItems = dataItems.getCurrentItemList();
        List<StockDataItem> actualItems = getStockDataItemList(priority);

        stockItems.forEach(stockItem -> {
            int index = actualItems.indexOf(stockItem);
            StockDataItem actual = actualItems.get(index);

            assertEquals(actual.getTicker(), stockItem.getTicker());
            assertEquals(actual.getExchange(), stockItem.getExchange());
            //assertEquals(actual.getAlertCount(), stockItem.getAlertCount());
            assertNull(stockItem.getPriority());
        });

    }

    @Test
    public void getStockItemsForPriority() {
        addItemsToDB();
        String priority = "P1";
        String exchange = "NSE";
        List<StockDataItem> dataItems = stockDAO.getStockItemsForPriority(priority,exchange);
        compareStockDataItems(dataItems,getStockDataItemList(priority,exchange));
    }

    @Test
    public void testScanStockItems() {
        addItemsToDB();
        List<StockDataItem> dataItems = stockDAO.getAllStockDataItems();
        compareStockDataItems(dataItems, stockDataItemList);
    }

    @Test
    public void updateStock() {
    }
}