import {
    Area,
    AreaChart,
    CartesianGrid,
    ResponsiveContainer,
    Tooltip,
    XAxis,
    YAxis,
} from "recharts";

import ChartTooltip from "./ChartTooltip";

export interface TrendPoint {
    label: string;
    value: number;
}

interface TrendLineChartProps {
    data: TrendPoint[];
    color?: string;
    valueFormatter?: (value: number) => string;
}

/** A single-series trend over time (e.g. income/expense by date, category monthly trend). */
export default function TrendLineChart({ data, color = "var(--primary)", valueFormatter }: TrendLineChartProps) {
    return (
        <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={data} margin={{ top: 8, right: 12, left: 0, bottom: 0 }}>
                <defs>
                    <linearGradient id="trendFill" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stopColor={color} stopOpacity={0.28} />
                        <stop offset="100%" stopColor={color} stopOpacity={0} />
                    </linearGradient>
                </defs>
                <CartesianGrid stroke="var(--border)" vertical={false} />
                <XAxis
                    dataKey="label"
                    stroke="var(--text-subtle)"
                    tick={{ fontSize: 12 }}
                    tickLine={false}
                    axisLine={{ stroke: "var(--border)" }}
                />
                <YAxis
                    stroke="var(--text-subtle)"
                    tick={{ fontSize: 12 }}
                    tickLine={false}
                    axisLine={false}
                    width={56}
                    tickFormatter={(value: number) => (valueFormatter ? valueFormatter(value) : String(value))}
                />
                <Tooltip
                    content={<ChartTooltip valueFormatter={valueFormatter} />}
                    cursor={{ stroke: "var(--border-strong)" }}
                />
                <Area
                    type="monotone"
                    dataKey="value"
                    name="Amount"
                    stroke={color}
                    strokeWidth={2}
                    fill="url(#trendFill)"
                    activeDot={{ r: 4 }}
                />
            </AreaChart>
        </ResponsiveContainer>
    );
}
