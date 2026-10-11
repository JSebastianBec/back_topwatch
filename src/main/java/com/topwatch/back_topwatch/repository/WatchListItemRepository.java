package com.topwatch.back_topwatch.repository;

import com.topwatch.back_topwatch.domain.WatchListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WatchListItemRepository extends JpaRepository<WatchListItem, Long> {

    List<WatchListItem> findByWatchList_Id(Long watchListId);

}
