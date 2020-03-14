package com.paytar2800.stockalertcommons.ddb.stock;

import com.amazonaws.util.IOUtils;
import com.paytar2800.stockalertcommons.ddb.alert.AlertData;
import com.paytar2800.stockalertcommons.ddb.stock.model.StockDataItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class StockData {

    private static final String STOCKDATA_CSV = "/stockdata.csv";

    public static ArrayList<StockDataItem> getSampleData() {
        ArrayList<StockDataItem> list = new ArrayList<>();
        try (InputStream is = AlertData.class.getResourceAsStream(STOCKDATA_CSV)) {
            final String cellConfigStr = IOUtils.toString(is);
            final String[] lines = cellConfigStr.split("\n");

            for (String line : lines) {
                final String[] fields = line.split(",");

                String ticker = fields[0];
                String priority = fields[1];
                Long alertCount = Long.parseLong(fields[2]);
                String exchange = fields[3];

                StockDataItem dataItem = StockDataItem.builder(ticker)
                        .priority(priority).alertCount(alertCount).exchange(exchange).build();
                list.add(dataItem);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

}
