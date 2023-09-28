package com.paytar2800.stockalertcommons.ddb.user.model;


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.paytar2800.stockalertcommons.ddb.user.UserDDBConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.paytar2800.stockalertcommons.api.APIParamConstants.*;

/**
 * UserData Table Item
 * Table name = UserData
 * PrimaryKey = EmailID, Global SecondaryKey = UserId and Changed  for tracking changed items
 */

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = UserDDBConstants.TABLE_NAME)
public class UserDataItem {

    @SerializedName(API_USER_ID_PARAM)
    @DynamoDBHashKey(attributeName = UserDDBConstants.TABLE_USERID_KEY)
    private String userId;

    @SerializedName(API_EMAIL_ID_PARAM)
    @DynamoDBIndexHashKey(attributeName = UserDDBConstants.TABLE_EMAIL_KEY,
            globalSecondaryIndexName = UserDDBConstants.TABLE_EMAIL_GSI_KEY)
    private String emailId;

    @SerializedName(API_ALERTSNOOZETIME_PARAM)
    @DynamoDBAttribute(attributeName = UserDDBConstants.TABLE_ALERTSNOOZETIME_KEY)
    private Integer alertSnoozeTimeSeconds;

    @SerializedName(API_ISALERTENABLED_PARAM)
    @DynamoDBAttribute(attributeName = UserDDBConstants.TABLE_ISALERTENABLED_KEY)
    private Boolean isAlertEnabled;

    @SerializedName(API_ISEXTENDEDHOURSENABLED_PARAM)
    @DynamoDBAttribute(attributeName = UserDDBConstants.TABLE_ISEXTENDEDHOURSENABLED_KEY)
    private Boolean isExtendedAlertsEnabled;

    @SerializedName(API_SUB_STATUS_PARAM)
    @DynamoDBAttribute(attributeName = UserDDBConstants.TABLE_SUB_STATUS_KEY)
    private Integer subscriptionStatus;

    @SerializedName(API_DEVICE_TOKEN)
    @DynamoDBAttribute(attributeName = UserDDBConstants.TABLE_DEVICE_TOKEN_KEY)
    private String deviceToken;

    @SerializedName(API_ALERT_SOUND)
    @DynamoDBAttribute(attributeName = UserDDBConstants.TABLE_ALERT_SOUND)
    private String alertSound;

    @SerializedName(API_DEVICE_OS)
    @DynamoDBAttribute(attributeName = UserDDBConstants.TABLE_DEVICE_OS)
    private String deviceOS;

    @SerializedName(API_LOCALE)
    @DynamoDBAttribute(attributeName = UserDDBConstants.TABLE_DEVICE_LOCALE)
    private String locale;

    @Expose(serialize = false, deserialize = false)
    @DynamoDBIndexHashKey(attributeName = UserDDBConstants.TABLE_HAS_CHANGED_KEY,
            globalSecondaryIndexName = UserDDBConstants.TABLE_HAS_CHANGED_GSI_KEY)
    private Boolean hasChanged;

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
