package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.Category;
import com.topwatch.back_topwatch.domain.Item;
import com.topwatch.back_topwatch.exception.BadRequestException;
import com.topwatch.back_topwatch.repository.CategoryRepository;
import com.topwatch.back_topwatch.repository.ItemRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;

    public Optional<Item> findItemById(@NonNull Long id){ return itemRepository.findById(id);}

    public List<Item> findAllItems() {return itemRepository.findAll();}

    public List<Item> findItemsByName(@NonNull String name) {
        return itemRepository.findByNameContainingIgnoreCase(name);
    }

    public Set<Category> resolveCategories(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<Category> categories = new HashSet<>(categoryRepository.findAllById(categoryIds));
        if (categories.size() != categoryIds.size()) {
            throw new BadRequestException("One or more category ids do not exist");
        }

        return categories;
    }

    public Item saveItem(@NonNull Item item){
        if (!StringUtils.hasText(item.getName())) {
            throw new BadRequestException("Item name is required");
        }
        if (item.getCreator() == null) {
            throw new BadRequestException("Item creator is required");
        }
        if (item.getType() == null) {
            throw new BadRequestException("Item type is required");
        }

        return itemRepository.save(item);
    }

}
