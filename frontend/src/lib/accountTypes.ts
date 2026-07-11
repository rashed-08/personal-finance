import type { AccountType } from "../types/account";

export interface AccountTypeOption {
    value: AccountType;
    label: string;
}

/**
 * Account types accepted by the backend (AccountType enum + DB CHECK).
 * Order is chosen for the "New Account" dropdown; the same labels are reused
 * by AccountTypeBadge.
 */
export const ACCOUNT_TYPES: AccountTypeOption[] = [
    { value: "CASH", label: "Cash" },
    { value: "BANK", label: "Bank" },
    { value: "MOBILE_BANKING", label: "Mobile Banking" },
    { value: "SAVINGS", label: "Savings" },
    { value: "CREDIT_CARD", label: "Credit Card" },
    { value: "INVESTMENT", label: "Investment" },
    { value: "E_WALLET", label: "E-Wallet" },
];

const LABELS: Record<string, string> = Object.fromEntries(
    ACCOUNT_TYPES.map((t) => [t.value, t.label]),
);

/** Human-readable label for an account type, with a safe fallback. */
export function accountTypeLabel(type: string): string {
    return LABELS[type] ?? type;
}
