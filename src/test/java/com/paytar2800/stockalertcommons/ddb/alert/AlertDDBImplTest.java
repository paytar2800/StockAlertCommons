package com.paytar2800.stockalertcommons.ddb.alert;

import com.paytar2800.stockalertcommons.ddb.alert.model.*;
import com.paytar2800.stockalertcommons.ddb.stock.model.StockDataItem;
import com.paytar2800.stockalertcommons.ddb.util.LocalDDBServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class AlertDDBImplTest {

    AlertDAO alertDAO;
    List<AlertDataItem> alertDataItemList;
    Map<String, Long> stockCountMap;

    @Before
    public void before() throws Exception {
        LocalDDBServer.startServer();
        LocalDDBServer.createTable(AlertDataItem.class);
        LocalDDBServer.createTable(StockDataItem.class);
        LocalDDBServer.createTable(AlertDataItem_DeletedData.class);
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
           alertDataItemList.forEach(alertDataItem -> {
                    alertDataItem.setExchange("DEF");
                    alertDAO.putNewAlert(alertDataItem, true);
                });
           alertDataItemList.forEach(alertDataItem -> {
            alertDataItem.setExchange("NSE");
            alertDAO.putNewAlert(alertDataItem, true);
          });
           alertDataItemList.forEach(alertDataItem -> {
            StockDataItem stockItem = (StockDataItem) LocalDDBServer.loadItemFromDB(new StockDataItem(alertDataItem.getTicker()));
            assertEquals(stockCountMap.get(stockItem.getTicker()), stockItem.getAlertCount());
        });
    }

    @Test
    public void test_copyUserDataToDeletedDataTable() {
        alertDataItemList.forEach(alertDataItem -> alertDAO.putNewAlert(alertDataItem, true));

        alertDAO.copyBatchAlertDataToDeletedDataTable(alertDataItemList);

        alertDataItemList.forEach(alertDataItem -> {
            AlertDataItem itemFromDB = (AlertDataItem) LocalDDBServer.loadItemFromDB(alertDataItem);
            AlertDataItem_DeletedData itemFromDb2 =  (AlertDataItem_DeletedData)
                    LocalDDBServer.loadItemFromDB(new AlertDataItem_DeletedData(itemFromDB));
            Assert.assertTrue(new ReflectionEquals(itemFromDb2).matches(itemFromDB));
        });
    }

    @Test
    public void updateAlert() {
        alertDataItemList.forEach(alertDataItem -> {
            alertDataItem.setSimplePriceAlertItem(
                    SimplePriceAlertItem.builder().highPrice(23.0).lowPrice(24.0).build());
            alertDataItem.setRecurringPriceAlertItem(
                    RecurringPriceAlertItem.builder().highPrice(23.0).lowPrice(24.0)
                            .recurFactor(10.0).lastTriggerPrice(22.0).build());
            alertDataItem.setSimpleDailyPercentAlertItem(
                    SimpleDailyPercentAlertItem.builder().lowPercent(22.0).build());
            alertDataItem.setSimpleVolumePercentAlertItem(
                    SimpleVolumePercentAlertItem.builder().triggerTime(22L).build());
            alertDataItem.setNetPercentChangeAlertItem(
                    NetPercentChangeAlertItem.builder().highPercent(22.0).build());

            alertDataItem.setDividendAlertItem(DividendAlertItem.builder().days(7).build());
            alertDataItem.setEarningsAlertItem(EarningsAlertItem.builder().days(14).build());
            alertDataItem.setFiftyDayAvgAlertItem(FiftyDayAvgAlertItem.builder()
                    .high(13.0).low(14.0).triggerTime(22L).build());
            alertDataItem.setTwoHundredDayAvgAlertItem(TwoHundredDayAvgAlertItem.builder()
                    .high(19.0).low(21.0).build());
            alertDataItem.setPegRatioAlertItem(PegRatioAlertItem.builder()
                    .high(34.0).low(23.0).build());
            alertDataItem.setShortPercentFloatAlertItem(ShortPercentFloatAlertItem.builder()
                    .low(11.5).high(45.5).build());
            alertDataItem.setShortRatioAlertItem(ShortRatioAlertItem.builder()
                    .high(1.2).low(3.4).build());
            alertDataItem.setTrailingPEAlertItem(TrailingPEAlertItem.builder()
                    .high(3.4).low(1.9).build());
            alertDataItem.setForwardPEAlertItem(ForwardPEAlertItem.builder()
                    .high(9.9).low(1.3).build());

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
    public void testUpdateBatchList() {
        List<AlertDataItem> alertDataItemBatchList = new ArrayList<>();
        alertDataItemList.forEach(alertDataItem -> {
            alertDataItem.setSimplePriceAlertItem(
                    SimplePriceAlertItem.builder().highPrice(23.0).lowPrice(24.0).build());
            alertDataItem.setRecurringPriceAlertItem(
                    RecurringPriceAlertItem.builder().highPrice(23.0).lowPrice(24.0)
                            .recurFactor(10.0).lastTriggerPrice(22.0).build());
            alertDataItem.setSimpleDailyPercentAlertItem(
                    SimpleDailyPercentAlertItem.builder().lowPercent(22.0).build());
            alertDataItem.setSimpleVolumePercentAlertItem(
                    SimpleVolumePercentAlertItem.builder().triggerTime(22L).build());
            alertDataItem.setNetPercentChangeAlertItem(
                    NetPercentChangeAlertItem.builder().highPercent(22.0).build()
            );
            alertDataItem.setDividendAlertItem(DividendAlertItem.builder().days(7).build());
            alertDataItem.setEarningsAlertItem(EarningsAlertItem.builder().days(14).build());
            alertDataItem.setFiftyDayAvgAlertItem(FiftyDayAvgAlertItem.builder()
                    .high(13.0).low(14.0).triggerTime(22L).build());
            alertDataItem.setTwoHundredDayAvgAlertItem(TwoHundredDayAvgAlertItem.builder()
                    .high(19.0).low(21.0).build());
            alertDataItem.setPegRatioAlertItem(PegRatioAlertItem.builder()
                    .high(34.0).low(23.0).build());
            alertDataItem.setShortPercentFloatAlertItem(ShortPercentFloatAlertItem.builder()
                    .low(11.5).high(45.5).build());
            alertDataItem.setShortRatioAlertItem(ShortRatioAlertItem.builder()
                    .high(1.2).low(3.4).build());
            alertDataItem.setTrailingPEAlertItem(TrailingPEAlertItem.builder()
                    .high(3.4).low(1.9).build());
            alertDataItem.setForwardPEAlertItem(ForwardPEAlertItem.builder()
                    .high(9.9).low(1.3).build());

            alertDataItemBatchList.add(alertDataItem);
        });

        alertDataItemList.forEach(alertDataItem -> alertDAO.putNewAlert(alertDataItem, true));
        alertDAO.updateBatchAlerts(alertDataItemBatchList);

        alertDataItemList.forEach(alertDataItem -> {
            StockDataItem stockItem = (StockDataItem) LocalDDBServer.loadItemFromDB(new StockDataItem(alertDataItem.getTicker()));
            assertEquals(stockCountMap.get(stockItem.getTicker()), stockItem.getAlertCount());
        });
    }

    @Test
    public void testUpdate() {
        AlertDataItem alertDataItem = alertDataItemList.get(0);
        alertDataItem.setSimplePriceAlertItem(
                SimplePriceAlertItem.builder().highPrice(23.0).lowPrice(24.0).build());
        alertDataItem.setRecurringPriceAlertItem(
                RecurringPriceAlertItem.builder().highPrice(23.0).lowPrice(24.0)
                        .recurFactor(10.0).lastTriggerPrice(22.0).build());
        alertDataItem.setSimpleDailyPercentAlertItem(
                SimpleDailyPercentAlertItem.builder().lowPercent(22.0).build());
        alertDataItem.setSimpleVolumePercentAlertItem(
                SimpleVolumePercentAlertItem.builder().triggerTime(22L).build());
        alertDataItem.setNetPercentChangeAlertItem(
                NetPercentChangeAlertItem.builder().highPercent(22.0).build());

        alertDataItem.setDividendAlertItem(DividendAlertItem.builder().days(7).build());
        alertDataItem.setEarningsAlertItem(EarningsAlertItem.builder().days(14).build());
        alertDataItem.setFiftyDayAvgAlertItem(FiftyDayAvgAlertItem.builder()
                .high(13.0).low(14.0).triggerTime(22L).build());
        alertDataItem.setTwoHundredDayAvgAlertItem(TwoHundredDayAvgAlertItem.builder()
                .high(19.0).low(21.0).build());
        alertDataItem.setPegRatioAlertItem(PegRatioAlertItem.builder()
                .high(34.0).low(23.0).build());
        alertDataItem.setShortPercentFloatAlertItem(ShortPercentFloatAlertItem.builder()
                .low(11.5).high(45.5).build());
        alertDataItem.setShortRatioAlertItem(ShortRatioAlertItem.builder()
                .high(1.2).low(3.4).build());
        alertDataItem.setTrailingPEAlertItem(TrailingPEAlertItem.builder()
                .high(3.4).low(1.9).build());
        alertDataItem.setForwardPEAlertItem(ForwardPEAlertItem.builder()
                .high(9.9).low(1.3).build());

        alertDAO.updateAlert(alertDataItem);

        AlertDataItem item = AlertDataItem.builder(alertDataItem.getTicker(),
                alertDataItem.getUserWatchlistId().getUserId(), alertDataItem.getUserWatchlistId().getWatchListId()).build();

        item.setSimplePriceAlertItem(
                SimplePriceAlertItem.builder().triggerTime(1950L).build());

        AlertDataItem dataItem = (AlertDataItem) LocalDDBServer.loadItemFromDB(alertDataItem);
        assertNotNull(dataItem);

        StockDataItem stockItem = (StockDataItem) LocalDDBServer.loadItemFromDB(new StockDataItem(alertDataItem.getTicker()));
        assertEquals(Long.valueOf(1), stockItem.getAlertCount());
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
            } else {
                assertEquals(Long.valueOf(count), stockItem.getAlertCount());
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
                if (dataItem != null) {
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
                if (dataItem != null) {
                    assertNotNull(dataItem.getExchange());
                    assertNotNull(dataItem.getPriority());
                }
            });
        }

        alertDataItemList.forEach(alertDataItem -> {
            AlertDataItem_DeletedData alertDataItemDeletedData = (AlertDataItem_DeletedData) LocalDDBServer.loadItemFromDB(
                    new AlertDataItem_DeletedData(alertDataItem));

            Assert.assertTrue(new ReflectionEquals(alertDataItem).matches(alertDataItemDeletedData));
        });
    }

    @Test
    public void Test_StockUpdateMethod() {
        alertDataItemList.forEach(alertDataItem -> alertDAO.putNewAlert(alertDataItem, true));
        StockDataItem dataItem = (StockDataItem) LocalDDBServer.loadItemFromDB(new StockDataItem("ADBE"));

        assertNotNull(dataItem);
        assertEquals(dataItem.getAlertCount(), Long.valueOf(3));

        dataItem.setAlertCount(4L);
        alertDAO.updateStock(dataItem);

        StockDataItem actualDataItem = (StockDataItem) LocalDDBServer.loadItemFromDB(new StockDataItem("ADBE"));
        assertNotNull(actualDataItem);
        assertEquals(actualDataItem.getAlertCount(), Long.valueOf(4));
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

        assertEquals(newAlertDataItem.getNetPercentChangeAlertItem().getTriggerTime(), Long.valueOf(100L));

        alertDAO.deleteAlert(alertDataItem, false);

        alertDAO.updateAlertTriggerTimeOnly(alertDataItem);
    }

    @Test
    public void Test_UpdatedAlertItem() {
        /*alertDataItemList.get(0).setHasChanged(true);
        alertDataItemList.get(1).setHasChanged(true);

        alertDataItemList.forEach(alertDataItem -> alertDAO.putNewAlert(alertDataItem, true));

        PaginatedItem<AlertDataItem, String> paginatedItem = alertDAO.getLatestUpdatedUsers(null, null);

        assertFalse(paginatedItem.getCurrentItemList().isEmpty());
        assertEquals(paginatedItem.getCurrentItemList().size(), 2);
        assertEquals(paginatedItem.getCurrentItemList().get(0), alertDataItemList.get(0));
        assertEquals(paginatedItem.getCurrentItemList().get(1), alertDataItemList.get(1));*/
    }
}