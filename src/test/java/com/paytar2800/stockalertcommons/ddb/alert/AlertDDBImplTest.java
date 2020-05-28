package com.paytar2800.stockalertcommons.ddb.alert;

import com.paytar2800.stockalertcommons.ddb.alert.model.AlertDataItem;
import com.paytar2800.stockalertcommons.ddb.alert.model.NetPercentChangeAlertItem;
import com.paytar2800.stockalertcommons.ddb.alert.model.SimpleDailyPercentAlertItem;
import com.paytar2800.stockalertcommons.ddb.alert.model.SimplePriceAlertItem;
import com.paytar2800.stockalertcommons.ddb.alert.model.SimpleVolumePercentAlertItem;
import com.paytar2800.stockalertcommons.ddb.alert.model.UserWatchlistId;
import com.paytar2800.stockalertcommons.ddb.stock.model.StockDataItem;
import com.paytar2800.stockalertcommons.ddb.util.LocalDDBServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AlertDDBImplTest {

    AlertDAO alertDAO;
    List<AlertDataItem> alertDataItemList;
    Map<String, Long> stockCountMap;

    @Before
    public void before() throws Exception {
        LocalDDBServer.startServer();
        LocalDDBServer.createTable(AlertDataItem.class);
        LocalDDBServer.createTable(StockDataItem.class);
        alertDataItemList = AlertData.getSampleData();
        stockCountMap = AlertData.getStockCountMap(alertDataItemList);
        alertDAO = new AlertDDBImpl(LocalDDBServer.getCustomMapper());
    }

    @After
    public void After() throws Exception {
        LocalDDBServer.stopServer();
        alertDAO = null;
    }

    @Test
    public void getAlert() {
        alertDataItemList.forEach(LocalDDBServer::saveItemToDB);
        alertDataItemList.forEach(alertDataItem -> {
            Optional<AlertDataItem> dataItem = alertDAO.getAlert(alertDataItem);
            assertTrue(dataItem.isPresent());
            assertEquals(alertDataItem, dataItem.get());
        });
    }

    @Test
    public void putNewAlert() {
        alertDataItemList.forEach(alertDataItem -> alertDAO.putNewAlert(alertDataItem, true));
        alertDataItemList.forEach(alertDataItem -> {
            StockDataItem stockItem = (StockDataItem) LocalDDBServer.loadItemFromDB(new StockDataItem(alertDataItem.getTicker()));
            assertEquals(stockCountMap.get(stockItem.getTicker()), stockItem.getAlertCount());
        });
    }

    @Test
    public void updateAlert() {
        alertDataItemList.forEach(alertDataItem -> {
            alertDataItem.setSimplePriceAlertItem(
                    SimplePriceAlertItem.builder().highPrice(23.0).lowPrice(24.0).build());
            alertDataItem.setSimpleDailyPercentAlertItem(
                    SimpleDailyPercentAlertItem.builder().lowPercent(22.0).build());
            alertDataItem.setSimpleVolumePercentAlertItem(
                    SimpleVolumePercentAlertItem.builder().triggerTime(22L).build());
            alertDataItem.setNetPercentChangeAlertItem(
                    NetPercentChangeAlertItem.builder().highPercent(22.0).build()
            );
            alertDAO.updateAlert(alertDataItem);
            Optional<AlertDataItem> dataItem = alertDAO.getAlert(alertDataItem);
            assertTrue(dataItem.isPresent());
            assertEquals(alertDataItem, dataItem.get());
        });

        alertDataItemList.forEach(alertDataItem -> {
            StockDataItem stockItem = (StockDataItem) LocalDDBServer.loadItemFromDB(new StockDataItem(alertDataItem.getTicker()));
            assertEquals(stockCountMap.get(stockItem.getTicker()), stockItem.getAlertCount());
        });
    }

    @Test
    public void testUpdate(){
        AlertDataItem alertDataItem = alertDataItemList.get(0);
        alertDataItem.setSimplePriceAlertItem(
                SimplePriceAlertItem.builder().highPrice(23.0).lowPrice(24.0).build());
        alertDataItem.setSimpleDailyPercentAlertItem(
                SimpleDailyPercentAlertItem.builder().lowPercent(22.0).build());
        alertDataItem.setSimpleVolumePercentAlertItem(
                SimpleVolumePercentAlertItem.builder().triggerTime(22L).build());
        alertDataItem.setNetPercentChangeAlertItem(
                NetPercentChangeAlertItem.builder().highPercent(22.0).build());

        alertDAO.updateAlert(alertDataItem);

        AlertDataItem item = AlertDataItem.builder(alertDataItem.getTicker(),
                alertDataItem.getUserWatchlistId().getUserId(),alertDataItem.getUserWatchlistId().getWatchListId()).build();

        item.setSimplePriceAlertItem(
                SimplePriceAlertItem.builder().triggerTime(1950L).build());

        AlertDataItem dataItem = (AlertDataItem) LocalDDBServer.loadItemFromDB(alertDataItem);
        assertNotNull(dataItem);

        StockDataItem stockItem = (StockDataItem) LocalDDBServer.loadItemFromDB(new StockDataItem(alertDataItem.getTicker()));
        assertEquals(new Long(1) , stockItem.getAlertCount());
    }


    @Test
    public void deleteStock() {
        putNewAlert();
        alertDataItemList.forEach(alertDataItem -> {
            alertDAO.deleteAlert(alertDataItem, false);
            long count = stockCountMap.get(alertDataItem.getTicker()) - 1;
            stockCountMap.put(alertDataItem.getTicker(), count);
            StockDataItem stockItem = (StockDataItem) LocalDDBServer.loadItemFromDB(new StockDataItem(alertDataItem.getTicker()));
            if (count == 0) {
                stockItem = (StockDataItem) LocalDDBServer.loadItemFromDB(new StockDataItem(alertDataItem.getTicker()));
                assertNull(stockItem);
            }else{
                assertEquals(new Long(count), stockItem.getAlertCount());
                assertNotNull(stockItem.getExchange());
                assertNotNull(stockItem.getPriority());
            }
        });
    }

    @Test
    public void deleteWatchlist() {
        Map<UserWatchlistId, List<String>> map = AlertData.getUserWatchListIdTickerMap(alertDataItemList);
        alertDataItemList.forEach(alertDataItem -> alertDAO.putNewAlert(alertDataItem, true));
        List<UserWatchlistId> userWatchlistIds = AlertData.getUserWatchIdList(alertDataItemList);

        userWatchlistIds.forEach(userWatchlistId -> {
            alertDAO.deleteWatchlist(userWatchlistId);
            List<String> tickerList = map.get(userWatchlistId);
            reduceTickerCountInMap(map.get(userWatchlistId));
            tickerList.forEach(ticker -> {
                StockDataItem dataItem = (StockDataItem) LocalDDBServer.loadItemFromDB(new StockDataItem(ticker));
                assertEquals(stockCountMap.get(ticker), dataItem == null ? dataItem : dataItem.getAlertCount());
                if(dataItem != null){
                    assertNotNull(dataItem.getExchange());
                    assertNotNull(dataItem.getPriority());
                }
            });
        });
    }

    private void reduceTickerCountInMap(List<String> tickerList) {
        for (String ticker : tickerList) {
            long count = stockCountMap.get(ticker) - 1L;
            stockCountMap.put(ticker, count);
            if (count == 0) {
                stockCountMap.remove(ticker);
            }
        }
    }

    @Test
    public void deleteUser() {
        alertDataItemList.forEach(alertDataItem -> alertDAO.putNewAlert(alertDataItem, true));

        String[] userList = {"tarun", "tar", "sachin"};

        for (String user : userList) {
            alertDAO.deleteUser(user);

            Map<String, List<String>> userIdTickerlistMap =
                    AlertData.getUserIdTickerMap(alertDataItemList);

            List<String> tickerList = userIdTickerlistMap.get(user);
            reduceTickerCountInMap(tickerList);

            tickerList.forEach(ticker -> {
                StockDataItem dataItem = (StockDataItem) LocalDDBServer.loadItemFromDB(new StockDataItem(ticker));
                assertEquals(stockCountMap.get(ticker), dataItem == null ? dataItem : dataItem.getAlertCount());
                if(dataItem != null){
                    assertNotNull(dataItem.getExchange());
                    assertNotNull(dataItem.getPriority());
                }
            });
        }
    }

    @Test
    public void Test_StockUpdateMethod() {
        alertDataItemList.forEach(alertDataItem -> alertDAO.putNewAlert(alertDataItem, true));
        StockDataItem dataItem = (StockDataItem) LocalDDBServer.loadItemFromDB(new StockDataItem("ADBE"));

        assertNotNull(dataItem);
        assertEquals(dataItem.getAlertCount(), new Long(3));

        dataItem.setAlertCount(4L);
        alertDAO.updateStock(dataItem);

        StockDataItem actualDataItem = (StockDataItem) LocalDDBServer.loadItemFromDB(new StockDataItem("ADBE"));
        assertNotNull(actualDataItem);
        assertEquals(actualDataItem.getAlertCount(), new Long(4));
    }

    @Test
    public void test_exceptionBehaviour() {
        alertDataItemList.forEach(alertDataItem -> alertDAO.putNewAlert(alertDataItem, true));
        StockDataItem dataItem = StockDataItem.builder(alertDataItemList.get(0).getTicker())
                .alertCount(1L)
                .exchange("DEF")
                .priority("P1")
                .build();
        LocalDDBServer.getDynamoDBMapper().save(dataItem);

        AlertDataItem alertDataItem = AlertDataItem.builder("ADBE", "tarun", "w1").build();
        StockDataItem stockItem = (StockDataItem) LocalDDBServer.loadItemFromDB(new StockDataItem(alertDataItem.getTicker()));

        alertDAO.deleteAlert(alertDataItem, false);
        long count = stockCountMap.get(alertDataItem.getTicker()) - 1;
        stockCountMap.put(alertDataItem.getTicker(), count);
        stockItem = (StockDataItem) LocalDDBServer.loadItemFromDB(new StockDataItem(alertDataItem.getTicker()));
        assertEquals(Long.valueOf(count), (stockItem.getAlertCount()));
        assertNotNull(stockItem.getExchange());
        assertNotNull(stockItem.getPriority());
    }

    @Test
    public void Test_exceptionBehaviour_ForWatchListDelete() {
        alertDataItemList.forEach(alertDataItem -> alertDAO.putNewAlert(alertDataItem, true));
        StockDataItem dataItem = StockDataItem.builder(alertDataItemList.get(0).getTicker())
                                                    .alertCount(1L)
                                                    .exchange("DEF")
                                                    .priority("P1")
                                                    .build();
        LocalDDBServer.getDynamoDBMapper().save(dataItem);

        AlertDataItem alertDataItem = AlertDataItem.builder("ADBE", "tarun", "w1").build();

        alertDAO.deleteWatchlist(new UserWatchlistId("tarun", "w1"));

        long count = stockCountMap.get(alertDataItem.getTicker()) - 1;
        stockCountMap.put(alertDataItem.getTicker(), count);
        StockDataItem stockItem = (StockDataItem) LocalDDBServer.loadItemFromDB(new StockDataItem(alertDataItem.getTicker()));
        assertEquals(Long.valueOf(count), (stockItem.getAlertCount()));
        assertNotNull(stockItem.getExchange());
        assertNotNull(stockItem.getPriority());
    }

    @Test
    public void Test_UpdateAlertTriggeredItem() {

        AlertDataItem alertDataItem = alertDataItemList.get(0);

        NetPercentChangeAlertItem netPercentChangeAlertItem = NetPercentChangeAlertItem.builder()
                .basePrice(30d)
                .highPercent(2d)
                .build();

        alertDataItem.setNetPercentChangeAlertItem(netPercentChangeAlertItem);

        alertDAO.putNewAlert(alertDataItem, true);

        AlertDataItem newAlertDataItem = alertDAO.getAlert(alertDataItem).get();

        assertEquals(newAlertDataItem, alertDataItem);

        assertNull(newAlertDataItem.getNetPercentChangeAlertItem().getTriggerTime());

        NetPercentChangeAlertItem netPercentChangeAlertItemWithTriggerTime = NetPercentChangeAlertItem.builder()
                .triggerTime(100L)
                .build();

        alertDataItem.setNetPercentChangeAlertItem(netPercentChangeAlertItemWithTriggerTime);

        alertDAO.updateAlertTriggerTimeOnly(alertDataItem);

        newAlertDataItem = alertDAO.getAlert(alertDataItem).get();

        assertEquals(newAlertDataItem.getNetPercentChangeAlertItem().getTriggerTime(), new Long(100L));

        alertDAO.deleteAlert(alertDataItem, false);

        alertDAO.updateAlertTriggerTimeOnly(alertDataItem);

    }
}