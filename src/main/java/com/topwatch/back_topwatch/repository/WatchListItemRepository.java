package com.topwatch.back_topwatch.repository;

import com.topwatch.back_topwatch.domain.WatchListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface WatchListItemRepository extends JpaRepository<WatchListItem, Long> {

    List<WatchListItem> findByWatchList_Id(Long watchListId);

    List<WatchListItem> findByWatchList_IdAndItem_IdIn(Long watchListId, Collection<Long> itemIds);

    Optional<WatchListItem> findByIdAndWatchList_Id(Long id, Long watchListId);

    long countByWatchList_Id(Long watchListId);

    /** Ids of the watch lists created by someone other than the user that contain the item. */
    @Query("""
            select distinct wli.watchList.id from WatchListItem wli
            where wli.item.id = :itemId and wli.watchList.creator.id <> :userId
            order by wli.watchList.id
            """)
    List<Long> findWatchListIdsWithItemNotCreatedBy(@Param("itemId") Long itemId, @Param("userId") Long userId);

    @Modifying
    @Query("delete from WatchListItem wli where wli.watchList.id = :watchListId")
    void deleteByWatchListId(@Param("watchListId") Long watchListId);

    @Modifying
    @Query("delete from WatchListItem wli where wli.item.id = :itemId")
    void deleteByItemId(@Param("itemId") Long itemId);

}
