package io.rashed.finance.infrastructure.persistence.entity;

import io.rashed.finance.common.enums.ReconciliationStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cash_reconciliations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CashReconciliationEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    @Column(name = "reconciliation_date", nullable = false)
    private LocalDate reconciliationDate;

    @Column(name = "expected_cash_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal expectedCashAmount;

    @Column(name = "actual_cash_amount", precision = 18, scale = 2)
    private BigDecimal actualCashAmount;

    @Column(name = "difference_amount", precision = 18, scale = 2)
    private BigDecimal differenceAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReconciliationStatus status;

    @Column(name = "adjustment_transaction_id")
    private UUID adjustmentTransactionId;

    @Column
    private String notes;

    @OneToMany(mappedBy = "reconciliation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("snapshotTime ASC")
    private List<CashSnapshotEntity> snapshots = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public CashReconciliationEntity(
            UUID id,
            UUID accountId,
            LocalDate reconciliationDate,
            BigDecimal expectedCashAmount,
            BigDecimal actualCashAmount,
            BigDecimal differenceAmount,
            ReconciliationStatus status,
            UUID adjustmentTransactionId,
            String notes,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.accountId = accountId;
        this.reconciliationDate = reconciliationDate;
        this.expectedCashAmount = expectedCashAmount;
        this.actualCashAmount = actualCashAmount;
        this.differenceAmount = differenceAmount;
        this.status = status;
        this.adjustmentTransactionId = adjustmentTransactionId;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void addSnapshot(CashSnapshotEntity snapshot) {
        snapshots.add(snapshot);
    }

    public void replaceSnapshots(List<CashSnapshotEntity> newSnapshots) {
        snapshots.clear();
        snapshots.addAll(newSnapshots);
    }
}
