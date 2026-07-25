package io.rashed.finance.infrastructure.persistence.repository.impl;

import io.rashed.finance.domain.recurring.RecurringTransactionExecution;
import io.rashed.finance.domain.recurring.RecurringTransactionExecutionRepository;
import io.rashed.finance.domain.recurring.RecurringTransactionId;
import io.rashed.finance.infrastructure.persistence.mapper.RecurringTransactionExecutionEntityMapper;
import io.rashed.finance.infrastructure.persistence.repository.jpa.RecurringTransactionExecutionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RecurringTransactionExecutionRepositoryImpl implements RecurringTransactionExecutionRepository {

    private final RecurringTransactionExecutionJpaRepository jpaRepository;

    @Override
    public RecurringTransactionExecution save(RecurringTransactionExecution execution) {

        return RecurringTransactionExecutionEntityMapper.toDomain(
                jpaRepository.save(
                        RecurringTransactionExecutionEntityMapper.toEntity(execution)
                )
        );
    }

    @Override
    public List<RecurringTransactionExecution> findByRecurringTransactionId(RecurringTransactionId recurringTransactionId) {

        return jpaRepository.findByRecurringTransactionIdOrderByScheduledDateDesc(recurringTransactionId.getValue())
                .stream()
                .map(RecurringTransactionExecutionEntityMapper::toDomain)
                .toList();
    }
}
