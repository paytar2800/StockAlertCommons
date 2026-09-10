package com.paytar2800.stockalertcommons.ddb.watchlist;

import com.paytar2800.stockalertcommons.ddb.watchlist.model.WatchlistDataItem;

import java.util.List;
import java.util.Optional;

public interface WatchlistDAO {

    List<WatchlistDataItem> getWatchlistsForUser(String userId);

    Optional<WatchlistDataItem> getWatchlist(String userId, String watchlistId);

    void saveWatchlist(WatchlistDataItem item);

    void deleteWatchlist(String userId, String watchlistId);

    void updateTickersOrder(String userId, String watchlistId, List<String> orderedTickers);

    void addTickerToWatchlist(String userId, String watchlistId, String ticker);

    void removeTickerFromWatchlist(String userId, String watchlistId, String ticker);
}
