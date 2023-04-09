package com.paytar2800.stockalertcommons.ddb.user.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.paytar2800.stockalertcommons.ddb.user.UserDDBConstants;

import java.lang.reflect.Field;

@DynamoDBTable(tableName = UserDDBConstants.USER_DELETED_DATA_TABLE_NAME)
public class UserDataItem_DeletedData extends UserDataItem {

    public UserDataItem_DeletedData() {}

    public UserDataItem_DeletedData(UserDataItem userDataItem) {
        Field[] fields = userDataItem.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                field.set(this, field.get(userDataItem));
            } catch (IllegalAccessException e) {
                // handle exception
            }
        }
    }
}
