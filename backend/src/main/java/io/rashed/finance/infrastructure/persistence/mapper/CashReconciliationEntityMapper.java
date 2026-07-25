package io.rashed.finance.infrastructure.persistence.mapper;

import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.reconciliation.CashReconciliation;
import io.rashed.finance.domain.reconciliation.CashReconciliationId;
import io.rashed.finance.domain.reconciliation.CashSnapshot;
import io.rashed.finance.domain.reconciliation.CashSnapshotId;
import io.rashed.finance.domain.transactions.TransactionId;
import io.rashed.finance.infrastructure.persistence.entity.CashReconciliationEntity;
import io.rashed.finance.infrastructure.persistence.entity.CashSnapshotEntity;

import java.util.List;

public final class CashReconciliationEntityMapper {

    private CashReconciliationEntityMapper() {
    }

    public static CashReconciliationEntity toEntity(CashReconciliation reconciliation) {

        if (reconciliation == null) {
            return null;
        }

        CashReconciliationEntity entity = new CashReconciliationEntity(
                reconciliation.getId().getValue(),
                reconciliation.getAccountId().getValue(),
                reconciliation.getReconciliationDate(),
                reconciliation.getExpectedCashAmount().getAmount(),
                reconciliation.getActualCashAmount() == null ? null : reconciliation.getActualCashAmount().getAmount(),
                reconciliation.getDifference() == null ? null : reconciliation.getDifference().getAmount(),
                reconciliation.getStatus(),
                reconciliation.getAdjustmentTransactionId() == null ? null : reconciliation.getAdjustmentTransactionId().getValue(),
                reconciliation.getNotes(),
                reconciliation.getCreatedAt(),
                reconciliation.getUpdatedAt()
        );

        List<CashSnapshotEntity> snapshotEntities = reconciliation.getSnapshots().stream()
                .map(snapshot -> new CashSnapshotEntity(
                        snapshot.getId().getValue(),
                        entity,
                        snapshot.getSnapshotTime(),
                        snapshot.getCashAmount().getAmount(),
                        snapshot.getNotes()
                ))
                .toList();

        entity.replaceSnapshots(snapshotEntities);

        return entity;
    }

    public static CashReconciliation toDomain(CashReconciliationEntity entity) {

        if (entity == null) {
            return null;
        }

        List<CashSnapshot> snapshots = entity.getSnapshots().stream()
                .map(s -> new CashSnapshot(
                        CashSnapshotId.of(s.getId()),
                        Money.of(s.getCashAmount()),
                        s.getNotes(),
                        s.getSnapshotTime()
                ))
                .toList();

        return new CashReconciliation(
                CashReconciliationId.of(entity.getId()),
                AccountId.of(entity.getAccountId()),
                entity.getReconciliationDate(),
                Money.of(entity.getExpectedCashAmount()),
                entity.getActualCashAmount() == null ? null : Money.of(entity.getActualCashAmount()),
                entity.getDifferenceAmount() == null ? null : Money.of(entity.getDifferenceAmount()),
                entity.getStatus(),
                entity.getAdjustmentTransactionId() == null ? null : TransactionId.of(entity.getAdjustmentTransactionId()),
                entity.getNotes(),
                snapshots,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
