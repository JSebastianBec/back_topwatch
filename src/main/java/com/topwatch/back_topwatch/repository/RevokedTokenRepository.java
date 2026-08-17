package com.topwatch.back_topwatch.repository;

import com.topwatch.back_topwatch.domain.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {

    boolean existsByJti(String jti);

    void deleteByExpiresAtBefore(Instant instant);

}
