package com.paytar2800.stockalertcommons.ddb.alert.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.File;

import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_USER_ID_PARAM;
import static com.paytar2800.stockalertcommons.api.APIParamConstants.API_WATCHLIST_ID_PARAM;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class UserWatchlistId implements DynamoDBTypeConverter<String, UserWatchlistId> {

    private static final String SEPARATOR = File.separator;

    @SerializedName(API_USER_ID_PARAM)
    private String userId;

    @SerializedName(API_WATCHLIST_ID_PARAM)
    private String watchListId;

    @Override
    public String convert(UserWatchlistId object) {
        return object.toString();
    }

    @Override
    public UserWatchlistId unconvert(String object) {
        String[] splitArr = object.split(SEPARATOR, 2);
        if (splitArr.length < 2) {
            return null;
        }
        return new UserWatchlistId(splitArr[0], splitArr[1]);
    }

    @Override
    public String toString() {
        return userId + SEPARATOR + watchListId;
    }

    public static String getSeparator() {
        return SEPARATOR;
    }
}
