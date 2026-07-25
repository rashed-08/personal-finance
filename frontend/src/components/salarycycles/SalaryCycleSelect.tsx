import { useState } from "react";
import type { FormEvent } from "react";

import { useCreateSalaryCycle, useSalaryCycles } from "../../hooks/useSalaryCycles";

interface Props {
    id: string;
    value: string;
    onChange(salaryCycleId: string): void;
}

function errorMessage(err: unknown): string {
    const detail = (err as { response?: { data?: { detail?: string } } })
        ?.response?.data?.detail;
    return detail ?? "Could not create salary cycle.";
}

/**
 * Dropdown for picking a salary cycle, with an inline "+ New" form since
 * there is no dedicated Salary Cycle management page yet.
 */
export default function SalaryCycleSelect({ id, value, onChange }: Props) {
    const { data = [], isLoading } = useSalaryCycles();
    const createMutation = useCreateSalaryCycle();

    const [creating, setCreating] = useState(false);
    const [name, setName] = useState("");
    const [startDate, setStartDate] = useState("");
    const [salaryDate, setSalaryDate] = useState("");

    function startCreating() {
        setName("");
        setStartDate("");
        setSalaryDate("");
        setCreating(true);
    }

    function submitNewCycle(e: FormEvent) {
        e.preventDefault();

        createMutation.mutate(
            { name: name.trim(), startDate, salaryDate },
            {
                onSuccess: (cycle) => {
                    onChange(cycle.id);
                    setCreating(false);
                },
            },
        );
    }

    if (creating) {
        return (
            <div className="field">
                <span className="field__label">New Salary Cycle</span>

                <div className="inline-panel">
                    {createMutation.isError && (
                        <div className="form-error" role="alert">
                            <span>⚠</span>
                            <span>{errorMessage(createMutation.error)}</span>
                        </div>
                    )}

                    <input
                        className="input"
                        placeholder="Cycle name, e.g. July 2026"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        maxLength={100}
                        autoFocus
                    />

                    <div className="inline-panel__row">
                        <input
                            className="input"
                            type="date"
                            aria-label="Start date"
                            value={startDate}
                            onChange={(e) => setStartDate(e.target.value)}
                        />
                        <input
                            className="input"
                            type="date"
                            aria-label="Salary date"
                            value={salaryDate}
                            onChange={(e) => setSalaryDate(e.target.value)}
                        />
                    </div>
                    <span className="field__hint">
                        Opens as an ongoing cycle — it closes automatically when the
                        next salary starts a new one.
                    </span>

                    <div className="inline-panel__actions">
                        <button
                            type="button"
                            className="btn btn--primary btn--sm"
                            disabled={
                                !name.trim() ||
                                !startDate ||
                                !salaryDate ||
                                createMutation.isPending
                            }
                            onClick={submitNewCycle}
                        >
                            {createMutation.isPending ? "Creating…" : "Create & Use"}
                        </button>
                        <button
                            type="button"
                            className="btn btn--ghost btn--sm"
                            onClick={() => setCreating(false)}
                        >
                            Cancel
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="field">
            <label className="field__label" htmlFor={id}>
                Salary Cycle<span className="field__req">*</span>
            </label>

            <div className="inline-panel__row">
                <select
                    id={id}
                    className="select"
                    value={value}
                    onChange={(e) => onChange(e.target.value)}
                    disabled={isLoading}
                >
                    <option value="">
                        {isLoading ? "Loading…" : "Select a salary cycle"}
                    </option>
                    {data.map((cycle) => (
                        <option key={cycle.id} value={cycle.id}>
                            {cycle.name} ({cycle.startDate} – {cycle.endDate ?? "ongoing"})
                        </option>
                    ))}
                </select>

                <button
                    type="button"
                    className="btn btn--ghost btn--sm"
                    onClick={startCreating}
                >
                    + New
                </button>
            </div>
        </div>
    );
}
