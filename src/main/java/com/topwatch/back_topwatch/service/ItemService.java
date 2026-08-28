package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.Item;
import com.topwatch.back_topwatch.exception.BadRequestException;
import com.topwatch.back_topwatch.repository.ItemRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public Optional<Item> findItemById(@NonNull Long id){ return itemRepository.findById(id);}

    public List<Item> findAllItems() {return itemRepository.findAll();}

    public List<Item> findItemsByName(@NonNull String name) {
        return itemRepository.findByNameContainingIgnoreCase(name);
    }

    public Item saveItem(@NonNull Item item){
        if (!StringUtils.hasText(item.getName())) {
            throw new BadRequestException("Item name is required");
        }
        if (item.getCreator() == null) {
            throw new BadRequestException("Item creator is required");
        }

        return itemRepository.save(item);
    }

}
