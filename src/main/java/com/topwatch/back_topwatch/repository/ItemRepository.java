package com.topwatch.back_topwatch.repository;

import com.topwatch.back_topwatch.domain.Item;
import com.topwatch.back_topwatch.domain.ListTop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
}
