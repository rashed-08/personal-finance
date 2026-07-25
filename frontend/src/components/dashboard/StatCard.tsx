import type { ReactNode } from "react";

interface StatCardProps {
    label: string;
    value: string;
    tone?: "neutral" | "positive" | "negative";
    meta?: ReactNode;
}

/** A single summary figure on the dashboard (balance, income, loan position, ...). */
export default function StatCard({ label, value, tone = "neutral", meta }: StatCardProps) {
    const valueClassName =
        tone === "positive"
            ? "stat-card__value stat-card__value--positive"
            : tone === "negative"
              ? "stat-card__value stat-card__value--negative"
              : "stat-card__value";

    return (
        <div className="card stat-card">
            <span className="stat-card__label">{label}</span>
            <span className={valueClassName}>{value}</span>
            {meta && <span className="stat-card__meta">{meta}</span>}
        </div>
    );
}
