import type { ReactNode } from "react";

interface ChartCardProps {
    title: string;
    subtitle?: string;
    height?: number;
    isEmpty?: boolean;
    emptyMessage?: string;
    children: ReactNode;
}

/**
 * Card shell for a single chart: title/subtitle header, fixed-height plot
 * area, and a shared empty state so every chart doesn't reinvent one.
 */
export default function ChartCard({
    title,
    subtitle,
    height = 280,
    isEmpty = false,
    emptyMessage = "No data for this period.",
    children,
}: ChartCardProps) {
    return (
        <div className="card chart-card">
            <div className="chart-card__header">
                <h3 className="chart-card__title">{title}</h3>
                {subtitle && <p className="chart-card__subtitle">{subtitle}</p>}
            </div>

            <div className="chart-card__body" style={{ height }}>
                {isEmpty ? (
                    <div className="state">
                        <div className="state__desc">{emptyMessage}</div>
                    </div>
                ) : (
                    children
                )}
            </div>
        </div>
    );
}
