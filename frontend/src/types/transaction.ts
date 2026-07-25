export type TransactionType =
    | "INCOME"
    | "EXPENSE"
    | "TRANSFER"
    | "ADJUSTMENT"
    | "OPENING_BALANCE"
    | "MIGRATION";

export type TransactionStatus = "POSTED" | "VOID" | "REVERSED";

export type AdjustmentReason =
    | "CASH_RECONCILIATION"
    | "OPENING_BALANCE"
    | "DATA_MIGRATION"
    | "MANUAL_CORRECTION"
    | "SYSTEM_CORRECTION"
    | "TRANSACTION_UPDATE";

export interface Transaction {
    id: string;

    transactionType: TransactionType;
    transactionStatus: TransactionStatus;
    transactionDate: string;
    amount: number;

    fromAccountId: string | null;
    toAccountId: string | null;
    categoryId: string | null;
    salaryCycleId: string | null;

    referenceNumber: string | null;
    adjustmentReason: AdjustmentReason | null;

    description: string | null;
    notes: string | null;

    createdAt: string;
    updatedAt: string;
}

export interface CreateTransactionRequest {
    transactionType: TransactionType;
    transactionDate: string;
    amount: number;

    fromAccountId?: string;
    toAccountId?: string;
    categoryId?: string;
    salaryCycleId?: string;

    description?: string;
    notes?: string;

    adjustmentReason?: AdjustmentReason;
    migrationBatchId?: string;
    referenceTransactionId?: string;

    /** INCOME only. Server resolves salaryCycleId and ignores it when set. */
    startsNewSalaryCycle?: boolean;
}

/**
 * Only the fields UpdateTransactionService actually applies. Amount changes
 * are recorded as a linked ADJUSTMENT transaction rather than rewriting
 * history — see docs/api/Transactions.md.
 */
export interface UpdateTransactionRequest {
    amount: number;
    categoryId?: string;
    description?: string;
    notes?: string;
}

export interface TransactionFilter {
    fromDate?: string;
    toDate?: string;
    transactionType?: TransactionType;
    transactionStatus?: TransactionStatus;
    accountId?: string;
    categoryId?: string;
    salaryCycleId?: string;
}

export interface Page<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
}
