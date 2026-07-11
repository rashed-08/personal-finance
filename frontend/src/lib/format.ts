/**
 * Formatting helpers.
 *
 * The system default currency is BDT (Bangladeshi Taka). Balances are always
 * derived from the ledger in the domain; on the accounts screen the value we
 * display is the account's opening balance.
 */

const amountFormatter = new Intl.NumberFormat("en-US", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
});

/** Format a numeric amount as BDT, e.g. 1250 -> "৳ 1,250.00". */
export function formatCurrency(amount: number): string {
    const value = Number.isFinite(amount) ? amount : 0;
    return `৳ ${amountFormatter.format(value)}`;
}
