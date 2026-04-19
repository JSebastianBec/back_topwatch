package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.dto.UpdateUserRequest;
import com.topwatch.back_topwatch.repository.UserRepository;

import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User update(@NonNull User currentUser, UpdateUserRequest request) {
        if (request.name() != null)      currentUser.setName(request.name());
        if (request.lastname() != null)  currentUser.setLastname(request.lastname());
        if (request.nickname() != null)  currentUser.setNickname(request.nickname());
        if (request.gender() != null)    currentUser.setGender(request.gender());
        if (request.email() != null)     currentUser.setEmail(request.email());
        if (request.avatarURL() != null) currentUser.setAvatarURL(request.avatarURL());
        if (request.password() != null)  currentUser.setPassword(passwordEncoder.encode(request.password()));

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
