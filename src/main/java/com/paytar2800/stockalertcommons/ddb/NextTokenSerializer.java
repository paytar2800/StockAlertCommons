package com.paytar2800.stockalertcommons.ddb;


import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytar2800.stockalertcommons.exceptions.DDBTokenSerialException;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton Class : use getInstance()
 * Pagination Token serializer for DDB queries
 * Jackson JSON processor is used to serialize and deserialize the data received in terms of Map of attributes
 */
public final class NextTokenSerializer {

    private static volatile NextTokenSerializer instance;

    /*
     *Use to get the instance of NextTokenSerializer
     */
    public static NextTokenSerializer getInstance() {
        if (instance == null) {
            synchronized (NextTokenSerializer.class) {
                if (instance == null) {
                    instance = new NextTokenSerializer();
                }
            }
        }
        return instance;
    }

    private NextTokenSerializer() {
    }

    /**
     * Prototype object for serializing the map.
     */
    private static Map<String, String> simpleStringMapProtoType = new HashMap<String, String>();

    /**
     * Jackson Mapper object for serializing and deserializing
     */
    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    /**
     * Given a <i>last-evaluated-key</i> from the underlying query, convert it to an opaque token.
     *
     * @param lastKey The last-evaluated-key resulting from a query operation or {@code null}.
     * @return {@code String} opaque token or {@code null} that will be passed in the API as a
     * {@code NextToken} in a {@code PaginagedResult<T>}.
     */
    public String serializeLastEvaluatedKey(final Map<String, AttributeValue> lastKey) {
        if (lastKey == null || lastKey.isEmpty()) {
            return null;
        }
        return serialize(lastKey);
    }

    /**
     * Given an opaque {@code NextToken}, extract from it an {@code exclusive-start-key}.
     *
     * @param nextToken - May be {@code null} or an opaque token {@code String}.
     * @return {@code Map<String,AttributeValue>} instance or {@code null}.
     */
    public Map<String, AttributeValue> deserializeExclusiveStartKey(final String nextToken) {
        if (nextToken == null || nextToken.isEmpty()) {
            return null;
        }
        return deserialize(nextToken);
    }

    /**
     * Serialize the last-evaluated key to be used in a {@code NextToken}.
     *
     * @param key - Instance of {@code Map<String,AttributeValue>} to be eventually used for a {@code NextToken}.
     * @return {@code String} representation of the token.
     * Note: First the Map<String,AttributeValue>} is converted to simple String map -
     * Map<String,String> for serializing
     */

    private String serialize(final Map<String, AttributeValue> key) {
        Map<String, String> value = convertAttributeMaptoSimpleStringMap(key);
        try {
            String jsonStr = OBJECT_MAPPER.writeValueAsString(value);
            return jsonStr;

        } catch (final Exception ex) {
            final String msg = "could not serialize key: " + key + " ,exception:" + ex.getMessage();
            throw new DDBTokenSerialException(msg);
        }
    }

    /**
     * De-serialize a {@code String} presentation of the exclusive-start-key for a query into a
     * {@code Map<String,AttributeValue>} instances.
     *
     * @param data The {@code String} representation.
     * @return Instance of {@code Map<String,AttributeValue>}
     * Note: Since the serialized object is simple string map we need to convert back to AttributeValue Map
     */
    private Map<String, AttributeValue> deserialize(final String data) {

        try {
            Map<String, String> interim = OBJECT_MAPPER.readValue(data, simpleStringMapProtoType.getClass());
            return convertSimpleStringMapToAttributeMap(interim);

        } catch (Exception e) {
            String msg = "could not deserialize data: " + data + " ,exception: " + e.getMessage();
            throw new DDBTokenSerialException(msg);
        }
    }

    /**
     * Method to convert the AttributeValue map to simple string map for smaller string token
     *
     * @param attributeValueMap Attribute value map received from DDB as lastEvaluatedKey
     * @return Map<String, String> Simple String map used for serializing the LastEvaluatedKey data
     */
    private Map<String, String> convertAttributeMaptoSimpleStringMap(Map<String, AttributeValue> attributeValueMap) {
        HashMap<String, String> value = new HashMap<String, String>();

        for (final Map.Entry<String, AttributeValue> entry : attributeValueMap.entrySet()) {
            value.put(entry.getKey(), entry.getValue().getS());
        }
        return value;
    }

    /**
     * Method to convert the simple string map to AttributeValue map since string map was serialized.
     *
     * @param map SimpleString map received on deserializing the next token string
     * @return Map<String, AttributeValue> Actual LastEvaluatedKey which needs to be passed to DDB for next results page
     */
    private Map<String, AttributeValue> convertSimpleStringMapToAttributeMap(Map<String, String> map) {
        HashMap<String, AttributeValue> result = new HashMap<String, AttributeValue>();
        for (final Map.Entry<String, String> entry : map.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            final AttributeValue attributeValue = new AttributeValue().withS(value);
            result.put(key, attributeValue);
        }
        return result;
    }
}
