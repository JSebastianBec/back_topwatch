package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.ListTop;
import com.topwatch.back_topwatch.repository.ListRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ListService {

    private final ListRepository listRepository;

    public ListTop saveOrUpdateListTop(@NonNull ListTop listTop){
        return listRepository.save(listTop);
    }

    public void deleteListTop(@NonNull ListTop listTop){
        listRepository.delete(listTop);
    }

    public Optional<ListTop> findListTop(@NonNull Long id){
        return listRepository.findById(id);
    }

    public List<ListTop> findAllListTop(){
        return listRepository.findAll();
    }
}
