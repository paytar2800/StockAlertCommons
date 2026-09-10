package com.paytar2800.stockalertcommons.ddb.watchlist;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.paytar2800.stockalertcommons.ddb.watchlist.model.WatchlistDataItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class WatchlistDDBImpl implements WatchlistDAO {

    private final DynamoDBMapper dynamoDBMapper;

    public WatchlistDDBImpl(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    @Override
    public List<WatchlistDataItem> getWatchlistsForUser(String userId) {
        WatchlistDataItem keyObject = WatchlistDataItem.builder()
                .userId(userId)
                .build();
        DynamoDBQueryExpression<WatchlistDataItem> queryExpression =
                new DynamoDBQueryExpression<WatchlistDataItem>()
                        .withHashKeyValues(keyObject)
                        .withConsistentRead(false);
        PaginatedQueryList<WatchlistDataItem> results = dynamoDBMapper.query(WatchlistDataItem.class, queryExpression);
        return results != null ? new ArrayList<>(results) : Collections.emptyList();
    }

    @Override
    public List<WatchlistDataItem> getWatchlistsMetadataForUser(String userId) {
        WatchlistDataItem keyObject = WatchlistDataItem.builder()
                .userId(userId)
                .build();
        DynamoDBQueryExpression<WatchlistDataItem> queryExpression =
                new DynamoDBQueryExpression<WatchlistDataItem>()
                        .withHashKeyValues(keyObject)
                        .withIndexName(WatchlistDDBConstants.WATCHLIST_METADATA_GSI)
                        .withConsistentRead(false);
        PaginatedQueryList<WatchlistDataItem> results = dynamoDBMapper.query(WatchlistDataItem.class, queryExpression);
        return results != null ? new ArrayList<>(results) : Collections.emptyList();
    }

    @Override
    public Optional<WatchlistDataItem> getWatchlist(String userId, String watchlistId) {
        return Optional.ofNullable(dynamoDBMapper.load(WatchlistDataItem.class, userId, watchlistId));
    }

    @Override
    public void saveWatchlist(WatchlistDataItem item) {
        item.setLastUpdateTime(System.currentTimeMillis());
        dynamoDBMapper.save(item);
    }

    @Override
    public void deleteWatchlist(String userId, String watchlistId) {
        WatchlistDataItem item = WatchlistDataItem.builder()
                .userId(userId)
                .watchlistId(watchlistId)
                .build();
        dynamoDBMapper.delete(item);
    }

    @Override
    public void updateTickersOrder(String userId, String watchlistId, List<String> orderedTickers) {
        Optional<WatchlistDataItem> optionalItem = getWatchlist(userId, watchlistId);
        WatchlistDataItem item = optionalItem.orElseGet(() -> WatchlistDataItem.builder()
                .userId(userId)
                .watchlistId(watchlistId)
                .build());
        item.setTickers(orderedTickers != null ? new ArrayList<>(orderedTickers) : new ArrayList<>());
        saveWatchlist(item);
    }

    @Override
    public void addTickerToWatchlist(String userId, String watchlistId, String ticker) {
        Optional<WatchlistDataItem> optionalItem = getWatchlist(userId, watchlistId);
        WatchlistDataItem item = optionalItem.orElseGet(() -> WatchlistDataItem.builder()
                .userId(userId)
                .watchlistId(watchlistId)
                .build());
        List<String> tickers = item.getTickers();
        if (tickers == null) {
            tickers = new ArrayList<>();
            item.setTickers(tickers);
        }
        if (!tickers.contains(ticker)) {
            tickers.add(ticker);
        }
        saveWatchlist(item);
    }

    @Override
    public void removeTickerFromWatchlist(String userId, String watchlistId, String ticker) {
        Optional<WatchlistDataItem> optionalItem = getWatchlist(userId, watchlistId);
        if (optionalItem.isPresent()) {
            WatchlistDataItem item = optionalItem.get();
            if (item.getTickers() != null) {
                item.getTickers().remove(ticker);
            }
            saveWatchlist(item);
        }
    }
}
