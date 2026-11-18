package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.domain.UserBlock;
import com.topwatch.back_topwatch.dto.BlockedUserResponse;
import com.topwatch.back_topwatch.exception.BadRequestException;
import com.topwatch.back_topwatch.exception.ResourceNotFoundException;
import com.topwatch.back_topwatch.repository.UserBlockRepository;
import com.topwatch.back_topwatch.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final UserBlockRepository userBlockRepository;
    private final UserRepository userRepository;

    /** Blocking someone already blocked changes nothing. */
    @Transactional
    public void block(@NonNull Long blockedId, @NonNull User blocker) {
        if (blockedId.equals(blocker.getId())) {
            throw new BadRequestException("You cannot block yourself");
        }
        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!userBlockRepository.existsByBlocker_IdAndBlocked_Id(blocker.getId(), blockedId)) {
            userBlockRepository.save(UserBlock.builder().blocker(blocker).blocked(blocked).build());
        }
    }

    @Transactional
    public void unblock(@NonNull Long blockedId, @NonNull User blocker) {
        userBlockRepository.deleteByBlockerAndBlocked(blocker.getId(), blockedId);
    }

    @Transactional(readOnly = true)
    public List<BlockedUserResponse> findBlockedUsers(@NonNull User blocker) {
        return userBlockRepository.findByBlocker_IdOrderByCreationDateDesc(blocker.getId()).stream()
                .map(block -> new BlockedUserResponse(block.getBlocked().getId(), block.getBlocked().getNickname()))
                .toList();
    }

    /** Creators whose watch lists and items must stay hidden from the user. */
    @Transactional(readOnly = true)
    public Set<Long> findBlockedUserIds(@NonNull User blocker) {
        return userBlockRepository.findByBlocker_IdOrderByCreationDateDesc(blocker.getId()).stream()
                .map(block -> block.getBlocked().getId())
                .collect(Collectors.toSet());
    }

}
