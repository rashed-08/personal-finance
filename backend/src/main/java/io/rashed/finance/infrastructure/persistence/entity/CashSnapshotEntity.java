package io.rashed.finance.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cash_snapshots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CashSnapshotEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reconciliation_id", nullable = false, updatable = false)
    private CashReconciliationEntity reconciliation;

    @Column(name = "snapshot_time", nullable = false)
    private LocalDateTime snapshotTime;

    @Column(name = "cash_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal cashAmount;

    @Column(length = 500)
    private String notes;

    public CashSnapshotEntity(
            UUID id,
            CashReconciliationEntity reconciliation,
            LocalDateTime snapshotTime,
            BigDecimal cashAmount,
            String notes
    ) {
        this.id = id;
        this.reconciliation = reconciliation;
        this.snapshotTime = snapshotTime;
        this.cashAmount = cashAmount;
        this.notes = notes;
    }
}
