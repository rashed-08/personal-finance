package io.rashed.finance.infrastructure.persistence.repository.jpa;

import io.rashed.finance.common.enums.ReconciliationStatus;
import io.rashed.finance.infrastructure.persistence.entity.CashReconciliationEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CashReconciliationJpaRepository extends JpaRepository<CashReconciliationEntity, UUID> {

    List<CashReconciliationEntity> findByAccountId(UUID accountId);

    List<CashReconciliationEntity> findByReconciliationDateBetween(LocalDate fromDate, LocalDate toDate);

    boolean existsByAccountIdAndStatus(UUID accountId, ReconciliationStatus status);
}
