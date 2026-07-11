package io.rashed.finance.infrastructure.persistence.repository.impl;

import io.rashed.finance.common.enums.AccountType;
import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.accounts.AccountRepository;
import io.rashed.finance.infrastructure.persistence.mapper.AccountEntityMapper;
import io.rashed.finance.infrastructure.persistence.repository.jpa.AccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepository {

    private final AccountJpaRepository jpaRepository;

    @Override
    public Account save(Account account) {

        return AccountEntityMapper.toDomain(
                jpaRepository.save(
                        AccountEntityMapper.toEntity(account)
                )
        );
    }

    @Override
    public List<Account> saveAll(List<Account> accounts) {

        return jpaRepository.saveAll(
                        accounts.stream()
                                .map(AccountEntityMapper::toEntity)
                                .toList()
                )
                .stream()
                .map(AccountEntityMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Account> findById(AccountId id) {

        return jpaRepository.findById(id.getValue())
                .map(AccountEntityMapper::toDomain);
    }

    @Override
    public Optional<Account> findByName(String name) {

        return jpaRepository.findByNameIgnoreCase(name)
                .map(AccountEntityMapper::toDomain);
    }

    @Override
    public boolean existsById(AccountId id) {

        return jpaRepository.existsById(id.getValue());
    }

    @Override
    public boolean existsByName(String name) {

        return jpaRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public List<Account> findAll() {

        return jpaRepository.findAllByOrderByNameAsc()
                .stream()
                .map(AccountEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<Account> findActive() {

        return jpaRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(AccountEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<Account> findInactive() {

        return jpaRepository.findByActiveFalseOrderByNameAsc()
                .stream()
                .map(AccountEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<Account> findByType(AccountType accountType) {

        return jpaRepository.findByAccountTypeOrderByNameAsc(accountType)
                .stream()
                .map(AccountEntityMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(AccountId id) {

        jpaRepository.deleteById(id.getValue());
    }
}