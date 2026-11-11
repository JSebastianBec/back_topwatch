package com.topwatch.back_topwatch.repository;

import com.topwatch.back_topwatch.domain.SavedWatchList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SavedWatchListRepository extends JpaRepository<SavedWatchList, Long> {

    List<SavedWatchList> findByUser_IdAndWatchList_IdIn(Long userId, Collection<Long> watchListIds);

}
