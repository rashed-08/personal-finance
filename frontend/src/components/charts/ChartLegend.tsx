import { chartColor } from "../../lib/chartColors";

export interface LegendDatum {
    name: string;
    value: number;
}

interface ChartLegendProps {
    data: LegendDatum[];
    valueFormatter?: (value: number) => string;
}

/** Compact color-key legend to pair with SpendingDonutChart / CategoryBarChart. */
export default function ChartLegend({ data, valueFormatter }: ChartLegendProps) {
    const format = valueFormatter ?? ((value: number) => value.toLocaleString());

    return (
        <ul className="chart-legend">
            {data.map((entry, index) => (
                <li key={entry.name} className="chart-legend__item">
                    <span className="chart-legend__dot" style={{ background: chartColor(index) }} />
                    <span className="chart-legend__name">{entry.name}</span>
                    <span className="chart-legend__value">{format(entry.value)}</span>
                </li>
            ))}
        </ul>
    );
}
