package io.rashed.finance.infrastructure.persistence.repository.jpa;

import io.rashed.finance.infrastructure.persistence.entity.RecurringTransactionExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecurringTransactionExecutionJpaRepository extends JpaRepository<RecurringTransactionExecutionEntity, UUID> {

    List<RecurringTransactionExecutionEntity> findByRecurringTransactionIdOrderByScheduledDateDesc(UUID recurringTransactionId);
}
