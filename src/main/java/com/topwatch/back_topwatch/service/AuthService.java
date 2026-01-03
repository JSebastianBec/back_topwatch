package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.Token;
import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.dto.LoginRequest;
import com.topwatch.back_topwatch.dto.RegisterRequest;
import com.topwatch.back_topwatch.dto.TokenResponse;
import com.topwatch.back_topwatch.repository.TokenRepository;
import com.topwatch.back_topwatch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    public TokenResponse register(RegisterRequest registerRequest) {
        User user = User.builder()
                .name(registerRequest.name())
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .build();
        User savedUser = userRepository.save(user);
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken);
        saveUserToken(savedUser, refreshToken);
        return new TokenResponse(jwtToken, refreshToken);
    }

    private void saveUserToken(User user, String jwtToken){
        Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(Token.TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    public TokenResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );
        User user = userRepository.findByEmail(loginRequest.email()).orElseThrow();
        String jwToken = jwtService.generateToken(user);
        String jwTokenRefresh = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwToken);
        saveUserToken(user, jwTokenRefresh);
        return new TokenResponse(jwToken, jwTokenRefresh);
    }

    private void revokeAllUserTokens(User user){
        final List<Token> validUserTokens = tokenRepository
                .findAllValidIsFalseOrRevokedIsFalseByUserId(user.getId());
        if(!validUserTokens.isEmpty()){
            for (final Token token: validUserTokens){
                token.setExpired(true);
                token.setRevoked(true);
            }
            tokenRepository.saveAll(validUserTokens);
        }
    }

    public TokenResponse refreshToken(final String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            throw new IllegalArgumentException("Invalid Bearer token");
        }
        final String refreshToken = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail == null){
            throw new IllegalArgumentException("Invalid refresh token");
        }
        
        final User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UsernameNotFoundException(userEmail));
        if (!jwtService.isTokenValid(refreshToken, user)){
            throw new IllegalArgumentException("Invalid refresh token");
        }
        String jwToken = jwtService.generateToken(user);
        String jwTokenRefresh = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwToken);
        saveUserToken(user, jwTokenRefresh);
        return new TokenResponse(jwToken, jwTokenRefresh);
    }

    public void logout(final String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Bearer token");
        }
        final String jwtToken = authHeader.substring(7);
        final Token foundToken = tokenRepository.findByToken(jwtToken)
                .orElseThrow(() -> new IllegalArgumentException("Token not found"));
        foundToken.setExpired(true);
        foundToken.setRevoked(true);
        tokenRepository.save(foundToken);
    }
}
