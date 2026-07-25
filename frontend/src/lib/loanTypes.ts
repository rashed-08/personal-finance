import type { LoanStatus, LoanType } from "../types/loan";

interface Option<T extends string> {
    value: T;
    label: string;
}

export const LOAN_TYPES: Option<LoanType>[] = [
    { value: "RECEIVABLE", label: "Money Lent (Receivable)" },
    { value: "PAYABLE", label: "Money Borrowed (Payable)" },
];

export const LOAN_STATUSES: Option<LoanStatus>[] = [
    { value: "ACTIVE", label: "Active" },
    { value: "CLOSED", label: "Closed" },
    { value: "CANCELLED", label: "Cancelled" },
];

function labelMap<T extends string>(options: Option<T>[]): Record<string, string> {
    return Object.fromEntries(options.map((o) => [o.value, o.label]));
}

const TYPE_LABELS = labelMap(LOAN_TYPES);
const STATUS_LABELS = labelMap(LOAN_STATUSES);

export function loanTypeLabel(type: string): string {
    return TYPE_LABELS[type] ?? type;
}

export function loanStatusLabel(status: string): string {
    return STATUS_LABELS[status] ?? status;
}
