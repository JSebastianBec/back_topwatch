package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.Category;
import com.topwatch.back_topwatch.domain.Item;
import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.exception.BadRequestException;
import com.topwatch.back_topwatch.exception.ResourceInUseException;
import com.topwatch.back_topwatch.exception.ResourceNotFoundException;
import com.topwatch.back_topwatch.repository.CategoryRepository;
import com.topwatch.back_topwatch.repository.ItemRepository;
import com.topwatch.back_topwatch.repository.WatchListItemRepository;
import com.topwatch.back_topwatch.repository.WatchListViewItemRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final WatchListItemRepository watchListItemRepository;
    private final WatchListViewItemRepository watchListViewItemRepository;

    public Optional<Item> findItemById(@NonNull Long id){ return itemRepository.findById(id);}

    public List<Item> findAllItems() {return itemRepository.findAll();}

    public List<Item> findItemsByName(@NonNull String name) {
        return itemRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Item> findItemsNotInWatchList(@NonNull Long watchListId) {
        return itemRepository.findAllNotInWatchList(watchListId);
    }

    public List<Item> findItemsByNameNotInWatchList(@NonNull String name, @NonNull Long watchListId) {
        return itemRepository.findByNameNotInWatchList(name, watchListId);
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

    /**
     * Deletes an item. Only its creator can do it, and only while no other user's watch list contains it
     * (their lists and progress would silently lose an entry), otherwise a ResourceInUseException is thrown.
     * The item is removed from the creator's own watch lists, along with every viewer's watched state for it.
     * Returns the deleted item so the caller can clean up its image.
     */
    @Transactional
    public Item deleteItem(@NonNull Long itemId, @NonNull User currentUser) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        if (!item.getCreator().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the item creator can delete it");
        }

        List<Long> otherUsersWatchListIds = watchListItemRepository.findWatchListIdsWithItemNotCreatedBy(itemId, currentUser.getId());
        if (!otherUsersWatchListIds.isEmpty()) {
            throw new ResourceInUseException("Item is used in watch lists created by other users: " + otherUsersWatchListIds);
        }

        // Children first so no foreign key points to a removed row; item_categories rows go with the item
        watchListViewItemRepository.deleteByItemId(itemId);
        watchListItemRepository.deleteByItemId(itemId);
        itemRepository.delete(item);
        return item;
    }

}
