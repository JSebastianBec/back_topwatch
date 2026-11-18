package com.topwatch.back_topwatch.repository;

import com.topwatch.back_topwatch.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByNameContainingIgnoreCase(String name);

    List<Item> findByCreator_Id(Long creatorId);

    boolean existsByCreator_Id(Long creatorId);

    @Query("SELECT i FROM Item i WHERE i.id NOT IN " +
            "(SELECT wli.item.id FROM WatchListItem wli WHERE wli.watchList.id = :watchListId)")
    List<Item> findAllNotInWatchList(@Param("watchListId") Long watchListId);

    @Query("SELECT i FROM Item i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%')) AND i.id NOT IN " +
            "(SELECT wli.item.id FROM WatchListItem wli WHERE wli.watchList.id = :watchListId)")
    List<Item> findByNameNotInWatchList(@Param("name") String name, @Param("watchListId") Long watchListId);

}
