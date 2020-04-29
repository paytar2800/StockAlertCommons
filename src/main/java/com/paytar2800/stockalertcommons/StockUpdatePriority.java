package com.paytar2800.stockalertcommons;

/*
 * StockPriority with time delay in minutes between Stock price update
 *
 * */

import java.util.Arrays;
import java.util.List;

public enum StockUpdatePriority {

    //MC = Stands for Market Close
    P1(0.5),
    P2(1),
    P3(5),
    P4(10),
    H(60),
    MC(960);

    private static StockUpdatePriority[] priorityArray = {P1};

    private double maxTimeDelayInMin;

    StockUpdatePriority(double v) {
        this.maxTimeDelayInMin = v;
    }

    public double getMaxTimeDelayInMin(){
        return maxTimeDelayInMin;
    }

    public StockUpdatePriority getHigherPriority(StockUpdatePriority priority1, StockUpdatePriority priority2) {
        return priority1.maxTimeDelayInMin < priority2.maxTimeDelayInMin ? priority1:priority2;
    }

    public boolean isPriorityLowerThan(StockUpdatePriority priority) {
        return maxTimeDelayInMin > priority.maxTimeDelayInMin;
    }

    public static List<StockUpdatePriority> getRunningPriorityList(){
        return Arrays.asList(priorityArray);
    }

}
