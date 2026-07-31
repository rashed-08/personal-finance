package io.rashed.finance.infrastructure.persistence.repository.impl;

import io.rashed.finance.domain.users.RefreshToken;
import io.rashed.finance.domain.users.RefreshTokenRepository;
import io.rashed.finance.domain.users.UserId;
import io.rashed.finance.infrastructure.persistence.mapper.RefreshTokenEntityMapper;
import io.rashed.finance.infrastructure.persistence.repository.jpa.RefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {

        return RefreshTokenEntityMapper.toDomain(
                jpaRepository.save(
                        RefreshTokenEntityMapper.toEntity(refreshToken)
                )
        );
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {

        return jpaRepository.findByTokenHash(tokenHash)
                .map(RefreshTokenEntityMapper::toDomain);
    }

    @Override
    @Transactional
    public void revokeAllForUser(UserId userId) {

        jpaRepository.revokeAllForUser(userId.getValue(), LocalDateTime.now());
    }
}
