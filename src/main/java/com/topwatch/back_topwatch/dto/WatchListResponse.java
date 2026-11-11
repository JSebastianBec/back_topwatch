package com.topwatch.back_topwatch.dto;

import com.topwatch.back_topwatch.domain.WatchList;

/**
 * started/viewId tell the authenticated user whether they already started watching
 * this list; viewId is their WatchListView id (null when not started). creator lets
 * the front show who made the list; owned is true only for lists the authenticated user
 * created, so lists added from other users can be shown read-only.
 */
public record WatchListResponse(
        Long id,
        String name,
        CreatorSummary creator,
        boolean owned,
        boolean started,
        Long viewId
) {

    public record CreatorSummary(Long id, String nickname) {}

    public static WatchListResponse from(WatchList watchList) {
        return from(watchList, null);
    }

    public static WatchListResponse from(WatchList watchList, Long viewId) {
        return from(watchList, viewId, null);
    }

    public static WatchListResponse from(WatchList watchList, Long viewId, Long currentUserId) {
        CreatorSummary creator = watchList.getCreator() != null
                ? new CreatorSummary(watchList.getCreator().getId(), watchList.getCreator().getNickname())
                : null;
        return new WatchListResponse(watchList.getId(), watchList.getName(), creator,
                creator != null && creator.id().equals(currentUserId), viewId != null, viewId);
    }

}
