package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.RevokedToken;
import com.topwatch.back_topwatch.repository.RevokedTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private RevokedTokenRepository revokedTokenRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void revoke_savesToken_whenJtiIsNotAlreadyRevoked() {
        String token = "some-token";
        when(jwtService.extractJti(token)).thenReturn("jti-1");
        when(revokedTokenRepository.existsByJti("jti-1")).thenReturn(false);
        when(jwtService.extractExpiration(token)).thenReturn(Date.from(Instant.now().plusSeconds(60)));

        tokenBlacklistService.revoke(token);

        ArgumentCaptor<RevokedToken> captor = ArgumentCaptor.forClass(RevokedToken.class);
        verify(revokedTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getJti()).isEqualTo("jti-1");
        assertThat(captor.getValue().getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void revoke_doesNothing_whenJtiIsAlreadyRevoked() {
        String token = "some-token";
        when(jwtService.extractJti(token)).thenReturn("jti-1");
        when(revokedTokenRepository.existsByJti("jti-1")).thenReturn(true);

        tokenBlacklistService.revoke(token);

        verify(revokedTokenRepository, never()).save(any());
    }

    @Test
    void revoke_doesNothing_whenJtiIsNull() {
        String token = "malformed-token";
        when(jwtService.extractJti(token)).thenReturn(null);

        tokenBlacklistService.revoke(token);

        verify(revokedTokenRepository, never()).save(any());
    }

    @Test
    void isRevoked_returnsTrue_whenJtiExists() {
        when(revokedTokenRepository.existsByJti("jti-1")).thenReturn(true);

        assertThat(tokenBlacklistService.isRevoked("jti-1")).isTrue();
    }

    @Test
    void isRevoked_returnsFalse_whenJtiIsNull() {
        assertThat(tokenBlacklistService.isRevoked(null)).isFalse();
        verify(revokedTokenRepository, never()).existsByJti(any());
    }

    @Test
    void isRevoked_returnsFalse_whenJtiNotFound() {
        when(revokedTokenRepository.existsByJti("jti-1")).thenReturn(false);

        assertThat(tokenBlacklistService.isRevoked("jti-1")).isFalse();
    }

    @Test
    void purgeExpired_deletesTokensExpiredBeforeNow() {
        tokenBlacklistService.purgeExpired();

        ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
        verify(revokedTokenRepository).deleteByExpiresAtBefore(captor.capture());
        assertThat(captor.getValue()).isCloseTo(Instant.now(), org.assertj.core.api.Assertions.within(5, java.time.temporal.ChronoUnit.SECONDS));
    }
}
