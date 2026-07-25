import { useEffect, useState } from "react";
import type { FormEvent } from "react";

import { useAccounts } from "../../hooks/useAccounts";
import { useCreateLoan, useUpdateLoan } from "../../hooks/useLoans";
import { LOAN_TYPES } from "../../lib/loanTypes";
import type { Loan, LoanType } from "../../types/loan";
import SalaryCycleSelect from "../salarycycles/SalaryCycleSelect";

interface Props {
    loan?: Loan;
    onSuccess: () => void;
}

function errorMessage(err: unknown): string {
    const detail = (err as { response?: { data?: { detail?: string } } })
        ?.response?.data?.detail;
    return detail ?? "Something went wrong. Please try again.";
}

function today(): string {
    return new Date().toISOString().slice(0, 10);
}

export default function LoanForm({ loan, onSuccess }: Props) {
    const isEditing = Boolean(loan);

    const { data: accounts = [] } = useAccounts();
    const activeAccounts = accounts.filter((a) => a.active);

    const createMutation = useCreateLoan();
    const updateMutation = useUpdateLoan();
    const mutation = isEditing ? updateMutation : createMutation;

    const [name, setName] = useState("");
    const [loanType, setLoanType] = useState<LoanType>("RECEIVABLE");
    const [principalAmount, setPrincipalAmount] = useState("");
    const [startDate, setStartDate] = useState(today());
    const [hasDueDate, setHasDueDate] = useState(false);
    const [dueDate, setDueDate] = useState("");
    const [accountId, setAccountId] = useState("");
    const [salaryCycleId, setSalaryCycleId] = useState("");
    const [description, setDescription] = useState("");
    const [formError, setFormError] = useState<string | null>(null);

    useEffect(() => {
        if (!loan) {
            setName("");
            setLoanType("RECEIVABLE");
            setPrincipalAmount("");
            setStartDate(today());
            setHasDueDate(false);
            setDueDate("");
            setAccountId("");
            setSalaryCycleId("");
            setDescription("");
            setFormError(null);
            return;
        }

        setName(loan.name);
        setLoanType(loan.loanType);
        setPrincipalAmount(String(loan.principalAmount));
        setStartDate(loan.startDate);
        setHasDueDate(loan.dueDate != null);
        setDueDate(loan.dueDate ?? "");
        setDescription(loan.description ?? "");
        setFormError(null);
    }, [loan]);

    function submit(e: FormEvent) {
        e.preventDefault();
        setFormError(null);

        if (!name.trim()) {
            setFormError("Name is required.");
            return;
        }

        if (isEditing) {
            updateMutation.mutate(
                {
                    id: loan!.id,
                    name: name.trim(),
                    dueDate: hasDueDate && dueDate ? dueDate : undefined,
                    description: description.trim() || undefined,
                },
                { onSuccess },
            );
            return;
        }

        const parsedAmount = Number(principalAmount);
        if (!principalAmount || !(parsedAmount > 0)) {
            setFormError("Principal amount must be greater than zero.");
            return;
        }
        if (!accountId) {
            setFormError("Account is required.");
            return;
        }
        if (!salaryCycleId) {
            setFormError("Salary cycle is required.");
            return;
        }

        createMutation.mutate(
            {
                name: name.trim(),
                loanType,
                principalAmount: parsedAmount,
                startDate,
                dueDate: hasDueDate && dueDate ? dueDate : undefined,
                accountId,
                salaryCycleId,
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
                <label className="field__label" htmlFor="loan-name">
                    Name<span className="field__req">*</span>
                </label>
                <input
                    id="loan-name"
                    className="input"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="e.g. Rahim, Brother"
                    maxLength={150}
                    autoFocus
                />
            </div>

            {!isEditing && (
                <>
                    <div className="field">
                        <label className="field__label" htmlFor="loan-type">
                            Type<span className="field__req">*</span>
                        </label>
                        <select
                            id="loan-type"
                            className="select"
                            value={loanType}
                            onChange={(e) => setLoanType(e.target.value as LoanType)}
                        >
                            {LOAN_TYPES.map((opt) => (
                                <option key={opt.value} value={opt.value}>
                                    {opt.label}
                                </option>
                            ))}
                        </select>
                        <span className="field__hint">
                            The loan's type cannot be changed after creation.
                        </span>
                    </div>

                    <div className="form-row">
                        <div className="field">
                            <label className="field__label" htmlFor="loan-principal">
                                Principal Amount<span className="field__req">*</span>
                            </label>
                            <div className="input-affix">
                                <span className="input-affix__prefix">৳</span>
                                <input
                                    id="loan-principal"
                                    className="input"
                                    type="number"
                                    min="0.01"
                                    step="0.01"
                                    value={principalAmount}
                                    onChange={(e) => setPrincipalAmount(e.target.value)}
                                />
                            </div>
                        </div>

                        <div className="field">
                            <label className="field__label" htmlFor="loan-start-date">
                                Start Date<span className="field__req">*</span>
                            </label>
                            <input
                                id="loan-start-date"
                                className="input"
                                type="date"
                                value={startDate}
                                onChange={(e) => setStartDate(e.target.value)}
                            />
                        </div>
                    </div>

                    <div className="field">
                        <label className="field__label" htmlFor="loan-account">
                            {loanType === "RECEIVABLE" ? "From Account" : "To Account"}
                            <span className="field__req">*</span>
                        </label>
                        <select
                            id="loan-account"
                            className="select"
                            value={accountId}
                            onChange={(e) => setAccountId(e.target.value)}
                        >
                            <option value="">Select an account</option>
                            {activeAccounts.map((a) => (
                                <option key={a.id} value={a.id}>
                                    {a.name}
                                </option>
                            ))}
                        </select>
                        <span className="field__hint">
                            {loanType === "RECEIVABLE"
                                ? "Money leaves this account when the loan is given."
                                : "Money enters this account when the loan is received."}
                        </span>
                    </div>

                    <SalaryCycleSelect
                        id="loan-salary-cycle"
                        value={salaryCycleId}
                        onChange={setSalaryCycleId}
                    />
                </>
            )}

            <label className="field-checkbox">
                <input
                    type="checkbox"
                    checked={hasDueDate}
                    onChange={(e) => setHasDueDate(e.target.checked)}
                />
                Set a target settlement date
            </label>

            {hasDueDate && (
                <div className="field">
                    <label className="field__label" htmlFor="loan-due-date">
                        Due Date
                    </label>
                    <input
                        id="loan-due-date"
                        className="input"
                        type="date"
                        value={dueDate}
                        onChange={(e) => setDueDate(e.target.value)}
                    />
                </div>
            )}

            <div className="field">
                <label className="field__label" htmlFor="loan-description">
                    Description
                </label>
                <textarea
                    id="loan-description"
                    className="textarea"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    placeholder="Optional notes"
                    maxLength={1000}
                />
            </div>

            <div className="form-actions">
                <button type="submit" className="btn btn--primary" disabled={mutation.isPending}>
                    {mutation.isPending
                        ? "Saving…"
                        : isEditing
                          ? "Update Loan"
                          : "Create Loan"}
                </button>
            </div>
        </form>
    );
}
