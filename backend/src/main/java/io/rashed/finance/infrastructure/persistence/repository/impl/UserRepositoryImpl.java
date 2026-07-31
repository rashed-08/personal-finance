package io.rashed.finance.infrastructure.persistence.repository.impl;

import io.rashed.finance.domain.users.User;
import io.rashed.finance.domain.users.UserId;
import io.rashed.finance.domain.users.UserRepository;
import io.rashed.finance.infrastructure.persistence.mapper.UserEntityMapper;
import io.rashed.finance.infrastructure.persistence.repository.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public User save(User user) {

        return UserEntityMapper.toDomain(
                jpaRepository.save(
                        UserEntityMapper.toEntity(user)
                )
        );
    }

    @Override
    public Optional<User> findById(UserId id) {

        return jpaRepository.findById(id.getValue())
                .map(UserEntityMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {

        return jpaRepository.findByEmailIgnoreCase(email)
                .map(UserEntityMapper::toDomain);
    }

    @Override
    public Optional<User> findByProviderSubject(String providerSubject) {

        return jpaRepository.findByProviderSubject(providerSubject)
                .map(UserEntityMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {

        return jpaRepository.existsByEmailIgnoreCase(email);
    }
}
