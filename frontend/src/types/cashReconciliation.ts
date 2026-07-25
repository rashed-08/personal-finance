export type ReconciliationStatus = "PENDING" | "COMPLETED";

export interface CashSnapshot {
    id: string;

    cashAmount: number;

    notes: string | null;

    snapshotTime: string;
}

export interface CashReconciliation {
    id: string;

    accountId: string;

    reconciliationDate: string;

    expectedCashAmount: number;

    /** Null until at least one snapshot has been recorded. */
    actualCashAmount: number | null;

    /** actualCashAmount - expectedCashAmount. Null under the same condition. */
    differenceAmount: number | null;

    status: ReconciliationStatus;

    adjustmentTransactionId: string | null;

    notes: string | null;

    snapshots: CashSnapshot[];

    createdAt: string;

    updatedAt: string;
}

export interface StartReconciliationRequest {
    accountId: string;

    reconciliationDate: string;

    notes?: string;
}

export interface RecordCashSnapshotRequest {
    cashAmount: number;

    notes?: string;
}
