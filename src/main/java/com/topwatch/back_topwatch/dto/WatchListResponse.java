package com.topwatch.back_topwatch.dto;

import com.topwatch.back_topwatch.domain.WatchList;

public record WatchListResponse(
        Long id,
        String name
) {

    public static WatchListResponse from(WatchList watchList) {
        return new WatchListResponse(watchList.getId(), watchList.getName());
    }

}
