package com.topwatch.back_topwatch.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public Item saveOrUpdateItem(@NonNull Item item){
        return itemRepository.save(item);
    }
    
    public void deleteItem(@NonNull Item item){
        itemRepository.delete(item);
    }
    
    public Optional<Item> getItem(@NonNull Long id){
        return itemRepository.findById(id);
    }
    
    public List<Item> getAllItem(){
        return itemRepository.findAll();
    }
}
