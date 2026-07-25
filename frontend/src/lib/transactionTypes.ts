import type {
    AdjustmentReason,
    TransactionStatus,
    TransactionType,
} from "../types/transaction";

interface Option<T extends string> {
    value: T;
    label: string;
}

export const TRANSACTION_TYPES: Option<TransactionType>[] = [
    { value: "INCOME", label: "Income" },
    { value: "EXPENSE", label: "Expense" },
    { value: "TRANSFER", label: "Transfer" },
    { value: "ADJUSTMENT", label: "Adjustment" },
    { value: "OPENING_BALANCE", label: "Opening Balance" },
    { value: "MIGRATION", label: "Migration" },
];

export const TRANSACTION_STATUSES: Option<TransactionStatus>[] = [
    { value: "POSTED", label: "Posted" },
    { value: "VOID", label: "Void" },
    { value: "REVERSED", label: "Reversed" },
];

export const ADJUSTMENT_REASONS: Option<AdjustmentReason>[] = [
    { value: "CASH_RECONCILIATION", label: "Cash Reconciliation" },
    { value: "OPENING_BALANCE", label: "Opening Balance Correction" },
    { value: "DATA_MIGRATION", label: "Data Migration Correction" },
    { value: "MANUAL_CORRECTION", label: "Manual Correction" },
    { value: "SYSTEM_CORRECTION", label: "System Correction" },
    { value: "TRANSACTION_UPDATE", label: "Transaction Update" },
];

function labelMap<T extends string>(options: Option<T>[]): Record<string, string> {
    return Object.fromEntries(options.map((o) => [o.value, o.label]));
}

const TYPE_LABELS = labelMap(TRANSACTION_TYPES);
const STATUS_LABELS = labelMap(TRANSACTION_STATUSES);
const REASON_LABELS = labelMap(ADJUSTMENT_REASONS);

export function transactionTypeLabel(type: string): string {
    return TYPE_LABELS[type] ?? type;
}

export function transactionStatusLabel(status: string): string {
    return STATUS_LABELS[status] ?? status;
}

export function adjustmentReasonLabel(reason: string): string {
    return REASON_LABELS[reason] ?? reason;
}

/**
 * Which fields the create form needs for a given transaction type.
 * ADJUSTMENT is handled separately by the form (it needs at most one of
 * fromAccountId/toAccountId, chosen via a direction toggle, not both).
 */
export interface TransactionTypeShape {
    needsFromAccount: boolean;
    needsToAccount: boolean;
    needsCategory: boolean;
    needsSalaryCycle: boolean;
}

const SHAPES: Record<TransactionType, TransactionTypeShape> = {
    INCOME: {
        needsFromAccount: false,
        needsToAccount: true,
        needsCategory: true,
        needsSalaryCycle: true,
    },
    EXPENSE: {
        needsFromAccount: true,
        needsToAccount: false,
        needsCategory: true,
        needsSalaryCycle: true,
    },
    TRANSFER: {
        needsFromAccount: true,
        needsToAccount: true,
        needsCategory: false,
        needsSalaryCycle: true,
    },
    ADJUSTMENT: {
        needsFromAccount: false,
        needsToAccount: false,
        needsCategory: false,
        needsSalaryCycle: false,
    },
    OPENING_BALANCE: {
        needsFromAccount: false,
        needsToAccount: true,
        needsCategory: false,
        needsSalaryCycle: false,
    },
    MIGRATION: {
        needsFromAccount: false,
        needsToAccount: true,
        needsCategory: false,
        needsSalaryCycle: false,
    },
};

export function shapeFor(type: TransactionType): TransactionTypeShape {
    return SHAPES[type];
}
