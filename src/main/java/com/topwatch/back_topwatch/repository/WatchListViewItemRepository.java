package com.topwatch.back_topwatch.repository;

import com.topwatch.back_topwatch.domain.WatchListViewItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchListViewItemRepository extends JpaRepository<WatchListViewItem, Long> {

    List<WatchListViewItem> findByWatchListView_Id(Long watchListViewId);

    Optional<WatchListViewItem> findByWatchListView_IdAndWatchListItem_Id(Long watchListViewId, Long watchListItemId);

    long countByWatchListView_IdAndWatchedTrue(Long watchListViewId);

    /**
     * The viewer's watched items across all their views, most recently watched first,
     * with the view, its watch list and the item loaded.
     */
    @Query("""
            select vi from WatchListViewItem vi
            join fetch vi.watchListView v
            join fetch v.watchList
            join fetch vi.watchListItem wli
            join fetch wli.item
            where v.viewer.id = :viewerId
              and vi.watched = true
              and vi.watchedDate is not null
            order by vi.watchedDate desc, vi.id desc
            """)
    List<WatchListViewItem> findRecentlyWatchedByViewer(@Param("viewerId") Long viewerId, Pageable pageable);

}
