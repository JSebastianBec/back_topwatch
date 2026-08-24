package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.domain.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    // Same key format used in application.yml: Base64, long enough for HS256/384/512.
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 900_000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604_800_000L);

        user = User.builder()
                .id(1L)
                .email("user@topwatch.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();
    }

    @Test
    void generateAccessToken_containsSubjectAndJti() {
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo(user.getEmail());
        assertThat(jwtService.extractJti(token)).isNotBlank();
    }

    @Test
    void generateAccessTokenAndRefreshToken_haveDifferentJti() {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        assertThat(jwtService.extractJti(accessToken)).isNotEqualTo(jwtService.extractJti(refreshToken));
    }

    @Test
    void generateAccessToken_calledTwice_producesDifferentJti() {
        String first = jwtService.generateAccessToken(user);
        String second = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractJti(first)).isNotEqualTo(jwtService.extractJti(second));
    }

    @Test
    void extractExpiration_isInTheFuture() {
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractExpiration(token)).isAfter(new Date());
    }

    @Test
    void isTokenValid_returnsTrue_forMatchingUserAndUnexpiredToken() {
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalse_forDifferentUser() {
        String token = jwtService.generateAccessToken(user);
        User otherUser = User.builder()
                .id(2L)
                .email("other@topwatch.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void isTokenValid_returnsFalse_forExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1_000L);
        String expiredToken = jwtService.generateAccessToken(user);

        assertThat(jwtService.isTokenValid(expiredToken, user)).isFalse();
    }

    @Test
    void extractUsernameAndExpiration_doNotThrow_forExpiredToken() {
        // Regression test: JJWT throws ExpiredJwtException while parsing an expired token,
        // so every claim extractor must keep working instead of propagating that exception.
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1_000L);
        String expiredToken = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractUsername(expiredToken)).isEqualTo(user.getEmail());
        assertThat(jwtService.extractExpiration(expiredToken)).isBefore(new Date());
    }
}
