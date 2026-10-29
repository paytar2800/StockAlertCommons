package com.paytar2800.stockalertcommons.ddb.userearnings;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.paytar2800.stockalertcommons.ddb.userearnings.model.UserEarningsDataItem;
import com.paytar2800.stockalertcommons.ddb.util.LocalDDBServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UserEarningsDDBImplTest {
    private UserEarningsDAO userEarningsDAO;

    @Before
    public void setUp() throws Exception {
        LocalDDBServer.startServer();
        LocalDDBServer.createTable(UserEarningsDataItem.class);
        userEarningsDAO = new UserEarningsDDBImpl(LocalDDBServer.getDynamoDBMapper());
    }

    @After
    public void tearDown() throws Exception {
        LocalDDBServer.stopServer();
        userEarningsDAO = null;
    }

    @Test
    public void testSaveAndGetUserEarnings() {
        UserEarningsDataItem item = UserEarningsDataItem.builder()
                .userId("user1")
                .customTickers(Arrays.asList("PLTR", "SHOP", "SNOW"))
                .hiddenTickers(Arrays.asList("AAPL"))
                .build();

        userEarningsDAO.saveUserEarnings(item);

        assertNotNull(item.getLastUpdateTime());
        assertTrue(item.getLastUpdateTime() > 0);

        Optional<UserEarningsDataItem> fetched = userEarningsDAO.getUserEarnings("user1");
        assertTrue(fetched.isPresent());
        assertEquals("user1", fetched.get().getUserId());
        assertEquals(Arrays.asList("PLTR", "SHOP", "SNOW"), fetched.get().getCustomTickers());
        assertEquals(Arrays.asList("AAPL"), fetched.get().getHiddenTickers());
        assertEquals(item.getLastUpdateTime(), fetched.get().getLastUpdateTime());
    }

    @Test
    public void testGetMissingUserReturnsEmpty() {
        assertFalse(userEarningsDAO.getUserEarnings("nobody").isPresent());
    }

    @Test
    public void testCustomTickerOrderIsPreserved() {
        List<String> ordered = Arrays.asList("ZM", "AAPL", "MSFT", "AMD", "BA");
        userEarningsDAO.saveUserEarnings(UserEarningsDataItem.builder()
                .userId("user_order").customTickers(ordered).hiddenTickers(new ArrayList<>()).build());

        assertEquals(ordered, userEarningsDAO.getUserEarnings("user_order").get().getCustomTickers());
    }

    @Test
    public void testSaveIsAFullReplace() {
        userEarningsDAO.saveUserEarnings(UserEarningsDataItem.builder()
                .userId("user_replace")
                .customTickers(Arrays.asList("PLTR", "SHOP"))
                .hiddenTickers(Arrays.asList("AAPL", "MSFT"))
                .build());

        userEarningsDAO.saveUserEarnings(UserEarningsDataItem.builder()
                .userId("user_replace")
                .customTickers(Arrays.asList("SNOW"))
                .hiddenTickers(Collections.<String>emptyList())
                .build());

        UserEarningsDataItem fetched = userEarningsDAO.getUserEarnings("user_replace").get();
        assertEquals(Arrays.asList("SNOW"), fetched.getCustomTickers());
        assertTrue(fetched.getHiddenTickers().isEmpty());
    }

    @Test
    public void testEmptyListsArePersistedAsEmptyNotMissing() {
        userEarningsDAO.saveUserEarnings(UserEarningsDataItem.builder()
                .userId("user_empty")
                .customTickers(new ArrayList<>())
                .hiddenTickers(new ArrayList<>())
                .build());

        UserEarningsDataItem fetched = userEarningsDAO.getUserEarnings("user_empty").get();
        assertNotNull(fetched.getCustomTickers());
        assertNotNull(fetched.getHiddenTickers());
        assertTrue(fetched.getCustomTickers().isEmpty());
        assertTrue(fetched.getHiddenTickers().isEmpty());
        assertTrue(fetched.getLastUpdateTime() > 0);
    }

    @Test
    public void testNullListsDoNotLeaveOldValuesBehind() {
        userEarningsDAO.saveUserEarnings(UserEarningsDataItem.builder()
                .userId("user_null")
                .customTickers(Arrays.asList("PLTR"))
                .hiddenTickers(Arrays.asList("AAPL"))
                .build());

        userEarningsDAO.saveUserEarnings(UserEarningsDataItem.builder().userId("user_null").build());

        UserEarningsDataItem fetched = userEarningsDAO.getUserEarnings("user_null").get();
        assertTrue(fetched.getCustomTickers().isEmpty());
        assertTrue(fetched.getHiddenTickers().isEmpty());
    }

    @Test
    public void testUsersAreIsolated() {
        userEarningsDAO.saveUserEarnings(UserEarningsDataItem.builder()
                .userId("userA").customTickers(Arrays.asList("PLTR")).hiddenTickers(new ArrayList<>()).build());
        userEarningsDAO.saveUserEarnings(UserEarningsDataItem.builder()
                .userId("userB").customTickers(Arrays.asList("SHOP")).hiddenTickers(new ArrayList<>()).build());

        assertEquals(Arrays.asList("PLTR"), userEarningsDAO.getUserEarnings("userA").get().getCustomTickers());
        assertEquals(Arrays.asList("SHOP"), userEarningsDAO.getUserEarnings("userB").get().getCustomTickers());
    }

    @Test
    public void testDeleteUserEarnings() {
        userEarningsDAO.saveUserEarnings(UserEarningsDataItem.builder()
                .userId("user_del").customTickers(Arrays.asList("PLTR")).hiddenTickers(new ArrayList<>()).build());
        userEarningsDAO.saveUserEarnings(UserEarningsDataItem.builder()
                .userId("user_keep").customTickers(Arrays.asList("SHOP")).hiddenTickers(new ArrayList<>()).build());

        userEarningsDAO.deleteUserEarnings("user_del");

        assertFalse(userEarningsDAO.getUserEarnings("user_del").isPresent());
        assertTrue(userEarningsDAO.getUserEarnings("user_keep").isPresent());
    }

    @Test
    public void testDeleteMissingUserIsANoOp() {
        userEarningsDAO.deleteUserEarnings("never_existed");
        assertFalse(userEarningsDAO.getUserEarnings("never_existed").isPresent());
    }

    @Test
    public void testJsonUsesApiParamNames() {
        UserEarningsDataItem item = UserEarningsDataItem.builder()
                .userId("user_json")
                .customTickers(Arrays.asList("PLTR"))
                .hiddenTickers(Arrays.asList("AAPL"))
                .lastUpdateTime(1234L)
                .build();

        JsonObject json = new JsonParser().parse(item.toJson()).getAsJsonObject();

        assertEquals("user_json", json.get("userId").getAsString());
        assertEquals("PLTR", json.get("customTickers").getAsJsonArray().get(0).getAsString());
        assertEquals("AAPL", json.get("hiddenTickers").getAsJsonArray().get(0).getAsString());
        assertEquals(1234L, json.get("lut").getAsLong());
    }

    // Stored attribute names and types are a contract with the earnings server stream parser
    @Test
    public void testStoredItemShapeMatchesTheStreamContract() {
        UserEarningsDataItem item = UserEarningsDataItem.builder()
                .userId("user_shape")
                .customTickers(Arrays.asList("PLTR", "SHOP"))
                .hiddenTickers(new ArrayList<>())
                .build();
        userEarningsDAO.saveUserEarnings(item);

        Map<String, AttributeValue> key = new HashMap<>();
        key.put(UserEarningsDDBConstants.TABLE_USER_ID_KEY, new AttributeValue().withS("user_shape"));
        Map<String, AttributeValue> raw = LocalDDBServer.getDynamoDB()
                .getItem(new GetItemRequest(UserEarningsDDBConstants.TABLE_NAME, key)).getItem();

        assertEquals("UserEarnings", UserEarningsDDBConstants.TABLE_NAME);
        assertEquals(new java.util.HashSet<>(Arrays.asList("UserId", "Custom", "Hidden", "LUT")), raw.keySet());
        assertEquals("user_shape", raw.get("UserId").getS());
        assertEquals(2, raw.get("Custom").getL().size());
        assertEquals("PLTR", raw.get("Custom").getL().get(0).getS());
        assertEquals("SHOP", raw.get("Custom").getL().get(1).getS());
        assertNotNull("Hidden must be a list even when empty", raw.get("Hidden").getL());
        assertTrue(raw.get("Hidden").getL().isEmpty());
        assertEquals(String.valueOf(item.getLastUpdateTime()), raw.get("LUT").getN());
    }
}
