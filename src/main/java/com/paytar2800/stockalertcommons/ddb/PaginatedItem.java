package com.paytar2800.stockalertcommons.ddb;

import java.util.List;

/**
 * Class for Pagination response for CellRoutingStore Query APIs
 *
 * @param <T> Represents Store Item Object type
 * @param <S> Represents token Object type for next pagination query.
 */
public class PaginatedItem<T, S> {

    List<T> itemList;
    S token;

    public PaginatedItem(List<T> itemList, S token){
        this.itemList = itemList;
        this.token = token;
    }

    public List<T> getCurrentItemList(){
        return itemList;
    }

    public S getToken(){
        return token;
    }

}

