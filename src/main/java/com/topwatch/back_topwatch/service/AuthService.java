package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.domain.enums.Role;
import com.topwatch.back_topwatch.dto.AuthResponse;
import com.topwatch.back_topwatch.dto.LoginRequest;
import com.topwatch.back_topwatch.dto.RegisterRequest;
import com.topwatch.back_topwatch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthResponse register(RegisterRequest request) {
        var user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .role(Role.USER)
                .build();

        userRepository.save(user);

        return new AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user)
        );
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        var user = userRepository.findByEmail(request.email())
                .orElseThrow();

        return new AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user)
        );
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (tokenBlacklistService.isRevoked(jwtService.extractJti(refreshToken))) {
            throw new IllegalArgumentException("Refresh token has been revoked");
        }

        final String userEmail = jwtService.extractUsername(refreshToken);

        var user = userRepository.findByEmail(userEmail)
                .orElseThrow();

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        return new AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user)
        );
    }

    public void logout(String accessToken, String refreshToken) {
        tokenBlacklistService.revoke(accessToken);
        if (StringUtils.hasText(refreshToken)) {
            tokenBlacklistService.revoke(refreshToken);
        }
    }
}
