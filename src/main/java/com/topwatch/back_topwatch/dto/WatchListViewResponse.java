package com.topwatch.back_topwatch.dto;

import com.topwatch.back_topwatch.domain.WatchList;
import com.topwatch.back_topwatch.domain.WatchListView;

import java.time.Instant;

public record WatchListViewResponse(
        Long id,
        Instant startDate,
        WatchListSummary watchList,
        long watchedItems,
        long totalItems,
        double progress
) {

    public record CreatorSummary(Long id, String nickname) {}

    public record WatchListSummary(Long id, String name, CreatorSummary creator) {

        public static WatchListSummary from(WatchList watchList) {
            CreatorSummary creator = watchList.getCreator() != null
                    ? new CreatorSummary(watchList.getCreator().getId(), watchList.getCreator().getNickname())
                    : null;
            return new WatchListSummary(watchList.getId(), watchList.getName(), creator);
        }

    }

    public static WatchListViewResponse from(WatchListView view, long watchedItems, long totalItems) {
        return new WatchListViewResponse(
                view.getId(),
                view.getStartDate(),
                WatchListSummary.from(view.getWatchList()),
                watchedItems,
                totalItems,
                progress(watchedItems, totalItems)
        );
    }

    // watched / total * 100, rounded to 2 decimals; an empty list counts as 0%
    public static double progress(long watchedItems, long totalItems) {
        if (totalItems == 0) {
            return 0;
        }
        return Math.round(watchedItems * 10000.0 / totalItems) / 100.0;
    }

}
