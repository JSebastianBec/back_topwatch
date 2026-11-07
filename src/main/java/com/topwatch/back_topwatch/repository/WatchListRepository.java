package com.topwatch.back_topwatch.repository;

import com.topwatch.back_topwatch.domain.WatchList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WatchListRepository extends JpaRepository<WatchList, Long> {

    List<WatchList> findByCreator_Id(Long creatorId);

}
