package io.rashed.finance.domain.reconciliation;

import io.rashed.finance.common.valueobject.Money;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A single physical cash count recorded during a reconciliation session.
 * Child entity of {@link CashReconciliation} — snapshots have no meaning
 * outside their parent reconciliation and are never accessed independently.
 */
@Getter
@ToString
@EqualsAndHashCode(of = "id")
public final class CashSnapshot {

    private final CashSnapshotId id;

    private final Money cashAmount;

    private final String notes;

    private final LocalDateTime snapshotTime;

    public CashSnapshot(CashSnapshotId id, Money cashAmount, String notes, LocalDateTime snapshotTime) {

        this.id = Objects.requireNonNull(id);
        this.cashAmount = Objects.requireNonNull(cashAmount, "Cash amount cannot be null.");

        if (cashAmount.isNegative()) {
            throw new IllegalArgumentException("Cash amount cannot be negative.");
        }

        this.notes = notes;
        this.snapshotTime = Objects.requireNonNull(snapshotTime);
    }

    public static CashSnapshot record(Money cashAmount, String notes) {
        return new CashSnapshot(CashSnapshotId.newId(), cashAmount, notes, LocalDateTime.now());
    }
}
