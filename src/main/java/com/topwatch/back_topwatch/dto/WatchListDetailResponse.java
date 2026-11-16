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
        boolean owned,
        boolean started,
        Long viewId,
        List<WatchListItemEntry> items
) {

    public record CreatorSummary(Long id, String nickname) {}

    public record WatchListItemEntry(
            Long id,
            String description,
            ItemResponse item
    ) {

        public static WatchListItemEntry from(WatchListItem watchListItem) {
            return new WatchListItemEntry(
                    watchListItem.getId(),
                    watchListItem.getDescription(),
                    ItemResponse.from(watchListItem.getItem())
            );
        }

    }

    public static WatchListDetailResponse from(WatchList watchList, List<WatchListItem> watchListItems) {
        return from(watchList, watchListItems, null);
    }

    /** viewId is the authenticated user's view on this list, or null if they have not started it. */
    public static WatchListDetailResponse from(WatchList watchList, List<WatchListItem> watchListItems, Long viewId) {
        return from(watchList, watchListItems, viewId, null);
    }

    /** owned is true only when currentUserId created the list (only the creator can change it). */
    public static WatchListDetailResponse from(
            WatchList watchList,
            List<WatchListItem> watchListItems,
            Long viewId,
            Long currentUserId
    ) {
        CreatorSummary creator = watchList.getCreator() != null
                ? new CreatorSummary(watchList.getCreator().getId(), watchList.getCreator().getNickname())
                : null;

        List<WatchListItemEntry> items = watchListItems.stream()
                .map(WatchListItemEntry::from)
                .toList();

        return new WatchListDetailResponse(
                watchList.getId(),
                watchList.getName(),
                watchList.getCreationDate(),
                watchList.getDescription(),
                creator,
                creator != null && creator.id().equals(currentUserId),
                viewId != null,
                viewId,
                items
        );
    }

}
