package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.WatchList;
import com.topwatch.back_topwatch.domain.WatchListItem;
import com.topwatch.back_topwatch.exception.BadRequestException;
import com.topwatch.back_topwatch.repository.WatchListItemRepository;
import com.topwatch.back_topwatch.repository.WatchListRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WatchListService {

    private final WatchListRepository watchListRepository;
    private final WatchListItemRepository watchListItemRepository;

    public List<WatchList> findAllWatchLists() {
        return watchListRepository.findAll();
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

}
