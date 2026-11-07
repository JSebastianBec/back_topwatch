package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.Item;
import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.domain.WatchList;
import com.topwatch.back_topwatch.domain.WatchListItem;
import com.topwatch.back_topwatch.exception.BadRequestException;
import com.topwatch.back_topwatch.exception.DuplicateResourceException;
import com.topwatch.back_topwatch.exception.ResourceNotFoundException;
import com.topwatch.back_topwatch.repository.ItemRepository;
import com.topwatch.back_topwatch.repository.WatchListItemRepository;
import com.topwatch.back_topwatch.repository.WatchListRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WatchListService {

    private final WatchListRepository watchListRepository;
    private final WatchListItemRepository watchListItemRepository;
    private final ItemRepository itemRepository;

    public List<WatchList> findAllWatchLists() {
        return watchListRepository.findAll();
    }

    public List<WatchList> findWatchListsByCreator(@NonNull User creator) {
        return watchListRepository.findByCreator_Id(creator.getId());
    }

    public Optional<WatchList> findWatchListById(@NonNull Long id) {
        return watchListRepository.findById(id);
    }

    public List<WatchListItem> findItemsByWatchListId(@NonNull Long watchListId) {
        return watchListItemRepository.findByWatchList_Id(watchListId);
    }

    public WatchList saveWatchList(@NonNull WatchList watchList) {
        if (!StringUtils.hasText(watchList.getName())) {
            throw new BadRequestException("Watch list name is required");
        }
        if (watchList.getCreator() == null) {
            throw new BadRequestException("Watch list creator is required");
        }

        return watchListRepository.save(watchList);
    }

    @Transactional
    public List<WatchListItem> addItemsToWatchList(@NonNull Long watchListId, @NonNull List<Long> itemIds, @NonNull User currentUser) {
        WatchList watchList = watchListRepository.findById(watchListId)
                .orElseThrow(() -> new ResourceNotFoundException("Watch list not found"));
        if (!watchList.getCreator().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the watch list creator can add items to it");
        }

        if (itemIds.isEmpty()) {
            throw new BadRequestException("At least one item id is required");
        }
        if (itemIds.stream().anyMatch(Objects::isNull)) {
            throw new BadRequestException("Item id is required");
        }
        Set<Long> uniqueItemIds = new LinkedHashSet<>(itemIds);
        if (uniqueItemIds.size() != itemIds.size()) {
            throw new BadRequestException("Each item can only appear once per request");
        }

        Map<Long, Item> itemsById = itemRepository.findAllById(uniqueItemIds).stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));
        List<Long> missingItemIds = uniqueItemIds.stream()
                .filter(itemId -> !itemsById.containsKey(itemId))
                .toList();
        if (!missingItemIds.isEmpty()) {
            throw new ResourceNotFoundException("Items not found: " + missingItemIds);
        }

        List<Long> alreadyLinkedItemIds = watchListItemRepository.findByWatchList_IdAndItem_IdIn(watchListId, uniqueItemIds).stream()
                .map(watchListItem -> watchListItem.getItem().getId())
                .toList();
        if (!alreadyLinkedItemIds.isEmpty()) {
            throw new DuplicateResourceException("Items already in this watch list: " + alreadyLinkedItemIds);
        }

        // New entries start without a description; watched state lives in each user's WatchListView
        List<WatchListItem> watchListItems = itemIds.stream()
                .map(itemId -> WatchListItem.builder()
                        .creator(currentUser)
                        .watchList(watchList)
                        .item(itemsById.get(itemId))
                        .build())
                .toList();

        return watchListItemRepository.saveAll(watchListItems);
    }

}
