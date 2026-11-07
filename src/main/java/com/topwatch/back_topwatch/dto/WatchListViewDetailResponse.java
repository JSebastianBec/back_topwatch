package com.topwatch.back_topwatch.dto;

import com.topwatch.back_topwatch.domain.WatchListItem;
import com.topwatch.back_topwatch.domain.WatchListView;
import com.topwatch.back_topwatch.domain.WatchListViewItem;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record WatchListViewDetailResponse(
        Long id,
        Instant startDate,
        WatchListViewResponse.WatchListSummary watchList,
        long watchedItems,
        long totalItems,
        double progress,
        List<ViewItemEntry> items
) {

    public record ViewItemEntry(
            Long watchListItemId,
            Boolean watched,
            Instant watchedDate,
            ItemResponse item
    ) {

        public static ViewItemEntry from(WatchListItem watchListItem, WatchListViewItem viewItem) {
            boolean watched = viewItem != null && Boolean.TRUE.equals(viewItem.getWatched());
            return new ViewItemEntry(
                    watchListItem.getId(),
                    watched,
                    watched ? viewItem.getWatchedDate() : null,
                    ItemResponse.from(watchListItem.getItem())
            );
        }

    }

    /**
     * @param viewItemsByWatchListItemId the viewer's watched state, keyed by watch list item id
     */
    public static WatchListViewDetailResponse from(
            WatchListView view,
            List<WatchListItem> watchListItems,
            Map<Long, WatchListViewItem> viewItemsByWatchListItemId
    ) {
        List<ViewItemEntry> items = watchListItems.stream()
                .map(watchListItem -> ViewItemEntry.from(watchListItem, viewItemsByWatchListItemId.get(watchListItem.getId())))
                .toList();
        long watchedItems = items.stream().filter(ViewItemEntry::watched).count();
        long totalItems = items.size();

        return new WatchListViewDetailResponse(
                view.getId(),
                view.getStartDate(),
                WatchListViewResponse.WatchListSummary.from(view.getWatchList()),
                watchedItems,
                totalItems,
                WatchListViewResponse.progress(watchedItems, totalItems),
                items
        );
    }

}
