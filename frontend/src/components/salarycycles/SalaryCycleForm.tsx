import { useEffect, useState } from "react";
import type { FormEvent } from "react";

import { useCreateSalaryCycle, useUpdateSalaryCycle } from "../../hooks/useSalaryCycles";
import type { SalaryCycle } from "../../types/salaryCycle";

interface Props {
    salaryCycle?: SalaryCycle;
    onSuccess: () => void;
}

function errorMessage(err: unknown): string {
    const detail = (err as { response?: { data?: { detail?: string } } })
        ?.response?.data?.detail;
    return detail ?? "Something went wrong. Please try again.";
}

export default function SalaryCycleForm({ salaryCycle, onSuccess }: Props) {
    const isEditing = Boolean(salaryCycle);

    const createMutation = useCreateSalaryCycle();
    const updateMutation = useUpdateSalaryCycle();
    const mutation = isEditing ? updateMutation : createMutation;

    const [name, setName] = useState("");
    const [startDate, setStartDate] = useState("");
    const [endDate, setEndDate] = useState("");
    const [salaryDate, setSalaryDate] = useState("");
    const [description, setDescription] = useState("");
    const [formError, setFormError] = useState<string | null>(null);

    useEffect(() => {
        if (!salaryCycle) {
            setName("");
            setStartDate("");
            setEndDate("");
            setSalaryDate("");
            setDescription("");
            setFormError(null);
            return;
        }

        setName(salaryCycle.name);
        setStartDate(salaryCycle.startDate);
        setEndDate(salaryCycle.endDate ?? "");
        setSalaryDate(salaryCycle.salaryDate);
        setDescription(salaryCycle.description ?? "");
        setFormError(null);
    }, [salaryCycle]);

    function submit(e: FormEvent) {
        e.preventDefault();
        setFormError(null);

        if (!name.trim()) {
            setFormError("Name is required.");
            return;
        }
        if (!salaryDate) {
            setFormError("Salary date is required.");
            return;
        }

        if (isEditing) {
            updateMutation.mutate(
                {
                    id: salaryCycle!.id,
                    name: name.trim(),
                    salaryDate,
                    description: description.trim() || undefined,
                },
                { onSuccess },
            );
            return;
        }

        if (!startDate) {
            setFormError("Start date is required.");
            return;
        }

        createMutation.mutate(
            {
                name: name.trim(),
                startDate,
                endDate: endDate || undefined,
                salaryDate,
                description: description.trim() || undefined,
            },
            { onSuccess },
        );
    }

    return (
        <form className="form" onSubmit={submit} noValidate>
            {formError && (
                <div className="form-error" role="alert">
                    <span>⚠</span>
                    <span>{formError}</span>
                </div>
            )}

            {mutation.isError && (
                <div className="form-error" role="alert">
                    <span>⚠</span>
                    <span>{errorMessage(mutation.error)}</span>
                </div>
            )}

            <div className="field">
                <label className="field__label" htmlFor="cycle-name">
                    Name<span className="field__req">*</span>
                </label>
                <input
                    id="cycle-name"
                    className="input"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="e.g. July 2026"
                    maxLength={100}
                    autoFocus
                />
            </div>

            {!isEditing && (
                <div className="form-row">
                    <div className="field">
                        <label className="field__label" htmlFor="cycle-start">
                            Start Date<span className="field__req">*</span>
                        </label>
                        <input
                            id="cycle-start"
                            type="date"
                            className="input"
                            value={startDate}
                            onChange={(e) => setStartDate(e.target.value)}
                        />
                    </div>

                    <div className="field">
                        <label className="field__label" htmlFor="cycle-end">
                            End Date
                        </label>
                        <input
                            id="cycle-end"
                            type="date"
                            className="input"
                            value={endDate}
                            onChange={(e) => setEndDate(e.target.value)}
                        />
                        <span className="field__hint">
                            Leave blank to create an ongoing cycle.
                        </span>
                    </div>
                </div>
            )}

            <div className="field">
                <label className="field__label" htmlFor="cycle-salary-date">
                    Salary Date<span className="field__req">*</span>
                </label>
                <input
                    id="cycle-salary-date"
                    type="date"
                    className="input"
                    value={salaryDate}
                    onChange={(e) => setSalaryDate(e.target.value)}
                />
            </div>

            <div className="field">
                <label className="field__label" htmlFor="cycle-description">
                    Description
                </label>
                <textarea
                    id="cycle-description"
                    className="textarea"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    placeholder="Optional notes"
                    maxLength={500}
                />
            </div>

            <div className="form-actions">
                <button
                    type="submit"
                    className="btn btn--primary"
                    disabled={mutation.isPending}
                >
                    {mutation.isPending
                        ? "Saving…"
                        : isEditing
                          ? "Update Salary Cycle"
                          : "Create Salary Cycle"}
                </button>
            </div>
        </form>
    );
}
