package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.Category;
import com.topwatch.back_topwatch.domain.enums.Type;
import com.topwatch.back_topwatch.repository.CategoryRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> findCategoriesByType(@NonNull Type type) {
        return categoryRepository.findByType(type);
    }

}
