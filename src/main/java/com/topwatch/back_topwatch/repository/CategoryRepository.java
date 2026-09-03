package com.topwatch.back_topwatch.repository;

import com.topwatch.back_topwatch.domain.Category;
import com.topwatch.back_topwatch.domain.enums.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByType(Type type);

}
