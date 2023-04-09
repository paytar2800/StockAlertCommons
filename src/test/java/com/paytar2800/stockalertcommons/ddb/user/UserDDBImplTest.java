package com.paytar2800.stockalertcommons.ddb.user;

import com.paytar2800.stockalertcommons.ddb.PaginatedItem;
import com.paytar2800.stockalertcommons.ddb.user.model.UserDataItem;
import com.paytar2800.stockalertcommons.ddb.user.model.UserDataItem_DeletedData;
import com.paytar2800.stockalertcommons.ddb.util.LocalDDBServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class UserDDBImplTest {

    private UserDAO userDAO;
    private List<UserDataItem> itemList;

    @Before
    public void before() throws Exception {
        LocalDDBServer.startServer();
        LocalDDBServer.createTable(UserDataItem.class);
        LocalDDBServer.createTable(UserDataItem_DeletedData.class);
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
    public void testPutItem_deletedData() {
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
            UserDataItem_DeletedData dataItemDeleted = new UserDataItem_DeletedData(actualItem);
            LocalDDBServer.saveItemToDB(dataItemDeleted);

            UserDataItem_DeletedData dataItemDeletedDataFromDB = (UserDataItem_DeletedData)
                    LocalDDBServer.loadItemFromDB(dataItemDeleted);

            Assert.assertTrue(new ReflectionEquals(dataItemDeletedDataFromDB).matches(actualItem));
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
    public void testBatchSaveItem(){
        itemList.forEach(this::addItemToDB);

        PaginatedItem<String, String> paginatedItem = userDAO.getLatestUpdatedUsers(
                null, null);

        List<String> userIdList = paginatedItem.getCurrentItemList();

        List<UserDataItem> itemList = new ArrayList<>();

        userIdList.forEach(userId -> {
            UserDataItem dataItem = UserDataItem.builder().userId(userId).build();
            UserDataItem actualItem = (UserDataItem) LocalDDBServer.loadItemFromDB(dataItem);
            assertTrue(actualItem.getHasChanged());
            dataItem.setHasChanged(false);
            userDAO.updateItem(dataItem);
        });


        //Verify that the flag has turned off
        userIdList.forEach(userId -> {
            UserDataItem dataItem = UserDataItem.builder().userId(userId).build();
            UserDataItem actualItem = (UserDataItem) LocalDDBServer.loadItemFromDB(dataItem);
            assertFalse(actualItem.getHasChanged());
            assertNotNull(actualItem.getEmailId());
            assertNotNull(actualItem.getDeviceToken());
            assertNotNull(actualItem.getAlertSnoozeTimeSeconds());
            assertNotNull(actualItem.getIsAlertEnabled());
        });
    }

}