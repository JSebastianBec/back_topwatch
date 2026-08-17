package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.RevokedToken;
import com.topwatch.back_topwatch.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RevokedTokenRepository revokedTokenRepository;
    private final JwtService jwtService;

    @Transactional
    public void revoke(String token) {
        final String jti = jwtService.extractJti(token);
        if (jti == null || revokedTokenRepository.existsByJti(jti)) {
            return;
        }

        revokedTokenRepository.save(RevokedToken.builder()
                .jti(jti)
                .expiresAt(jwtService.extractExpiration(token).toInstant())
                .build());
    }

    public boolean isRevoked(String jti) {
        return jti != null && revokedTokenRepository.existsByJti(jti);
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpired() {
        revokedTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }

}
