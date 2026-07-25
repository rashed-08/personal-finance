import { useState } from "react";

import { useSalaryCycles } from "../../hooks/useSalaryCycles";
import CarryForwardPanel from "../salarycycles/CarryForwardPanel";

export default function SalaryCycleReportView() {
    const { data: cycles = [], isLoading, error } = useSalaryCycles();
    const [salaryCycleId, setSalaryCycleId] = useState("");

    const sorted = [...cycles].sort((a, b) => b.startDate.localeCompare(a.startDate));
    const selected = sorted.find((c) => c.id === salaryCycleId);

    return (
        <div>
            <div className="filter-bar">
                <div className="field">
                    <label className="field__label" htmlFor="salary-cycle-report-cycle">
                        Salary Cycle<span className="field__req">*</span>
                    </label>
                    <select
                        id="salary-cycle-report-cycle"
                        className="select"
                        value={salaryCycleId}
                        onChange={(e) => setSalaryCycleId(e.target.value)}
                    >
                        <option value="">Select a cycle…</option>
                        {sorted.map((c) => (
                            <option key={c.id} value={c.id}>
                                {c.name} ({c.startDate} – {c.endDate ?? "open"})
                            </option>
                        ))}
                    </select>
                </div>
            </div>

            {isLoading ? (
                <div className="card">
                    <div className="state">
                        <div className="spinner" />
                        <div className="state__desc">Loading salary cycles…</div>
                    </div>
                </div>
            ) : error ? (
                <div className="card">
                    <div className="state">
                        <div className="state__icon">⚠</div>
                        <div className="state__title">Couldn't load salary cycles</div>
                    </div>
                </div>
            ) : !selected ? (
                <div className="card">
                    <div className="state">
                        <div className="state__desc">Choose a salary cycle to see its carry-forward breakdown.</div>
                    </div>
                </div>
            ) : (
                <div className="card">
                    <CarryForwardPanel salaryCycleId={selected.id} />
                </div>
            )}
        </div>
    );
}
