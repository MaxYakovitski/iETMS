package com.mayak.ietms.infrastructure.security.auth.persistence;

import com.mayak.ietms.infrastructure.security.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    boolean existsByUserIdAndRevokedFalseAndExpiresAtAfter(Long userId, Instant expiresAt);

    @Modifying(clearAutomatically = true, flushAutomatically =  true)
    @Transactional
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.userId = :userId AND r.revoked = false")
    void revokeAllByUserId(@Param("userId") Long userId);
}