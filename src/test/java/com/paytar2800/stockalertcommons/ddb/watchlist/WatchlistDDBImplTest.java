package com.paytar2800.stockalertcommons.ddb.watchlist;

import com.paytar2800.stockalertcommons.ddb.util.LocalDDBServer;
import com.paytar2800.stockalertcommons.ddb.watchlist.model.WatchlistDataItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class WatchlistDDBImplTest {

    private WatchlistDAO watchlistDAO;

    @Before
    public void setUp() throws Exception {
        LocalDDBServer.startServer();
        LocalDDBServer.createTable(WatchlistDataItem.class);
        watchlistDAO = new WatchlistDDBImpl(LocalDDBServer.getDynamoDBMapper());
    }

    @After
    public void tearDown() throws Exception {
        LocalDDBServer.stopServer();
        watchlistDAO = null;
    }

    @Test
    public void testSaveAndGetWatchlist() {
        String userId = "user123";
        String watchlistId = "wl1";
        List<String> tickers = Arrays.asList("AAPL", "MSFT", "GOOGL");

        WatchlistDataItem item = WatchlistDataItem.builder()
                .userId(userId)
                .watchlistId(watchlistId)
                .watchlistName("Tech Giants")
                .displayOrder(0)
                .tickers(tickers)
                .build();

        watchlistDAO.saveWatchlist(item);

        assertNotNull(item.getLastUpdateTime());
        assertTrue(item.getLastUpdateTime() > 0);

        Optional<WatchlistDataItem> fetched = watchlistDAO.getWatchlist(userId, watchlistId);
        assertTrue(fetched.isPresent());
        WatchlistDataItem fetchedItem = fetched.get();
        assertEquals(userId, fetchedItem.getUserId());
        assertEquals(watchlistId, fetchedItem.getWatchlistId());
        assertEquals("Tech Giants", fetchedItem.getWatchlistName());
        assertEquals(Integer.valueOf(0), fetchedItem.getDisplayOrder());
        assertEquals(tickers, fetchedItem.getTickers());
        assertEquals(item.getLastUpdateTime(), fetchedItem.getLastUpdateTime());
    }

    @Test
    public void testGetWatchlistsForUser() {
        String userId = "user_multi";

        WatchlistDataItem wl1 = WatchlistDataItem.builder()
                .userId(userId)
                .watchlistId("wl1")
                .watchlistName("Watchlist 1")
                .displayOrder(0)
                .tickers(Arrays.asList("AAPL"))
                .build();

        WatchlistDataItem wl2 = WatchlistDataItem.builder()
                .userId(userId)
                .watchlistId("wl2")
                .watchlistName("Watchlist 2")
                .displayOrder(1)
                .tickers(Arrays.asList("TSLA", "NVDA"))
                .build();

        WatchlistDataItem otherUserWl = WatchlistDataItem.builder()
                .userId("other_user")
                .watchlistId("wl1")
                .watchlistName("Other")
                .displayOrder(0)
                .build();

        watchlistDAO.saveWatchlist(wl1);
        watchlistDAO.saveWatchlist(wl2);
        watchlistDAO.saveWatchlist(otherUserWl);

        List<WatchlistDataItem> userWatchlists = watchlistDAO.getWatchlistsForUser(userId);
        assertNotNull(userWatchlists);
        assertEquals(2, userWatchlists.size());

        List<WatchlistDataItem> nonExistentUserWatchlists = watchlistDAO.getWatchlistsForUser("non_existent");
        assertNotNull(nonExistentUserWatchlists);
        assertTrue(nonExistentUserWatchlists.isEmpty());
    }

    @Test
    public void testDeleteWatchlist() {
        String userId = "user_del";
        String watchlistId = "wl_to_delete";

        WatchlistDataItem item = WatchlistDataItem.builder()
                .userId(userId)
                .watchlistId(watchlistId)
                .watchlistName("To Delete")
                .build();

        watchlistDAO.saveWatchlist(item);
        assertTrue(watchlistDAO.getWatchlist(userId, watchlistId).isPresent());

        watchlistDAO.deleteWatchlist(userId, watchlistId);
        assertFalse(watchlistDAO.getWatchlist(userId, watchlistId).isPresent());
    }

    @Test
    public void testUpdateTickersOrder() throws InterruptedException {
        String userId = "user_reorder";
        String watchlistId = "wl_reorder";

        WatchlistDataItem item = WatchlistDataItem.builder()
                .userId(userId)
                .watchlistId(watchlistId)
                .watchlistName("Reorder Test")
                .tickers(Arrays.asList("A", "B", "C"))
                .build();

        watchlistDAO.saveWatchlist(item);
        Long firstUpdateTime = item.getLastUpdateTime();

        Thread.sleep(10);
        List<String> reordered = Arrays.asList("C", "A", "B");
        watchlistDAO.updateTickersOrder(userId, watchlistId, reordered);

        Optional<WatchlistDataItem> fetched = watchlistDAO.getWatchlist(userId, watchlistId);
        assertTrue(fetched.isPresent());
        assertEquals(reordered, fetched.get().getTickers());
        assertTrue(fetched.get().getLastUpdateTime() >= firstUpdateTime);
    }

    @Test
    public void testAddTickerToWatchlist() {
        String userId = "user_add";
        String watchlistId = "wl_add";

        WatchlistDataItem item = WatchlistDataItem.builder()
                .userId(userId)
                .watchlistId(watchlistId)
                .watchlistName("Add Test")
                .tickers(new ArrayList<>(Arrays.asList("AAPL")))
                .build();

        watchlistDAO.saveWatchlist(item);

        watchlistDAO.addTickerToWatchlist(userId, watchlistId, "MSFT");

        Optional<WatchlistDataItem> fetched = watchlistDAO.getWatchlist(userId, watchlistId);
        assertTrue(fetched.isPresent());
        assertEquals(Arrays.asList("AAPL", "MSFT"), fetched.get().getTickers());

        // Test duplicate ticker is not added
        watchlistDAO.addTickerToWatchlist(userId, watchlistId, "MSFT");
        fetched = watchlistDAO.getWatchlist(userId, watchlistId);
        assertTrue(fetched.isPresent());
        assertEquals(Arrays.asList("AAPL", "MSFT"), fetched.get().getTickers());
    }

    @Test
    public void testUpdateTickersOrder_nonExistentItemAndNullList() {
        String userId = "user_reorder_new";
        String watchlistId = "wl_reorder_new";

        // Item does not exist yet, orderedTickers is null
        watchlistDAO.updateTickersOrder(userId, watchlistId, null);

        Optional<WatchlistDataItem> fetched = watchlistDAO.getWatchlist(userId, watchlistId);
        assertTrue(fetched.isPresent());
        assertNotNull(fetched.get().getTickers());
        assertTrue(fetched.get().getTickers().isEmpty());
    }

    @Test
    public void testAddTickerToWatchlist_nonExistentAndNullTickers() {
        String userId = "user_add_new";
        String watchlistId = "wl_add_new";

        // Item does not exist yet
        watchlistDAO.addTickerToWatchlist(userId, watchlistId, "NVDA");

        Optional<WatchlistDataItem> fetched = watchlistDAO.getWatchlist(userId, watchlistId);
        assertTrue(fetched.isPresent());
        assertEquals(Arrays.asList("NVDA"), fetched.get().getTickers());
    }

    @Test
    public void testRemoveTickerFromWatchlist_nonExistentAndNullTickers() {
        String userId = "user_remove_edge";
        String watchlistId = "wl_remove_edge";

        // 1. Non-existent item should not throw exception
        watchlistDAO.removeTickerFromWatchlist(userId, watchlistId, "AAPL");
        assertFalse(watchlistDAO.getWatchlist(userId, watchlistId).isPresent());

        // 2. Existing item with null tickers list
        WatchlistDataItem itemWithNullTickers = WatchlistDataItem.builder()
                .userId(userId)
                .watchlistId(watchlistId)
                .watchlistName("Null Tickers")
                .tickers(null)
                .build();
        watchlistDAO.saveWatchlist(itemWithNullTickers);

        watchlistDAO.removeTickerFromWatchlist(userId, watchlistId, "AAPL");
        Optional<WatchlistDataItem> fetched = watchlistDAO.getWatchlist(userId, watchlistId);
        assertTrue(fetched.isPresent());
    }

    @Test
    public void testRemoveTickerFromWatchlist() {
        String userId = "user_remove_normal";
        String watchlistId = "wl_remove_normal";

        WatchlistDataItem item = WatchlistDataItem.builder()
                .userId(userId)
                .watchlistId(watchlistId)
                .watchlistName("Remove Normal")
                .tickers(new ArrayList<>(Arrays.asList("AAPL", "MSFT", "GOOGL")))
                .build();

        watchlistDAO.saveWatchlist(item);

        watchlistDAO.removeTickerFromWatchlist(userId, watchlistId, "MSFT");

        Optional<WatchlistDataItem> fetched = watchlistDAO.getWatchlist(userId, watchlistId);
        assertTrue(fetched.isPresent());
        assertEquals(Arrays.asList("AAPL", "GOOGL"), fetched.get().getTickers());
    }

    @Test
    public void testToJson() {
        WatchlistDataItem item = WatchlistDataItem.builder()
                .userId("u1")
                .watchlistId("w1")
                .watchlistName("Tech")
                .displayOrder(1)
                .tickers(Arrays.asList("AAPL"))
                .lastUpdateTime(123456789L)
                .build();

        String json = item.toJson();
        assertNotNull(json);
        assertTrue(json.contains("\"userId\":\"u1\""));
        assertTrue(json.contains("\"watchlistId\":\"w1\""));
        assertTrue(json.contains("\"name\":\"Tech\""));
        assertTrue(json.contains("\"ord\":1"));
        assertTrue(json.contains("\"tickers\":[\"AAPL\"]"));
        assertTrue(json.contains("\"lut\":123456789"));
    }
}
