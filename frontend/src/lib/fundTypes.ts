import type { FundType } from "../types/fund";

export interface FundTypeOption {
    value: FundType;
    label: string;
}

/**
 * Fund types accepted by the backend (FundType enum + DB CHECK). Order is
 * chosen for the "New Fund" dropdown; the same labels are reused by
 * FundTypeBadge.
 */
export const FUND_TYPES: FundTypeOption[] = [
    { value: "EMERGENCY", label: "Emergency" },
    { value: "SAVINGS", label: "Savings" },
    { value: "GOAL", label: "Goal" },
    { value: "ZAKAT", label: "Zakat" },
    { value: "INVESTMENT", label: "Investment" },
    { value: "CUSTOM", label: "Custom" },
];

const LABELS: Record<string, string> = Object.fromEntries(
    FUND_TYPES.map((t) => [t.value, t.label]),
);

/** Human-readable label for a fund type, with a safe fallback. */
export function fundTypeLabel(type: string): string {
    return LABELS[type] ?? type;
}
