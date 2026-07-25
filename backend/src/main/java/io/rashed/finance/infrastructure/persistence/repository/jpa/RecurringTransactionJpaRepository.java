package io.rashed.finance.infrastructure.persistence.repository.jpa;

import io.rashed.finance.infrastructure.persistence.entity.RecurringTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecurringTransactionJpaRepository extends JpaRepository<RecurringTransactionEntity, UUID> {

    List<RecurringTransactionEntity> findAllByOrderByNameAsc();

    List<RecurringTransactionEntity> findByActiveTrueOrderByNameAsc();

    List<RecurringTransactionEntity> findByActiveTrueAndAutoGenerateTrueAndNextExecutionDateLessThanEqual(LocalDate date);

    List<RecurringTransactionEntity> findByActiveTrueAndNextExecutionDateLessThanEqual(LocalDate date);
}
