package com.paytar2800.stockalertcommons.ddb;

import lombok.SneakyThrows;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StockUtils {

    @SneakyThrows
    public static Date getTodayDate() {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date today = new Date();
        return formatter.parse(formatter.format(today));
    }

}
