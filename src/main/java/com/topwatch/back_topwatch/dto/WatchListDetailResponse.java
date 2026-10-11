package com.topwatch.back_topwatch.dto;

import com.topwatch.back_topwatch.domain.WatchList;
import com.topwatch.back_topwatch.domain.WatchListItem;

import java.time.Instant;
import java.util.List;

public record WatchListDetailResponse(
        Long id,
        String name,
        Instant creationDate,
        String description,
        CreatorSummary creator,
        List<WatchListItemEntry> items
) {

    public record CreatorSummary(Long id, String nickname) {}

    public record WatchListItemEntry(
            Long id,
            Boolean watched,
            Instant watchedDate,
            String description,
            ItemResponse item
    ) {}

    public static WatchListDetailResponse from(WatchList watchList, List<WatchListItem> watchListItems) {
        CreatorSummary creator = watchList.getCreator() != null
                ? new CreatorSummary(watchList.getCreator().getId(), watchList.getCreator().getNickname())
                : null;

        List<WatchListItemEntry> items = watchListItems.stream()
                .map(watchListItem -> new WatchListItemEntry(
                        watchListItem.getId(),
                        watchListItem.getWatched(),
                        watchListItem.getWatchedDate(),
                        watchListItem.getDescription(),
                        ItemResponse.from(watchListItem.getItem())
                ))
                .toList();

        return new WatchListDetailResponse(
                watchList.getId(),
                watchList.getName(),
                watchList.getCreationDate(),
                watchList.getDescription(),
                creator,
                items
        );
    }

}
