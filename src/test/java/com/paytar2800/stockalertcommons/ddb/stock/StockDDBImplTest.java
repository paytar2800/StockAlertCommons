package com.paytar2800.stockalertcommons.ddb.stock;

import com.paytar2800.stockalertcommons.ddb.DDBUtils;
import com.paytar2800.stockalertcommons.ddb.stock.model.StockDataItem;
import com.paytar2800.stockalertcommons.ddb.util.LocalDDBServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
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

    private void compareStockDataItems(List<StockDataItem> expected, List<StockDataItem> actual){
        assertEquals(expected.size(), actual.size());
        expected.forEach(expectedItem -> assertTrue(actual.contains(expectedItem)));
    }

    @Test
    public void getTickersForPriority() {
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
    public void updateStock() {
    }
}