package com.paytar2800.stockalertcommons;

import java.util.UUID;

public class UserIdGenerator {

    public static String generateUserId(String emailId) {
        return UUID.randomUUID().toString();
    }

}
