package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.ListTop;
import com.topwatch.back_topwatch.domain.Progress;
import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.dto.SaveProgressRequest;
import com.topwatch.back_topwatch.repository.ProgressRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ProgressRepository progressRepository;

    public Progress save(@NonNull Progress progress) {
        return progressRepository.save(progress);
    }

    public Progress saveFromRequest(@NonNull SaveProgressRequest request) {
        @NonNull Progress progress = Progress.builder()
                .id(request.id())
                .user(User.builder().id(request.userId()).build())
                .listTop(ListTop.builder().id(request.listTopId()).build())
                .itemsFinished(request.itemsFinished())
                .creationDate(request.id() == null ? new Date() : null)
                .lastUpdated(new Date())
                .build();
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
