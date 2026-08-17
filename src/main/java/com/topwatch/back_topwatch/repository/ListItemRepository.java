package com.topwatch.back_topwatch.repository;

import com.topwatch.back_topwatch.domain.ListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListItemRepository extends JpaRepository<ListItem, Long> {

    List<ListItem> findByListTopId(Long listId);

    List<ListItem> findByItemId(Long itemId);

}
