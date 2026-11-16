package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.domain.WatchList;
import com.topwatch.back_topwatch.domain.WatchListItem;
import com.topwatch.back_topwatch.domain.WatchListView;
import com.topwatch.back_topwatch.domain.WatchListViewItem;
import com.topwatch.back_topwatch.exception.BadRequestException;
import com.topwatch.back_topwatch.exception.DuplicateResourceException;
import com.topwatch.back_topwatch.exception.ResourceNotFoundException;
import com.topwatch.back_topwatch.repository.WatchListItemRepository;
import com.topwatch.back_topwatch.repository.WatchListRepository;
import com.topwatch.back_topwatch.repository.WatchListViewItemRepository;
import com.topwatch.back_topwatch.repository.WatchListViewRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WatchListViewService {

    private final WatchListViewRepository watchListViewRepository;
    private final WatchListViewItemRepository watchListViewItemRepository;
    private final WatchListRepository watchListRepository;
    private final WatchListItemRepository watchListItemRepository;

    /**
     * Starts viewing any watch list (own or someone else's). Only a view record is
     * created; the watch list and its items are left untouched.
     */
    @Transactional
    public WatchListView startView(@NonNull Long watchListId, @NonNull User viewer) {
        WatchList watchList = watchListRepository.findById(watchListId)
                .orElseThrow(() -> new ResourceNotFoundException("Watch list not found"));
        if (watchListViewRepository.existsByViewer_IdAndWatchList_Id(viewer.getId(), watchListId)) {
            throw new DuplicateResourceException("You are already viewing this watch list");
        }

        return watchListViewRepository.save(WatchListView.builder()
                .viewer(viewer)
                .watchList(watchList)
                .build());
    }

    public List<WatchListView> findViewsByViewer(@NonNull User viewer) {
        return watchListViewRepository.findByViewer_IdOrderByStartDateDesc(viewer.getId());
    }

    /** Watch list id -> id of the viewer's view on it, for every watch list the viewer has started. */
    public Map<Long, Long> findViewIdsByWatchListId(@NonNull User viewer) {
        return watchListViewRepository.findByViewer_IdOrderByStartDateDesc(viewer.getId()).stream()
                .collect(Collectors.toMap(view -> view.getWatchList().getId(), WatchListView::getId));
    }

    public Optional<WatchListView> findViewByViewerAndWatchList(@NonNull User viewer, @NonNull Long watchListId) {
        return watchListViewRepository.findByViewer_IdAndWatchList_Id(viewer.getId(), watchListId);
    }

    public static final int DEFAULT_RECENTLY_WATCHED_LIMIT = 20;
    public static final int MAX_RECENTLY_WATCHED_LIMIT = 100;

    /** The viewer's most recently watched items across all their views, newest first. */
    public List<WatchListViewItem> findRecentlyWatchedItems(@NonNull User viewer, int limit) {
        if (limit < 1 || limit > MAX_RECENTLY_WATCHED_LIMIT) {
            throw new BadRequestException("Limit must be between 1 and " + MAX_RECENTLY_WATCHED_LIMIT);
        }
        return watchListViewItemRepository.findRecentlyWatchedByViewer(viewer.getId(), PageRequest.of(0, limit));
    }

    public WatchListView findOwnView(@NonNull Long viewId, @NonNull User viewer) {
        WatchListView view = watchListViewRepository.findById(viewId)
                .orElseThrow(() -> new ResourceNotFoundException("Watch list view not found"));
        if (!view.getViewer().getId().equals(viewer.getId())) {
            throw new AccessDeniedException("Only the user who started this view can access it");
        }
        return view;
    }

    public List<WatchListItem> findWatchListItems(@NonNull WatchListView view) {
        return watchListItemRepository.findByWatchList_Id(view.getWatchList().getId());
    }

    /** Viewer's watched state keyed by watch list item id. Items without an entry are unwatched. */
    public Map<Long, WatchListViewItem> findViewItemsByWatchListItemId(@NonNull WatchListView view) {
        return watchListViewItemRepository.findByWatchListView_Id(view.getId()).stream()
                .collect(Collectors.toMap(viewItem -> viewItem.getWatchListItem().getId(), Function.identity()));
    }

    public long countWatchedItems(@NonNull WatchListView view) {
        return watchListViewItemRepository.countByWatchListView_IdAndWatchedTrue(view.getId());
    }

    public long countTotalItems(@NonNull WatchListView view) {
        return watchListItemRepository.countByWatchList_Id(view.getWatchList().getId());
    }

    /**
     * Marks an item of the viewed list as watched (watchedDate = now, kept if it was already
     * watched) or unwatched (watchedDate cleared) for this viewer only. The item is identified by its watch list
     * item id, the same id the list and view detail responses expose for each entry.
     */
    @Transactional
    public WatchListView setWatched(@NonNull Long viewId, @NonNull Long watchListItemId, boolean watched, @NonNull User viewer) {
        WatchListView view = findOwnView(viewId, viewer);
        WatchListItem watchListItem = watchListItemRepository.findByIdAndWatchList_Id(watchListItemId, view.getWatchList().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Item is not in this watch list"));

        WatchListViewItem viewItem = watchListViewItemRepository
                .findByWatchListView_IdAndWatchListItem_Id(view.getId(), watchListItem.getId())
                .orElseGet(() -> WatchListViewItem.builder()
                        .watchListView(view)
                        .watchListItem(watchListItem)
                        .build());
        if (watched) {
            // Keep the original date when the item was already watched so re-sending true doesn't reorder lists
            if (!Boolean.TRUE.equals(viewItem.getWatched()) || viewItem.getWatchedDate() == null) {
                viewItem.setWatchedDate(Instant.now());
            }
        } else {
            viewItem.setWatchedDate(null);
        }
        viewItem.setWatched(watched);
        watchListViewItemRepository.save(viewItem);

        return view;
    }

}
