package com.paytar2800.stockalertcommons.ddb.util;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class ObjectComparator {

    public static boolean areEqualIgnoringFields(Object obj1, Object obj2, String... fieldsToIgnore) {
        if (obj1 == null || obj2 == null) {
            return obj1 == obj2;
        }

        if (!obj1.getClass().equals(obj2.getClass())) {
            return false;
        }

        Set<String> ignoreSet = new HashSet<>();
        for (String field : fieldsToIgnore) {
            ignoreSet.add(field);
        }

        for (Field field : obj1.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            if (ignoreSet.contains(field.getName())) {
                continue;
            }

            try {
                Object value1 = field.get(obj1);
                Object value2 = field.get(obj2);

                if (value1 == null) {
                    if (value2 != null) {
                        return false;
                    }
                } else if (!value1.equals(value2)) {
                    return false;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }
}
