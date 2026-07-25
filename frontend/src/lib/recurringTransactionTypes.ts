import type { Frequency } from "../types/recurringTransaction";

interface Option<T extends string> {
    value: T;
    label: string;
}

export const FREQUENCIES: Option<Frequency>[] = [
    { value: "DAILY", label: "Daily" },
    { value: "WEEKLY", label: "Weekly" },
    { value: "MONTHLY", label: "Monthly" },
    { value: "YEARLY", label: "Yearly" },
];

const FREQUENCY_LABELS: Record<string, string> = Object.fromEntries(
    FREQUENCIES.map((f) => [f.value, f.label]),
);

export function frequencyLabel(frequency: string): string {
    return FREQUENCY_LABELS[frequency] ?? frequency;
}

export const RECURRING_TRANSACTION_TYPES: Option<"EXPENSE" | "INCOME" | "TRANSFER">[] = [
    { value: "EXPENSE", label: "Expense" },
    { value: "INCOME", label: "Income" },
    { value: "TRANSFER", label: "Transfer" },
];

export interface RecurringTransactionTypeShape {
    needsFromAccount: boolean;
    needsToAccount: boolean;
    needsCategory: boolean;
}

const SHAPES: Record<string, RecurringTransactionTypeShape> = {
    EXPENSE: { needsFromAccount: true, needsToAccount: false, needsCategory: true },
    INCOME: { needsFromAccount: false, needsToAccount: true, needsCategory: true },
    TRANSFER: { needsFromAccount: true, needsToAccount: true, needsCategory: false },
};

export function shapeFor(transactionType: string): RecurringTransactionTypeShape {
    return SHAPES[transactionType];
}
