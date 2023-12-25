package com.paytar2800.stockalertcommons.ddb.user.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.google.gson.annotations.Expose;
import com.paytar2800.stockalertcommons.ddb.user.UserDDBConstants;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.Date;

@Setter
@Getter
@DynamoDBTable(tableName = UserDDBConstants.USER_DELETED_DATA_TABLE_NAME)
public class UserDataItem_DeletedData extends UserDataItem {

    public UserDataItem_DeletedData() {}

    /**
     * Used to signify if the user was delted by me manually in case where the lastactive date was beyond a threshold
      */
    @Expose(serialize = false, deserialize = false)
    @DynamoDBAttribute(attributeName = UserDDBConstants.TABLE_IS_USER_DELETED_MANUALLY_KEY)
    private Boolean isUserDeletedManually;

    @Expose(serialize = false, deserialize = false)
    @DynamoDBAttribute(attributeName = UserDDBConstants.TABLE_USER_DELETE_DATE_KEY)
    private Date deleteDate;

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
