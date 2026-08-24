package com.topwatch.back_topwatch.controller;

import com.topwatch.back_topwatch.dto.AuthResponse;
import com.topwatch.back_topwatch.dto.LoginRequest;
import com.topwatch.back_topwatch.dto.LogoutRequest;
import com.topwatch.back_topwatch.dto.RegisterRequest;
import com.topwatch.back_topwatch.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authService);
    }

    @Test
    void register_returnsCreatedWithTokens() {
        RegisterRequest request = new RegisterRequest("sebas@topwatch.com", "password123", "sebas");
        AuthResponse expected = new AuthResponse("access-token", "refresh-token");
        when(authService.register(request)).thenReturn(expected);

        ResponseEntity<AuthResponse> response = authController.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void login_returnsOkWithTokens() {
        LoginRequest request = new LoginRequest("sebas@topwatch.com", "password123");
        AuthResponse expected = new AuthResponse("access-token", "refresh-token");
        when(authService.login(request)).thenReturn(expected);

        ResponseEntity<AuthResponse> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void refresh_returnsUnauthorized_whenHeaderIsMissing() {
        ResponseEntity<AuthResponse> response = authController.refresh(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(authService, never()).refreshToken(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void refresh_returnsUnauthorized_whenHeaderIsNotBearer() {
        ResponseEntity<AuthResponse> response = authController.refresh("Basic abc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void refresh_returnsNewTokens_whenHeaderIsValid() {
        AuthResponse expected = new AuthResponse("new-access-token", "new-refresh-token");
        when(authService.refreshToken("valid-refresh-token")).thenReturn(expected);

        ResponseEntity<AuthResponse> response = authController.refresh("Bearer valid-refresh-token");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void logout_returnsUnauthorized_whenHeaderIsMissing() {
        ResponseEntity<Void> response = authController.logout(null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(authService, never()).logout(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void logout_revokesAccessTokenOnly_whenBodyIsMissing() {
        ResponseEntity<Void> response = authController.logout("Bearer access-token", null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(authService).logout("access-token", null);
    }

    @Test
    void logout_revokesBothTokens_whenBodyIncludesRefreshToken() {
        ResponseEntity<Void> response = authController.logout("Bearer access-token", new LogoutRequest("refresh-token"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(authService).logout("access-token", "refresh-token");
    }
}
