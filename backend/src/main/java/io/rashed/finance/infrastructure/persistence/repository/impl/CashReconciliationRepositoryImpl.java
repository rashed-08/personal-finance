package io.rashed.finance.infrastructure.persistence.repository.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import io.rashed.finance.common.enums.ReconciliationStatus;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.reconciliation.CashReconciliation;
import io.rashed.finance.domain.reconciliation.CashReconciliationId;
import io.rashed.finance.domain.reconciliation.CashReconciliationRepository;
import io.rashed.finance.infrastructure.persistence.mapper.CashReconciliationEntityMapper;
import io.rashed.finance.infrastructure.persistence.repository.jpa.CashReconciliationJpaRepository;

@Repository
public class CashReconciliationRepositoryImpl implements CashReconciliationRepository {

    private final CashReconciliationJpaRepository jpaRepository;

    public CashReconciliationRepositoryImpl(CashReconciliationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CashReconciliation save(CashReconciliation reconciliation) {

        return CashReconciliationEntityMapper.toDomain(
                jpaRepository.save(CashReconciliationEntityMapper.toEntity(reconciliation))
        );
    }

    @Override
    public Optional<CashReconciliation> findById(CashReconciliationId id) {

        return jpaRepository.findById(id.getValue())
                .map(CashReconciliationEntityMapper::toDomain);
    }

    @Override
    public List<CashReconciliation> findByAccount(AccountId accountId) {

        return jpaRepository.findByAccountId(accountId.getValue())
                .stream()
                .map(CashReconciliationEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<CashReconciliation> findByDateRange(LocalDate fromDate, LocalDate toDate) {

        return jpaRepository.findByReconciliationDateBetween(fromDate, toDate)
                .stream()
                .map(CashReconciliationEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<CashReconciliation> findAll() {

        return jpaRepository.findAll()
                .stream()
                .map(CashReconciliationEntityMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsPendingForAccount(AccountId accountId) {

        return jpaRepository.existsByAccountIdAndStatus(accountId.getValue(), ReconciliationStatus.PENDING);
    }
}
