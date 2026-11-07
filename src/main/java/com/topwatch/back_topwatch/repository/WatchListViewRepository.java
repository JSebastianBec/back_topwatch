package com.topwatch.back_topwatch.repository;

import com.topwatch.back_topwatch.domain.WatchListView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WatchListViewRepository extends JpaRepository<WatchListView, Long> {

    List<WatchListView> findByViewer_IdOrderByStartDateDesc(Long viewerId);

    boolean existsByViewer_IdAndWatchList_Id(Long viewerId, Long watchListId);

}
