package io.rashed.finance.infrastructure.persistence.repository.impl;

import io.rashed.finance.domain.users.GoogleOAuthScope;
import io.rashed.finance.domain.users.GoogleOAuthToken;
import io.rashed.finance.domain.users.GoogleOAuthTokenRepository;
import io.rashed.finance.domain.users.UserId;
import io.rashed.finance.infrastructure.persistence.mapper.GoogleOAuthTokenEntityMapper;
import io.rashed.finance.infrastructure.persistence.repository.jpa.GoogleOAuthTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GoogleOAuthTokenRepositoryImpl implements GoogleOAuthTokenRepository {

    private final GoogleOAuthTokenJpaRepository jpaRepository;

    @Override
    public GoogleOAuthToken save(GoogleOAuthToken token) {

        return GoogleOAuthTokenEntityMapper.toDomain(
                jpaRepository.save(
                        GoogleOAuthTokenEntityMapper.toEntity(token)
                )
        );
    }

    @Override
    public Optional<GoogleOAuthToken> findByUserAndScope(UserId userId, GoogleOAuthScope scope) {

        return jpaRepository.findByUserIdAndScope(userId.getValue(), scope)
                .map(GoogleOAuthTokenEntityMapper::toDomain);
    }

    @Override
    public Optional<GoogleOAuthToken> findAnyByScope(GoogleOAuthScope scope) {

        return jpaRepository.findFirstByScopeOrderByConnectedAtDesc(scope)
                .map(GoogleOAuthTokenEntityMapper::toDomain);
    }

    /**
     * Derived delete queries need a transaction of their own when nothing
     * else supplies one — disconnecting Drive is a single call from the
     * controller.
     */
    @Override
    @Transactional
    public void deleteByUserAndScope(UserId userId, GoogleOAuthScope scope) {

        jpaRepository.deleteByUserIdAndScope(userId.getValue(), scope);
    }
}
