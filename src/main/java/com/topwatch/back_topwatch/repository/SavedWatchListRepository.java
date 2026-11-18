package com.topwatch.back_topwatch.repository;

import com.topwatch.back_topwatch.domain.SavedWatchList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SavedWatchListRepository extends JpaRepository<SavedWatchList, Long> {

    List<SavedWatchList> findByUser_IdAndWatchList_IdIn(Long userId, Collection<Long> watchListIds);

    boolean existsByWatchList_Id(Long watchListId);

    @Modifying
    @Query("delete from SavedWatchList s where s.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("delete from SavedWatchList s where s.watchList.id = :watchListId")
    void deleteByWatchListId(@Param("watchListId") Long watchListId);

}
