package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.Item;
import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.domain.WatchList;
import com.topwatch.back_topwatch.dto.UpdateUserRequest;
import com.topwatch.back_topwatch.exception.DuplicateResourceException;
import com.topwatch.back_topwatch.repository.ContentReportRepository;
import com.topwatch.back_topwatch.repository.ItemRepository;
import com.topwatch.back_topwatch.repository.SavedWatchListRepository;
import com.topwatch.back_topwatch.repository.UserBlockRepository;
import com.topwatch.back_topwatch.repository.UserRepository;
import com.topwatch.back_topwatch.repository.WatchListItemRepository;
import com.topwatch.back_topwatch.repository.WatchListRepository;
import com.topwatch.back_topwatch.repository.WatchListViewItemRepository;
import com.topwatch.back_topwatch.repository.WatchListViewRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageStorageService imageStorageService;
    private final WatchListRepository watchListRepository;
    private final WatchListItemRepository watchListItemRepository;
    private final WatchListViewRepository watchListViewRepository;
    private final WatchListViewItemRepository watchListViewItemRepository;
    private final SavedWatchListRepository savedWatchListRepository;
    private final ItemRepository itemRepository;
    private final UserBlockRepository userBlockRepository;
    private final ContentReportRepository contentReportRepository;

    @Transactional
    public User update(@NonNull User currentUser, UpdateUserRequest request) {
        if (StringUtils.hasText(request.name()))      currentUser.setName(request.name());
        if (StringUtils.hasText(request.lastname()))  currentUser.setLastname(request.lastname());
        if (request.gender() != null)                 currentUser.setGender(request.gender());
        if (StringUtils.hasText(request.avatarURL()) && !request.avatarURL().equals(currentUser.getAvatarURL())) {
            imageStorageService.validateImageUrl(request.avatarURL(), currentUser.getId());
            String oldUrl = currentUser.getAvatarURL();
            currentUser.setAvatarURL(request.avatarURL());
            imageStorageService.deleteIfOwned(oldUrl, currentUser.getId()); // removes the old S3 file
        }

        if (StringUtils.hasText(request.nickname()) && !request.nickname().equals(currentUser.getNickname())) {
            if (userRepository.existsByNicknameAndIdNot(request.nickname(), currentUser.getId())) {
                throw new DuplicateResourceException("Nickname is already in use");
            }
            currentUser.setNickname(request.nickname());
        }

        if (StringUtils.hasText(request.email()) && !request.email().equals(currentUser.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(request.email(), currentUser.getId())) {
                throw new DuplicateResourceException("Email is already in use");
            }
            currentUser.setEmail(request.email());
        }

        return userRepository.save(currentUser);
    }

    public Optional<User> findUserById(@NonNull Long id){
        return userRepository.findById(id);
    }

    /**
     * Deletes an account and the user's personal data: profile, progress, saved lists, and the watch lists
     * and items nobody else uses. Watch lists and items other users depend on stay available, so the user
     * row they point to is kept with every personal field wiped and no way to log in.
     * Returns the image URLs that are no longer used so the caller can remove the files.
     */
    @Transactional
    public List<String> delete(@NonNull User user) {
        Long userId = user.getId();
        List<String> unusedImages = new ArrayList<>();

        // Children first so no foreign key points to a removed row
        watchListViewItemRepository.deleteByViewerId(userId);
        watchListViewRepository.deleteByViewerId(userId);
        savedWatchListRepository.deleteByUserId(userId);
        userBlockRepository.deleteByUserId(userId);
        contentReportRepository.deleteByReporterId(userId);

        for (WatchList watchList : watchListRepository.findByCreator_Id(userId)) {
            Long watchListId = watchList.getId();
            if (watchListViewRepository.existsByWatchList_Id(watchListId)
                    || savedWatchListRepository.existsByWatchList_Id(watchListId)) {
                continue;
            }
            watchListItemRepository.deleteByWatchListId(watchListId);
            watchListRepository.delete(watchList);
        }

        for (Item item : itemRepository.findByCreator_Id(userId)) {
            if (watchListItemRepository.existsByItem_Id(item.getId())) {
                continue;
            }
            if (item.getUrlAvatar() != null) unusedImages.add(item.getUrlAvatar());
            itemRepository.delete(item);
        }

        if (user.getAvatarURL() != null) unusedImages.add(user.getAvatarURL());

        boolean othersDependOnUser = watchListRepository.existsByCreator_Id(userId)
                || itemRepository.existsByCreator_Id(userId)
                || watchListItemRepository.existsByCreator_Id(userId);
        if (othersDependOnUser) {
            anonymize(user);
        } else {
            userRepository.delete(user);
        }
        return unusedImages;
    }

    private void anonymize(User user) {
        user.setName(null);
        user.setLastname(null);
        user.setGender(null);
        user.setAvatarURL(null);
        user.setTermsAcceptedAt(null);
        user.setTermsVersion(null);
        user.setNickname("deleted-user-" + user.getId());
        user.setEmail("deleted-user-" + user.getId() + "@deleted.invalid");
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        userRepository.save(user);
    }

    public List<User> findAllUsers(){
        return userRepository.findAll();
    }

}
