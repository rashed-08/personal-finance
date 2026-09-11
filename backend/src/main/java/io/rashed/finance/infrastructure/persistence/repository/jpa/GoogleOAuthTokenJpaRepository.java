package io.rashed.finance.infrastructure.persistence.repository.jpa;

import io.rashed.finance.domain.users.GoogleOAuthScope;
import io.rashed.finance.infrastructure.persistence.entity.GoogleOAuthTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoogleOAuthTokenJpaRepository extends JpaRepository<GoogleOAuthTokenEntity, UUID> {

    Optional<GoogleOAuthTokenEntity> findByUserIdAndScope(UUID userId, GoogleOAuthScope scope);

    Optional<GoogleOAuthTokenEntity> findFirstByScopeOrderByConnectedAtDesc(GoogleOAuthScope scope);

    void deleteByUserIdAndScope(UUID userId, GoogleOAuthScope scope);
}
