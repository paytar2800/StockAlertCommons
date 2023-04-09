package com.paytar2800.stockalertcommons.ddb.alert;

import com.amazonaws.util.IOUtils;
import com.paytar2800.stockalertcommons.ddb.alert.model.AlertDataItem;
import com.paytar2800.stockalertcommons.ddb.alert.model.SimplePriceAlertItem;
import com.paytar2800.stockalertcommons.ddb.alert.model.UserWatchlistId;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlertData {

    private static final String ALERTDATA_CSV = "/alertdata.csv";

    public static ArrayList<AlertDataItem> getSampleData() {
        ArrayList<AlertDataItem> list = new ArrayList<>();
        try (InputStream is = AlertData.class.getResourceAsStream(ALERTDATA_CSV)) {
            final String cellConfigStr = IOUtils.toString(is);
            final String[] lines = cellConfigStr.split("\n");

            for (String line : lines) {
                final String[] fields = line.split(",");

                String ticker = fields[0];
                String userId = fields[1];
                String watchlistid = fields[2];
                SimplePriceAlertItem simplePriceAlertItem = new SimplePriceAlertItem();
                simplePriceAlertItem.setHighPrice(Double.valueOf(fields[3]));
                simplePriceAlertItem.setLowPrice(Double.valueOf(fields[4]));


                AlertDataItem alertDataItem = AlertDataItem.builder(ticker, userId, watchlistid).build();
                alertDataItem.setSimplePriceAlertItem(simplePriceAlertItem);
                list.add(alertDataItem);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Map<String, Long> getStockCountMap(List<AlertDataItem> alertList) {
        Map<String, Long> map = new HashMap<>();

        for (AlertDataItem dataItem : alertList) {
            if (map.containsKey(dataItem.getTicker())) {
                map.put(dataItem.getTicker(), map.get(dataItem.getTicker()) + 1L);
            } else {
                map.put(dataItem.getTicker(), 1L);
            }
        }
        return map;
    }

    public static List<UserWatchlistId> getUserWatchIdList(List<AlertDataItem> alertList){
        List<UserWatchlistId> list = new ArrayList<>();
        for(AlertDataItem dataItem : alertList){
            if(!list.contains(dataItem.getUserWatchlistId())){
                list.add(dataItem.getUserWatchlistId());
            }
        }
        return list;
    }

    public static Map<UserWatchlistId, List<String>> getUserWatchListIdTickerMap(List<AlertDataItem> alertList) {
        Map<UserWatchlistId, List<String>> map = new HashMap<>();

        for (AlertDataItem dataItem : alertList) {
            List<String> list = map.get(dataItem.getUserWatchlistId());
            if(list == null){
                list = new ArrayList<>();
            }
            list.add(dataItem.getTicker());
            map.put(dataItem.getUserWatchlistId(),list);
        }
        return map;
    }

    public static Map<String, List<String>> getUserIdTickerMap(List<AlertDataItem> alertList) {
        Map<String, List<String>> map = new HashMap<>();

        for (AlertDataItem dataItem : alertList) {
            List<String> list = map.get(dataItem.getUserWatchlistId().getUserId());
            if(list == null){
                list = new ArrayList<>();
            }
            list.add(dataItem.getTicker());
            map.put(dataItem.getUserWatchlistId().getUserId(),list);
        }
        return map;
    }
}
