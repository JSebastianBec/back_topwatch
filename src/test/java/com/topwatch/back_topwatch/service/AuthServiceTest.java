package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.domain.enums.Role;
import com.topwatch.back_topwatch.dto.AuthResponse;
import com.topwatch.back_topwatch.dto.LoginRequest;
import com.topwatch.back_topwatch.dto.RegisterRequest;
import com.topwatch.back_topwatch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("sebas@topwatch.com")
                .password("encoded-password")
                .nickname("sebas")
                .role(Role.USER)
                .build();
    }

    @Test
    void register_savesUserWithEncodedPasswordAndReturnsTokens() {
        RegisterRequest request = new RegisterRequest("sebas@topwatch.com", "plainPassword", "sebas");
        when(passwordEncoder.encode("plainPassword")).thenReturn("encoded-password");
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("sebas@topwatch.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getNickname()).isEqualTo("sebas");
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void login_authenticatesAndReturnsTokens() {
        LoginRequest request = new LoginRequest("sebas@topwatch.com", "plainPassword");
        when(userRepository.findByEmail("sebas@topwatch.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

        AuthResponse response = authService.login(request);

        verify(authenticationManager).authenticate(any());
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void login_propagatesException_whenCredentialsAreInvalid() {
        LoginRequest request = new LoginRequest("sebas@topwatch.com", "wrongPassword");
        org.mockito.Mockito.doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void refreshToken_returnsNewTokens_whenValid() {
        String refreshToken = "valid-refresh-token";
        when(jwtService.extractJti(refreshToken)).thenReturn("jti-1");
        when(tokenBlacklistService.isRevoked("jti-1")).thenReturn(false);
        when(jwtService.extractUsername(refreshToken)).thenReturn("sebas@topwatch.com");
        when(userRepository.findByEmail("sebas@topwatch.com")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(refreshToken, user)).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("new-refresh-token");

        AuthResponse response = authService.refreshToken(refreshToken);

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    void refreshToken_throwsIllegalArgumentException_whenTokenIsRevoked() {
        String refreshToken = "revoked-refresh-token";
        when(jwtService.extractJti(refreshToken)).thenReturn("jti-1");
        when(tokenBlacklistService.isRevoked("jti-1")).thenReturn(true);

        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("revoked");

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void refreshToken_throwsIllegalArgumentException_whenTokenIsInvalid() {
        String refreshToken = "stale-refresh-token";
        when(jwtService.extractJti(refreshToken)).thenReturn("jti-1");
        when(tokenBlacklistService.isRevoked("jti-1")).thenReturn(false);
        when(jwtService.extractUsername(refreshToken)).thenReturn("sebas@topwatch.com");
        when(userRepository.findByEmail("sebas@topwatch.com")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(refreshToken, user)).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid");
    }

    @Test
    void refreshToken_throwsNoSuchElementException_whenUserNoLongerExists() {
        String refreshToken = "orphan-refresh-token";
        when(jwtService.extractJti(refreshToken)).thenReturn("jti-1");
        when(tokenBlacklistService.isRevoked("jti-1")).thenReturn(false);
        when(jwtService.extractUsername(refreshToken)).thenReturn("ghost@topwatch.com");
        when(userRepository.findByEmail("ghost@topwatch.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void logout_revokesOnlyAccessToken_whenNoRefreshTokenProvided() {
        authService.logout("access-token", null);

        verify(tokenBlacklistService).revoke("access-token");
        verify(tokenBlacklistService, never()).revoke(eq("refresh-token"));
    }

    @Test
    void logout_revokesBothTokens_whenRefreshTokenProvided() {
        authService.logout("access-token", "refresh-token");

        verify(tokenBlacklistService).revoke("access-token");
        verify(tokenBlacklistService).revoke("refresh-token");
    }

    @Test
    void logout_ignoresBlankRefreshToken() {
        authService.logout("access-token", "   ");

        verify(tokenBlacklistService).revoke("access-token");
        verify(tokenBlacklistService, never()).revoke("   ");
    }
}
