import { useEffect, useState } from "react";
import type { FormEvent } from "react";

import type { Fund, FundType } from "../../types/fund";
import { FUND_TYPES } from "../../lib/fundTypes";
import { useCreateFund, useUpdateFund } from "../../hooks/useFunds";

interface Props {
    fund?: Fund;
    onSuccess: () => void;
}

function errorMessage(err: unknown): string {
    // Backend returns an RFC-7807 ProblemDetail with a "detail" field.
    const detail = (err as { response?: { data?: { detail?: string } } })
        ?.response?.data?.detail;
    return detail ?? "Something went wrong. Please try again.";
}

export default function FundForm({ fund, onSuccess }: Props) {
    const createMutation = useCreateFund();
    const updateMutation = useUpdateFund();

    const isEditing = Boolean(fund);
    const mutation = isEditing ? updateMutation : createMutation;
    const isPending = mutation.isPending;

    const [name, setName] = useState("");
    const [fundType, setFundType] = useState<FundType>("SAVINGS");
    const [hasTarget, setHasTarget] = useState(false);
    const [targetAmount, setTargetAmount] = useState("");
    const [targetDate, setTargetDate] = useState("");
    const [description, setDescription] = useState("");
    const [formError, setFormError] = useState<string | null>(null);

    useEffect(() => {
        if (!fund) {
            setName("");
            setFundType("SAVINGS");
            setHasTarget(false);
            setTargetAmount("");
            setTargetDate("");
            setDescription("");
            setFormError(null);
            return;
        }

        setName(fund.name);
        setFundType(fund.fundType);
        setHasTarget(fund.targetAmount != null);
        setTargetAmount(fund.targetAmount != null ? String(fund.targetAmount) : "");
        setTargetDate(fund.targetDate ?? "");
        setDescription(fund.description ?? "");
        setFormError(null);
    }, [fund]);

    function submit(e: FormEvent) {
        e.preventDefault();
        setFormError(null);

        if (!name.trim()) {
            setFormError("Fund name is required.");
            return;
        }

        const parsedTarget = hasTarget ? Number(targetAmount) : undefined;
        if (hasTarget && !(parsedTarget! > 0)) {
            setFormError("Target amount must be greater than zero when specified.");
            return;
        }

        if (isEditing) {
            updateMutation.mutate(
                {
                    id: fund!.id,
                    name: name.trim(),
                    targetAmount: parsedTarget,
                    targetDate: hasTarget && targetDate ? targetDate : undefined,
                    description: description.trim() || undefined,
                },
                { onSuccess },
            );
            return;
        }

        createMutation.mutate(
            {
                name: name.trim(),
                fundType,
                targetAmount: parsedTarget,
                targetDate: hasTarget && targetDate ? targetDate : undefined,
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
                <label className="field__label" htmlFor="fund-name">
                    Name<span className="field__req">*</span>
                </label>
                <input
                    id="fund-name"
                    className="input"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="e.g. Emergency Fund, New Laptop"
                    maxLength={100}
                    autoFocus
                />
            </div>

            {!isEditing && (
                <div className="field">
                    <label className="field__label" htmlFor="fund-type">
                        Type<span className="field__req">*</span>
                    </label>
                    <select
                        id="fund-type"
                        className="select"
                        value={fundType}
                        onChange={(e) => setFundType(e.target.value as FundType)}
                    >
                        {FUND_TYPES.map((opt) => (
                            <option key={opt.value} value={opt.value}>
                                {opt.label}
                            </option>
                        ))}
                    </select>
                    <span className="field__hint">
                        The fund's type cannot be changed after creation.
                    </span>
                </div>
            )}

            <label className="field-checkbox">
                <input
                    type="checkbox"
                    checked={hasTarget}
                    onChange={(e) => setHasTarget(e.target.checked)}
                />
                Set a savings goal
            </label>

            {hasTarget && (
                <div className="form-row">
                    <div className="field">
                        <label className="field__label" htmlFor="fund-target-amount">
                            Target Amount
                        </label>
                        <div className="input-affix">
                            <span className="input-affix__prefix">৳</span>
                            <input
                                id="fund-target-amount"
                                className="input"
                                type="number"
                                min="0.01"
                                step="0.01"
                                value={targetAmount}
                                onChange={(e) => setTargetAmount(e.target.value)}
                            />
                        </div>
                    </div>

                    <div className="field">
                        <label className="field__label" htmlFor="fund-target-date">
                            Target Date
                        </label>
                        <input
                            id="fund-target-date"
                            className="input"
                            type="date"
                            value={targetDate}
                            onChange={(e) => setTargetDate(e.target.value)}
                        />
                    </div>
                </div>
            )}

            <div className="field">
                <label className="field__label" htmlFor="fund-description">
                    Description
                </label>
                <textarea
                    id="fund-description"
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
                    disabled={isPending}
                >
                    {isPending
                        ? "Saving…"
                        : isEditing
                          ? "Update Fund"
                          : "Create Fund"}
                </button>
            </div>
        </form>
    );
}
