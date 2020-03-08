package com.paytar2800.stockalertcommons.ddb.user;

import com.paytar2800.stockalertcommons.ddb.user.model.UserDataItem;
import com.paytar2800.stockalertcommons.ddb.util.LocalDDBServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertNotEquals;

public class UserDDBImplTest {

    private UserDAO userDAO;
    private List<UserDataItem> itemList;

    @Before
    public void before() throws Exception {
        LocalDDBServer.startServer();
        LocalDDBServer.createTable(UserDataItem.class);
        itemList = UserData.getSampleData();
        userDAO = new UserDDBImpl(LocalDDBServer.getDynamoDBMapper());
    }

    @After
    public void after() throws Exception {
        LocalDDBServer.stopServer();
        userDAO = null;
    }

    public void addItemToDB(UserDataItem dataItem) {
        LocalDDBServer.addItemToDB(dataItem);
    }

    @Test
    public void testGetItemForUserId() {
        itemList.forEach(this::addItemToDB);
        itemList.forEach(actualItem -> {
            Optional<UserDataItem> expected;
            try {
                expected = userDAO.getItemUsingUserId(actualItem.getUserId());
                assertTrue(expected.isPresent());
                assertEquals(expected.get(), actualItem);
            } catch (Exception e) {
                fail("failed with exception = " + e.getMessage());
            }
        });
    }

    @Test
    public void testGetItemForEmail() {
        itemList.forEach(this::addItemToDB);
        itemList.forEach(actualItem -> {
            Optional<UserDataItem> expected;
            try {
                Optional<String> userId = userDAO.getUserIdForEmail(actualItem.getEmailId());
                assertTrue(userId.isPresent());
                assertEquals(userId.get(), actualItem.getUserId());
            } catch (Exception e) {
                fail("failed with exception = " + e.getMessage());
            }
        });
    }


    @Test
    public void testPutItem() {
        itemList.forEach(item -> {
            try {
                userDAO.updateItem(item);
            } catch (Exception e) {
                fail("failed with exception = " + e.getMessage());
            }
        });
        itemList.forEach(item -> {
            UserDataItem fetchItem = UserDataItem.builder().userId(item.getUserId()).build();
            UserDataItem actualItem = (UserDataItem) LocalDDBServer.loadItemFromDB(fetchItem);
            assertEquals(actualItem, item);
        });
    }

    @Test
    public void testUpdateItem() throws Exception {
        itemList.forEach(this::addItemToDB);
        UserDataItem userDataItem = UserDataItem.builder().userId(itemList.get(0).getUserId())
                .emailId("sds")
                .build();
        userDAO.updateItem(userDataItem);
        UserDataItem queryItem = UserDataItem.builder().userId(itemList.get(0).getUserId()).build();
        UserDataItem actualItem = (UserDataItem) LocalDDBServer.loadItemFromDB(queryItem);
        assertEquals("sds", actualItem.getEmailId());
        assertNotEquals(itemList.get(0).getEmailId(), actualItem.getEmailId());
        actualItem.setEmailId(itemList.get(0).getEmailId());
        assertEquals(itemList.get(0), actualItem);
    }

    @Test
    public void testDeleteItem() {
        itemList.forEach(this::addItemToDB);
        itemList.forEach(item -> {
            UserDataItem actualItem = (UserDataItem) LocalDDBServer.loadItemFromDB(item);
            assertEquals(item, actualItem);
            try {
                userDAO.deleteItem(item);
            } catch (Exception e) {
                fail("failed with exception = " + e.getMessage());
                e.printStackTrace();
            }
            actualItem = (UserDataItem) LocalDDBServer.loadItemFromDB(item);
            assertNull(actualItem);
        });
    }

    @Test
    public void test() {
        UserDataItem dataItem = UserDataItem.builder().emailId("tarun").userId("tar").alertSnoozeTimeSeconds(0).build();
        System.out.println(dataItem.toJson().toString());
    }

}