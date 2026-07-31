package io.rashed.finance.infrastructure.persistence.repository.impl;

import io.rashed.finance.domain.users.PasswordResetToken;
import io.rashed.finance.domain.users.PasswordResetTokenRepository;
import io.rashed.finance.infrastructure.persistence.mapper.PasswordResetTokenEntityMapper;
import io.rashed.finance.infrastructure.persistence.repository.jpa.PasswordResetTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PasswordResetTokenRepositoryImpl implements PasswordResetTokenRepository {

    private final PasswordResetTokenJpaRepository jpaRepository;

    @Override
    public PasswordResetToken save(PasswordResetToken token) {

        return PasswordResetTokenEntityMapper.toDomain(
                jpaRepository.save(
                        PasswordResetTokenEntityMapper.toEntity(token)
                )
        );
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {

        return jpaRepository.findByTokenHash(tokenHash)
                .map(PasswordResetTokenEntityMapper::toDomain);
    }
}
