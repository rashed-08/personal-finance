import { Bar, BarChart, CartesianGrid, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";

import { chartColor } from "../../lib/chartColors";
import ChartTooltip from "./ChartTooltip";

export interface CategoryBarDatum {
    name: string;
    value: number;
}

interface CategoryBarChartProps {
    data: CategoryBarDatum[];
    valueFormatter?: (value: number) => string;
}

/** Horizontal bar chart for category breakdowns (income/expense by category, top spending). */
export default function CategoryBarChart({ data, valueFormatter }: CategoryBarChartProps) {
    return (
        <ResponsiveContainer width="100%" height="100%">
            <BarChart
                data={data}
                layout="vertical"
                margin={{ top: 8, right: 24, left: 0, bottom: 0 }}
                barCategoryGap={10}
            >
                <CartesianGrid stroke="var(--border)" horizontal={false} />
                <XAxis
                    type="number"
                    stroke="var(--text-subtle)"
                    tick={{ fontSize: 12 }}
                    tickLine={false}
                    axisLine={{ stroke: "var(--border)" }}
                    tickFormatter={(value: number) => (valueFormatter ? valueFormatter(value) : String(value))}
                />
                <YAxis
                    type="category"
                    dataKey="name"
                    stroke="var(--text-subtle)"
                    tick={{ fontSize: 12.5 }}
                    tickLine={false}
                    axisLine={false}
                    width={110}
                />
                <Tooltip
                    content={<ChartTooltip valueFormatter={valueFormatter} />}
                    cursor={{ fill: "var(--surface-2)" }}
                />
                <Bar dataKey="value" name="Amount" radius={[0, 6, 6, 0]} maxBarSize={22}>
                    {data.map((entry, index) => (
                        <Cell key={entry.name} fill={chartColor(index)} />
                    ))}
                </Bar>
            </BarChart>
        </ResponsiveContainer>
    );
}
