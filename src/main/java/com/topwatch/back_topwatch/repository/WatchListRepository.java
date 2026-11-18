package com.topwatch.back_topwatch.repository;

import com.topwatch.back_topwatch.domain.WatchList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WatchListRepository extends JpaRepository<WatchList, Long> {

    List<WatchList> findByCreator_Id(Long creatorId);

    boolean existsByCreator_Id(Long creatorId);

    /** Lists the user created plus lists from other users they added to their own, own ones first. */
    @Query("""
            select w from WatchList w
            where w.creator.id = :userId
               or exists (
                   select s.id from SavedWatchList s
                   where s.watchList = w and s.user.id = :userId
               )
            order by case when w.creator.id = :userId then 0 else 1 end, w.id
            """)
    List<WatchList> findMineIncludingSaved(@Param("userId") Long userId);

    /**
     * Lists from other users the user can still add to their own: not created by
     * them, not already added and not started yet.
     */
    @Query("""
            select w from WatchList w
            where w.creator.id <> :userId
              and not exists (
                  select v.id from WatchListView v
                  where v.watchList = w and v.viewer.id = :userId
              )
              and not exists (
                  select s.id from SavedWatchList s
                  where s.watchList = w and s.user.id = :userId
              )
            order by w.id
            """)
    List<WatchList> findNotStartedByViewer(@Param("userId") Long userId);

}
