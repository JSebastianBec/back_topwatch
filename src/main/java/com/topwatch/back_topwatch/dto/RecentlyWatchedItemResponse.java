package com.topwatch.back_topwatch.dto;

import com.topwatch.back_topwatch.domain.Item;
import com.topwatch.back_topwatch.domain.WatchList;
import com.topwatch.back_topwatch.domain.WatchListViewItem;

import java.time.Instant;

/**
 * One item the user watched, with the watch list it belongs to. Meant for the home page feed.
 */
public record RecentlyWatchedItemResponse(
        Long watchListViewId,
        Long watchListItemId,
        Instant watchedDate,
        ItemSummary item,
        WatchListSummary watchList
) {

    public record ItemSummary(Long id, String name, String urlAvatar) {

        public static ItemSummary from(Item item) {
            return new ItemSummary(item.getId(), item.getName(), item.getUrlAvatar());
        }

    }

    public record WatchListSummary(Long id, String name) {

        public static WatchListSummary from(WatchList watchList) {
            return new WatchListSummary(watchList.getId(), watchList.getName());
        }

    }

    public static RecentlyWatchedItemResponse from(WatchListViewItem viewItem) {
        return new RecentlyWatchedItemResponse(
                viewItem.getWatchListView().getId(),
                viewItem.getWatchListItem().getId(),
                viewItem.getWatchedDate(),
                ItemSummary.from(viewItem.getWatchListItem().getItem()),
                WatchListSummary.from(viewItem.getWatchListView().getWatchList())
        );
    }

}
