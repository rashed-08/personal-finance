interface TooltipPayloadEntry {
    name?: string;
    value?: number | string;
    color?: string;
}

interface ChartTooltipProps {
    active?: boolean;
    label?: string | number;
    payload?: TooltipPayloadEntry[];
    valueFormatter?: (value: number) => string;
}

/**
 * Recharts' default tooltip doesn't pick up the app's design tokens, so we
 * render our own using the same CSS variables as the rest of the UI — it
 * follows light/dark mode for free.
 */
export default function ChartTooltip({ active, label, payload, valueFormatter }: ChartTooltipProps) {
    if (!active || !payload || payload.length === 0) {
        return null;
    }

    const format = valueFormatter ?? ((value: number) => value.toLocaleString());

    return (
        <div
            style={{
                background: "var(--surface)",
                border: "1px solid var(--border)",
                borderRadius: "var(--radius-sm)",
                boxShadow: "var(--shadow-md)",
                padding: "8px 12px",
                fontSize: 13,
            }}
        >
            {label !== undefined && (
                <div style={{ color: "var(--text-subtle)", marginBottom: 4, fontSize: 12 }}>{label}</div>
            )}
            {payload.map((entry, index) => (
                <div key={index} style={{ display: "flex", alignItems: "center", gap: 8 }}>
                    <span
                        style={{
                            width: 8,
                            height: 8,
                            borderRadius: "50%",
                            background: entry.color ?? "var(--primary)",
                            flexShrink: 0,
                        }}
                    />
                    <span style={{ color: "var(--text-muted)" }}>{entry.name}</span>
                    <strong style={{ marginLeft: "auto", color: "var(--text)", fontVariantNumeric: "tabular-nums" }}>
                        {typeof entry.value === "number" ? format(entry.value) : entry.value}
                    </strong>
                </div>
            ))}
        </div>
    );
}
