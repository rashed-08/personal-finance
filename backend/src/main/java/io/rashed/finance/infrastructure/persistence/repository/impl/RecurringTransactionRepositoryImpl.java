package io.rashed.finance.infrastructure.persistence.repository.impl;

import io.rashed.finance.domain.recurring.RecurringTransaction;
import io.rashed.finance.domain.recurring.RecurringTransactionId;
import io.rashed.finance.domain.recurring.RecurringTransactionRepository;
import io.rashed.finance.infrastructure.persistence.mapper.RecurringTransactionEntityMapper;
import io.rashed.finance.infrastructure.persistence.repository.jpa.RecurringTransactionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RecurringTransactionRepositoryImpl implements RecurringTransactionRepository {

    private final RecurringTransactionJpaRepository jpaRepository;

    @Override
    public RecurringTransaction save(RecurringTransaction recurringTransaction) {

        return RecurringTransactionEntityMapper.toDomain(
                jpaRepository.save(
                        RecurringTransactionEntityMapper.toEntity(recurringTransaction)
                )
        );
    }

    @Override
    public Optional<RecurringTransaction> findById(RecurringTransactionId id) {

        return jpaRepository.findById(id.getValue())
                .map(RecurringTransactionEntityMapper::toDomain);
    }

    @Override
    public List<RecurringTransaction> findAll() {

        return jpaRepository.findAllByOrderByNameAsc()
                .stream()
                .map(RecurringTransactionEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<RecurringTransaction> findActive() {

        return jpaRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(RecurringTransactionEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<RecurringTransaction> findDue(LocalDate asOfDate) {

        return jpaRepository.findByActiveTrueAndNextExecutionDateLessThanEqual(asOfDate)
                .stream()
                .map(RecurringTransactionEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<RecurringTransaction> findDueForAutoGeneration(LocalDate asOfDate) {

        return jpaRepository.findByActiveTrueAndAutoGenerateTrueAndNextExecutionDateLessThanEqual(asOfDate)
                .stream()
                .map(RecurringTransactionEntityMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(RecurringTransactionId id) {

        jpaRepository.deleteById(id.getValue());
    }

    @Override
    public boolean existsById(RecurringTransactionId id) {

        return jpaRepository.existsById(id.getValue());
    }
}
