package ddb.util;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.paytar2800.stockalertcommons.ddb.CustomDynamoDBMapper;

import java.io.File;
import java.io.FileNotFoundException;

public class LocalDDBServer {

    private static final String SERVICE_ENDPOINT = "http://localhost:8000/";
    private static final String SIGNING_REGION = Regions.US_WEST_2.getName();

    private static DynamoDBProxyServer dynamoDBProxyServer;
    private static DynamoDBMapper dynamoDBMapper;
    private static AmazonDynamoDB dynamoDB;

    private static final ProvisionedThroughput THROUGHPUT = new ProvisionedThroughput()
            .withReadCapacityUnits(5L).withWriteCapacityUnits(5L);

    public static void startServer() throws Exception {
        //Need to set the SQLite4Java library path to avoid a linker error

        File file = new File("./DDBlocal_lib");

        if(!file.exists()){
            throw new FileNotFoundException("DDB Library does not exists, download dynamodb_local jars first");
        }

        System.setProperty("sqlite4java.library.path", "./DDBlocal_lib");

        // Create an in-memory and in-process instance of DynamoDB Local that runs over HTTP
        final String[] localArgs = {"-inMemory"};
        dynamoDBProxyServer = ServerRunner.createServerFromCommandLineArgs(localArgs);
        dynamoDBProxyServer.start();
    }

    public static void stopServer() throws Exception {
        if (dynamoDBProxyServer != null) {
            dynamoDBProxyServer.stop();
        }
    }

    public static DynamoDBMapper getDynamoDBMapper() {
        if (dynamoDBMapper == null) {
            connectToDB();
        }
        return dynamoDBMapper;
    }

    public static CustomDynamoDBMapper getCustomMapper(){
        return new CustomDynamoDBMapper(dynamoDB);
    }

    private static void connectToDB() {
        dynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(SERVICE_ENDPOINT, SIGNING_REGION))
                .build();
        dynamoDBMapper = new DynamoDBMapper(dynamoDB);
    }

    public static void createTable(Class<?> clazz) {
        CreateTableRequest createTableRequest = getDynamoDBMapper().generateCreateTableRequest(clazz);
        if (createTableRequest.getGlobalSecondaryIndexes() != null) {
            createTableRequest.getGlobalSecondaryIndexes().forEach(v -> v.setProvisionedThroughput(THROUGHPUT));
        }
        createTableRequest.setProvisionedThroughput(THROUGHPUT);
        dynamoDB.createTable(createTableRequest);
    }

    public static void addItemToDB(Object dataItem) {
        getDynamoDBMapper().save(dataItem);
    }

    public static Object loadItemFromDB(Object dataItem) {
        return getDynamoDBMapper().load(dataItem);
    }

    public static void saveItemToDB(Object dataItem){
        getDynamoDBMapper().save(dataItem);
    }
    public static String getServiceEndpoint() {
        return SERVICE_ENDPOINT;
    }

    public static String getSigningRegion() {
        return SIGNING_REGION;
    }
}
