package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.dto.UpdateUserRequest;
import com.topwatch.back_topwatch.exception.DuplicateResourceException;
import com.topwatch.back_topwatch.repository.UserRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User update(@NonNull User currentUser, UpdateUserRequest request) {
        if (StringUtils.hasText(request.name()))      currentUser.setName(request.name());
        if (StringUtils.hasText(request.lastname()))  currentUser.setLastname(request.lastname());
        if (request.gender() != null)                 currentUser.setGender(request.gender());
        if (StringUtils.hasText(request.avatarURL())) currentUser.setAvatarURL(request.avatarURL());
        if (StringUtils.hasText(request.password()))  currentUser.setPassword(passwordEncoder.encode(request.password()));

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

    public void delete(@NonNull User currentUser){
        userRepository.delete(currentUser);
    }

    public List<User> findAllUsers(){
        return userRepository.findAll();
    }

}
