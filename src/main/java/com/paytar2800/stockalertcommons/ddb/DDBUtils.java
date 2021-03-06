package com.paytar2800.stockalertcommons.ddb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public class DDBUtils {

    private static DynamoDBMapper dynamoDBMapper;
    private static CustomDynamoDBMapper customDynamoDBMapper;
    /*
     * Gets default DB mapper which uses system credentials for executing DDB work
     */
    public static DynamoDBMapper getDynamoDBMapper() {
        if (dynamoDBMapper == null) {
            AmazonDynamoDB client = AmazonDynamoDBClientBuilder
                    .standard()
                    .build();
            dynamoDBMapper = new DynamoDBMapper(client);
        }
        return dynamoDBMapper;
    }


    /*
     * Gets Custom DB mapper made for transactions which uses system credentials for executing DDB work
     */
    public static CustomDynamoDBMapper getCustomDynamoDBMapper() {
        if(customDynamoDBMapper == null){
            AmazonDynamoDB client = AmazonDynamoDBClientBuilder
                    .standard()
                    .build();
            customDynamoDBMapper = new CustomDynamoDBMapper(client);
        }
        return customDynamoDBMapper;
    }

}
