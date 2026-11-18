package com.topwatch.back_topwatch.repository;

import com.topwatch.back_topwatch.domain.WatchListViewItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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

    @Modifying
    @Query("delete from WatchListViewItem vi where vi.watchListView.id = :viewId")
    void deleteByViewId(@Param("viewId") Long viewId);

    /** The viewer's watched state across all their views. */
    @Modifying
    @Query("""
            delete from WatchListViewItem vi
            where vi.watchListView.id in (select v.id from WatchListView v where v.viewer.id = :viewerId)
            """)
    void deleteByViewerId(@Param("viewerId") Long viewerId);

    /** Every viewer's watched state for the entries of one watch list. */
    @Modifying
    @Query("""
            delete from WatchListViewItem vi
            where vi.watchListView.id in (select v.id from WatchListView v where v.watchList.id = :watchListId)
            """)
    void deleteByWatchListId(@Param("watchListId") Long watchListId);

    /** Every viewer's watched state for the watch list entries that point to one item. */
    @Modifying
    @Query("""
            delete from WatchListViewItem vi
            where vi.watchListItem.id in (select wli.id from WatchListItem wli where wli.item.id = :itemId)
            """)
    void deleteByItemId(@Param("itemId") Long itemId);

    /** Every viewer's watched state for the given watch list entries. */
    @Modifying
    @Query("delete from WatchListViewItem vi where vi.watchListItem.id in :watchListItemIds")
    void deleteByWatchListItemIds(@Param("watchListItemIds") Collection<Long> watchListItemIds);

}
