package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.Item;
import com.topwatch.back_topwatch.domain.SavedWatchList;
import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.domain.WatchList;
import com.topwatch.back_topwatch.domain.WatchListItem;
import com.topwatch.back_topwatch.exception.BadRequestException;
import com.topwatch.back_topwatch.exception.DuplicateResourceException;
import com.topwatch.back_topwatch.exception.ResourceNotFoundException;
import com.topwatch.back_topwatch.repository.ItemRepository;
import com.topwatch.back_topwatch.repository.SavedWatchListRepository;
import com.topwatch.back_topwatch.repository.WatchListItemRepository;
import com.topwatch.back_topwatch.repository.WatchListRepository;
import com.topwatch.back_topwatch.repository.WatchListViewItemRepository;
import com.topwatch.back_topwatch.repository.WatchListViewRepository;
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
    private final SavedWatchListRepository savedWatchListRepository;
    private final WatchListViewRepository watchListViewRepository;
    private final WatchListViewItemRepository watchListViewItemRepository;

    public List<WatchList> findAllWatchLists() {
        return watchListRepository.findAll();
    }

    public List<WatchList> findWatchListsByCreator(@NonNull User creator) {
        return watchListRepository.findByCreator_Id(creator.getId());
    }

    /** The user's own watch lists plus the ones from other users they added. */
    public List<WatchList> findMyWatchLists(@NonNull User user) {
        return watchListRepository.findMineIncludingSaved(user.getId());
    }

    /**
     * Adds watch lists created by other users to the user's own lists. All are added or none:
     * fails on unknown ids, on the user's own lists and on lists already added.
     */
    @Transactional
    public List<WatchList> addToMyWatchLists(@NonNull List<Long> watchListIds, @NonNull User user) {
        if (watchListIds.isEmpty()) {
            throw new BadRequestException("At least one watch list id is required");
        }
        if (watchListIds.stream().anyMatch(Objects::isNull)) {
            throw new BadRequestException("Watch list id is required");
        }
        Set<Long> uniqueIds = new LinkedHashSet<>(watchListIds);
        if (uniqueIds.size() != watchListIds.size()) {
            throw new BadRequestException("Each watch list can only appear once per request");
        }

        Map<Long, WatchList> watchListsById = watchListRepository.findAllById(uniqueIds).stream()
                .collect(Collectors.toMap(WatchList::getId, Function.identity()));
        List<Long> missingIds = uniqueIds.stream()
                .filter(id -> !watchListsById.containsKey(id))
                .toList();
        if (!missingIds.isEmpty()) {
            throw new ResourceNotFoundException("Watch lists not found: " + missingIds);
        }

        List<Long> ownIds = uniqueIds.stream()
                .filter(id -> watchListsById.get(id).getCreator().getId().equals(user.getId()))
                .toList();
        if (!ownIds.isEmpty()) {
            throw new BadRequestException("These watch lists are already yours: " + ownIds);
        }

        List<Long> alreadyAddedIds = savedWatchListRepository.findByUser_IdAndWatchList_IdIn(user.getId(), uniqueIds).stream()
                .map(saved -> saved.getWatchList().getId())
                .toList();
        if (!alreadyAddedIds.isEmpty()) {
            throw new DuplicateResourceException("Watch lists already in your lists: " + alreadyAddedIds);
        }

        List<WatchList> watchLists = uniqueIds.stream().map(watchListsById::get).toList();
        savedWatchListRepository.saveAll(watchLists.stream()
                .map(watchList -> SavedWatchList.builder().user(user).watchList(watchList).build())
                .toList());
        return watchLists;
    }

    /** Lists from other users that the user has not added nor started yet. */
    public List<WatchList> findWatchListsNotStartedBy(@NonNull User viewer) {
        return watchListRepository.findNotStartedByViewer(viewer.getId());
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

    /**
     * Deletes a watch list. Only its creator can do it. Everything that depends on the list goes with it:
     * its entries, every user's view of it (with their watched state) and every "added to my lists" link.
     * The items themselves are kept.
     */
    @Transactional
    public void deleteWatchList(@NonNull Long watchListId, @NonNull User currentUser) {
        WatchList watchList = watchListRepository.findById(watchListId)
                .orElseThrow(() -> new ResourceNotFoundException("Watch list not found"));
        if (!watchList.getCreator().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the watch list creator can delete it");
        }

        // Children first so no foreign key points to a removed row
        watchListViewItemRepository.deleteByWatchListId(watchListId);
        watchListViewRepository.deleteByWatchListId(watchListId);
        savedWatchListRepository.deleteByWatchListId(watchListId);
        watchListItemRepository.deleteByWatchListId(watchListId);
        watchListRepository.delete(watchList);
    }

    /**
     * Removes items from a watch list. Only its creator can do it. Every viewer's watched state for those
     * entries goes with them. The items themselves are kept. All are removed or none: fails on unknown
     * ids and on items that are not in the list.
     */
    @Transactional
    public void removeItemsFromWatchList(@NonNull Long watchListId, @NonNull List<Long> itemIds, @NonNull User currentUser) {
        WatchList watchList = watchListRepository.findById(watchListId)
                .orElseThrow(() -> new ResourceNotFoundException("Watch list not found"));
        if (!watchList.getCreator().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the watch list creator can remove items from it");
        }

        if (itemIds.isEmpty()) {
            throw new BadRequestException("At least one item id is required");
        }
        if (itemIds.stream().anyMatch(Objects::isNull)) {
            throw new BadRequestException("Item id is required");
        }
        Set<Long> uniqueItemIds = new LinkedHashSet<>(itemIds);

        List<WatchListItem> entries = watchListItemRepository.findByWatchList_IdAndItem_IdIn(watchListId, uniqueItemIds);
        Set<Long> foundItemIds = entries.stream()
                .map(entry -> entry.getItem().getId())
                .collect(Collectors.toSet());
        List<Long> missingItemIds = uniqueItemIds.stream()
                .filter(itemId -> !foundItemIds.contains(itemId))
                .toList();
        if (!missingItemIds.isEmpty()) {
            throw new ResourceNotFoundException("Items not in this watch list: " + missingItemIds);
        }

        // Children first so no foreign key points to a removed row
        watchListViewItemRepository.deleteByWatchListItemIds(entries.stream().map(WatchListItem::getId).toList());
        watchListItemRepository.deleteAllInBatch(entries);
    }

}
