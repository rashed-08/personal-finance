package io.rashed.finance.infrastructure.persistence.repository.impl;

import io.rashed.finance.domain.users.EmailVerificationToken;
import io.rashed.finance.domain.users.EmailVerificationTokenRepository;
import io.rashed.finance.infrastructure.persistence.mapper.EmailVerificationTokenEntityMapper;
import io.rashed.finance.infrastructure.persistence.repository.jpa.EmailVerificationTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmailVerificationTokenRepositoryImpl implements EmailVerificationTokenRepository {

    private final EmailVerificationTokenJpaRepository jpaRepository;

    @Override
    public EmailVerificationToken save(EmailVerificationToken token) {

        return EmailVerificationTokenEntityMapper.toDomain(
                jpaRepository.save(
                        EmailVerificationTokenEntityMapper.toEntity(token)
                )
        );
    }

    @Override
    public Optional<EmailVerificationToken> findByTokenHash(String tokenHash) {

        return jpaRepository.findByTokenHash(tokenHash)
                .map(EmailVerificationTokenEntityMapper::toDomain);
    }
}
