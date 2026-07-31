package io.rashed.finance.infrastructure.persistence.repository.jpa;

import io.rashed.finance.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            UPDATE RefreshTokenEntity t
            SET t.revokedAt = :now
            WHERE t.userId = :userId
              AND t.revokedAt IS NULL
            """)
    void revokeAllForUser(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
}
