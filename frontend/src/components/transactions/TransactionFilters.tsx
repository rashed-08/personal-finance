import { useAccounts } from "../../hooks/useAccounts";
import { useCategories } from "../../hooks/useCategories";
import { TRANSACTION_STATUSES, TRANSACTION_TYPES } from "../../lib/transactionTypes";
import type { TransactionFilter } from "../../types/transaction";

interface Props {
    filter: TransactionFilter;
    onChange(filter: TransactionFilter): void;
}

export default function TransactionFilters({ filter, onChange }: Props) {
    const { data: accounts = [] } = useAccounts();
    const { data: categories = [] } = useCategories();

    function set(key: keyof TransactionFilter, value: string) {
        onChange({ ...filter, [key]: value || undefined });
    }

    const hasActiveFilters = Object.values(filter).some(Boolean);

    return (
        <div className="filter-bar">
            <div className="field">
                <label className="field__label" htmlFor="filter-type">
                    Type
                </label>
                <select
                    id="filter-type"
                    className="select"
                    value={filter.transactionType ?? ""}
                    onChange={(e) => set("transactionType", e.target.value)}
                >
                    <option value="">All types</option>
                    {TRANSACTION_TYPES.map((t) => (
                        <option key={t.value} value={t.value}>
                            {t.label}
                        </option>
                    ))}
                </select>
            </div>

            <div className="field">
                <label className="field__label" htmlFor="filter-status">
                    Status
                </label>
                <select
                    id="filter-status"
                    className="select"
                    value={filter.transactionStatus ?? ""}
                    onChange={(e) => set("transactionStatus", e.target.value)}
                >
                    <option value="">All statuses</option>
                    {TRANSACTION_STATUSES.map((s) => (
                        <option key={s.value} value={s.value}>
                            {s.label}
                        </option>
                    ))}
                </select>
            </div>

            <div className="field">
                <label className="field__label" htmlFor="filter-account">
                    Account
                </label>
                <select
                    id="filter-account"
                    className="select"
                    value={filter.accountId ?? ""}
                    onChange={(e) => set("accountId", e.target.value)}
                >
                    <option value="">All accounts</option>
                    {accounts.map((a) => (
                        <option key={a.id} value={a.id}>
                            {a.name}
                        </option>
                    ))}
                </select>
            </div>

            <div className="field">
                <label className="field__label" htmlFor="filter-category">
                    Category
                </label>
                <select
                    id="filter-category"
                    className="select"
                    value={filter.categoryId ?? ""}
                    onChange={(e) => set("categoryId", e.target.value)}
                >
                    <option value="">All categories</option>
                    {categories.map((c) => (
                        <option key={c.id} value={c.id}>
                            {c.name}
                        </option>
                    ))}
                </select>
            </div>

            <div className="field">
                <label className="field__label" htmlFor="filter-from">
                    From
                </label>
                <input
                    id="filter-from"
                    type="date"
                    className="input"
                    value={filter.fromDate ?? ""}
                    onChange={(e) => set("fromDate", e.target.value)}
                />
            </div>

            <div className="field">
                <label className="field__label" htmlFor="filter-to">
                    To
                </label>
                <input
                    id="filter-to"
                    type="date"
                    className="input"
                    value={filter.toDate ?? ""}
                    onChange={(e) => set("toDate", e.target.value)}
                />
            </div>

            {hasActiveFilters && (
                <button
                    type="button"
                    className="btn btn--ghost btn--sm"
                    onClick={() => onChange({})}
                >
                    Clear filters
                </button>
            )}
        </div>
    );
}
