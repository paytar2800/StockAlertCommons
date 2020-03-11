package com.paytar2800.stockalertcommons.ddb.user;

import com.amazonaws.util.IOUtils;
import com.paytar2800.stockalertcommons.ddb.user.model.UserDataItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class UserData {

    private static final String USER_DATA_PATH = "/userdata.csv";

    static ArrayList<UserDataItem> getSampleData() {
        ArrayList<UserDataItem> list = new ArrayList<>();
        try (InputStream is = UserData.class.getResourceAsStream(USER_DATA_PATH)) {
            final String cellConfigStr = IOUtils.toString(is);
            final String[] lines = cellConfigStr.split("\n");

            for (String line : lines) {
                final String[] fields = line.split(",");

                String emailId = fields[0];
                String userId = fields[1];
                Integer snoozeTime = Integer.parseInt(fields[2]);
                Boolean isAlertEnabled = Boolean.parseBoolean(fields[3]);
                Integer subStatus = Integer.parseInt(fields[4]);
                String deviceToken = fields[5];

                UserDataItem dataItem = UserDataItem.builder()
                        .emailId(emailId)
                        .userId(userId)
                        .isAlertEnabled(isAlertEnabled)
                        .subscriptionStatus(subStatus)
                        .alertSnoozeTimeSeconds(snoozeTime)
                        .deviceToken(deviceToken)
                        .build();

                list.add(dataItem);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
