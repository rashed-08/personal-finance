import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts";

import { chartColor } from "../../lib/chartColors";
import ChartTooltip from "./ChartTooltip";

export interface DonutDatum {
    name: string;
    value: number;
}

interface SpendingDonutChartProps {
    data: DonutDatum[];
    valueFormatter?: (value: number) => string;
}

/** Donut chart for a small set of shares (e.g. top spending categories). */
export default function SpendingDonutChart({ data, valueFormatter }: SpendingDonutChartProps) {
    return (
        <ResponsiveContainer width="100%" height="100%">
            <PieChart>
                <Tooltip content={<ChartTooltip valueFormatter={valueFormatter} />} />
                <Pie
                    data={data}
                    dataKey="value"
                    nameKey="name"
                    innerRadius="55%"
                    outerRadius="85%"
                    paddingAngle={2}
                    stroke="var(--surface)"
                    strokeWidth={2}
                >
                    {data.map((entry, index) => (
                        <Cell key={entry.name} fill={chartColor(index)} />
                    ))}
                </Pie>
            </PieChart>
        </ResponsiveContainer>
    );
}
