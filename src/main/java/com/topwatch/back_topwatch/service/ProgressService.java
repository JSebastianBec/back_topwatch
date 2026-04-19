package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.Progress;
import com.topwatch.back_topwatch.repository.ProgressRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ProgressRepository progressRepository;

    public Progress save(@NonNull Progress progress) {
        return progressRepository.save(progress);
    }

    public Optional<Progress> findById(@NonNull Long id) {
        return progressRepository.findById(id);
    }

    public List<Progress> findByUserId(@NonNull Long userId) {
        return progressRepository.findByUserId(userId);
    }

    public List<Progress> findByListId(@NonNull Long listId) {
        return progressRepository.findByListTopId(listId);
    }

    public void delete(@NonNull Long id) {
        progressRepository.deleteById(id);
    }

}
