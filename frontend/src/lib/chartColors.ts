/**
 * Chart series colors, kept consistent with the badge colors already used
 * across accounts/funds/loans/transactions (see .badge--* in index.css) so a
 * category that's teal in a table is the same teal in a chart.
 */
export const CHART_COLORS = [
    "#0e9384", // teal
    "#175cd3", // blue
    "#b54708", // amber
    "#7839ee", // violet
    "#c11574", // pink
    "#3538cd", // indigo
    "#d92d20", // red
    "#067647", // green
] as const;

export function chartColor(index: number): string {
    return CHART_COLORS[index % CHART_COLORS.length];
}
