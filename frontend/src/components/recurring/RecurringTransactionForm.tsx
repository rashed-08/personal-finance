import { useEffect, useState } from "react";
import type { FormEvent } from "react";

import { useAccounts } from "../../hooks/useAccounts";
import { useCategories } from "../../hooks/useCategories";
import {
    useCreateRecurringTransaction,
    useUpdateRecurringTransaction,
} from "../../hooks/useRecurringTransactions";
import { FREQUENCIES, RECURRING_TRANSACTION_TYPES, shapeFor } from "../../lib/recurringTransactionTypes";
import type { Frequency, RecurringTransaction } from "../../types/recurringTransaction";

interface Props {
    recurringTransaction?: RecurringTransaction;
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

export default function RecurringTransactionForm({ recurringTransaction, onSuccess }: Props) {
    const isEditing = Boolean(recurringTransaction);

    const { data: accounts = [] } = useAccounts();
    const { data: categories = [] } = useCategories();
    const activeAccounts = accounts.filter((a) => a.active);

    const createMutation = useCreateRecurringTransaction();
    const updateMutation = useUpdateRecurringTransaction();
    const mutation = isEditing ? updateMutation : createMutation;

    const [name, setName] = useState("");
    const [transactionType, setTransactionType] = useState<"EXPENSE" | "INCOME" | "TRANSFER">("EXPENSE");
    const [fromAccountId, setFromAccountId] = useState("");
    const [toAccountId, setToAccountId] = useState("");
    const [categoryId, setCategoryId] = useState("");
    const [amount, setAmount] = useState("");
    const [frequency, setFrequency] = useState<Frequency>("MONTHLY");
    const [startDate, setStartDate] = useState(today());
    const [hasEndDate, setHasEndDate] = useState(false);
    const [endDate, setEndDate] = useState("");
    const [autoGenerate, setAutoGenerate] = useState(false);
    const [description, setDescription] = useState("");
    const [notes, setNotes] = useState("");
    const [formError, setFormError] = useState<string | null>(null);

    useEffect(() => {
        if (!recurringTransaction) {
            setName("");
            setTransactionType("EXPENSE");
            setFromAccountId("");
            setToAccountId("");
            setCategoryId("");
            setAmount("");
            setFrequency("MONTHLY");
            setStartDate(today());
            setHasEndDate(false);
            setEndDate("");
            setAutoGenerate(false);
            setDescription("");
            setNotes("");
            setFormError(null);
            return;
        }

        setName(recurringTransaction.name);
        setTransactionType(recurringTransaction.transactionType);
        setAmount(String(recurringTransaction.amount));
        setFrequency(recurringTransaction.frequency);
        setHasEndDate(recurringTransaction.endDate != null);
        setEndDate(recurringTransaction.endDate ?? "");
        setAutoGenerate(recurringTransaction.autoGenerate);
        setDescription(recurringTransaction.description ?? "");
        setNotes(recurringTransaction.notes ?? "");
        setFormError(null);
    }, [recurringTransaction]);

    const shape = shapeFor(transactionType);
    const categoryOptions = categories.filter(
        (c) =>
            c.categoryType === (transactionType === "INCOME" ? "INCOME" : "EXPENSE") &&
            (c.active || c.id === categoryId),
    );

    function submit(e: FormEvent) {
        e.preventDefault();
        setFormError(null);

        if (!name.trim()) {
            setFormError("Name is required.");
            return;
        }

        const parsedAmount = Number(amount);
        if (!amount || !(parsedAmount > 0)) {
            setFormError("Amount must be greater than zero.");
            return;
        }

        if (isEditing) {
            updateMutation.mutate(
                {
                    id: recurringTransaction!.id,
                    name: name.trim(),
                    amount: parsedAmount,
                    frequency,
                    endDate: hasEndDate && endDate ? endDate : undefined,
                    autoGenerate,
                    description: description.trim() || undefined,
                    notes: notes.trim() || undefined,
                },
                { onSuccess },
            );
            return;
        }

        if (shape.needsFromAccount && !fromAccountId) {
            setFormError("Source account is required.");
            return;
        }
        if (shape.needsToAccount && !toAccountId) {
            setFormError("Destination account is required.");
            return;
        }
        if (transactionType === "TRANSFER" && fromAccountId === toAccountId) {
            setFormError("Source and destination account cannot be the same.");
            return;
        }
        if (shape.needsCategory && !categoryId) {
            setFormError("Category is required.");
            return;
        }

        createMutation.mutate(
            {
                name: name.trim(),
                transactionType,
                fromAccountId: shape.needsFromAccount ? fromAccountId : undefined,
                toAccountId: shape.needsToAccount ? toAccountId : undefined,
                categoryId: shape.needsCategory ? categoryId : undefined,
                amount: parsedAmount,
                frequency,
                startDate,
                endDate: hasEndDate && endDate ? endDate : undefined,
                autoGenerate,
                description: description.trim() || undefined,
                notes: notes.trim() || undefined,
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
                <label className="field__label" htmlFor="rt-name">
                    Name<span className="field__req">*</span>
                </label>
                <input
                    id="rt-name"
                    className="input"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="e.g. House Rent, Gym Membership"
                    maxLength={100}
                    autoFocus
                />
            </div>

            {!isEditing && (
                <div className="field">
                    <label className="field__label" htmlFor="rt-type">
                        Type<span className="field__req">*</span>
                    </label>
                    <select
                        id="rt-type"
                        className="select"
                        value={transactionType}
                        onChange={(e) => setTransactionType(e.target.value as typeof transactionType)}
                    >
                        {RECURRING_TRANSACTION_TYPES.map((opt) => (
                            <option key={opt.value} value={opt.value}>
                                {opt.label}
                            </option>
                        ))}
                    </select>
                </div>
            )}

            <div className="form-row">
                <div className="field">
                    <label className="field__label" htmlFor="rt-amount">
                        Amount<span className="field__req">*</span>
                    </label>
                    <div className="input-affix">
                        <span className="input-affix__prefix">৳</span>
                        <input
                            id="rt-amount"
                            className="input"
                            type="number"
                            min="0.01"
                            step="0.01"
                            value={amount}
                            onChange={(e) => setAmount(e.target.value)}
                        />
                    </div>
                </div>

                <div className="field">
                    <label className="field__label" htmlFor="rt-frequency">
                        Frequency<span className="field__req">*</span>
                    </label>
                    <select
                        id="rt-frequency"
                        className="select"
                        value={frequency}
                        onChange={(e) => setFrequency(e.target.value as Frequency)}
                    >
                        {FREQUENCIES.map((opt) => (
                            <option key={opt.value} value={opt.value}>
                                {opt.label}
                            </option>
                        ))}
                    </select>
                </div>
            </div>

            {!isEditing && (
                <>
                    {shape.needsFromAccount && (
                        <div className="field">
                            <label className="field__label" htmlFor="rt-from-account">
                                {transactionType === "TRANSFER" ? "From Account" : "Account"}
                                <span className="field__req">*</span>
                            </label>
                            <select
                                id="rt-from-account"
                                className="select"
                                value={fromAccountId}
                                onChange={(e) => setFromAccountId(e.target.value)}
                            >
                                <option value="">Select an account</option>
                                {activeAccounts.map((a) => (
                                    <option key={a.id} value={a.id}>
                                        {a.name}
                                    </option>
                                ))}
                            </select>
                        </div>
                    )}

                    {shape.needsToAccount && (
                        <div className="field">
                            <label className="field__label" htmlFor="rt-to-account">
                                {transactionType === "TRANSFER" ? "To Account" : "Account"}
                                <span className="field__req">*</span>
                            </label>
                            <select
                                id="rt-to-account"
                                className="select"
                                value={toAccountId}
                                onChange={(e) => setToAccountId(e.target.value)}
                            >
                                <option value="">Select an account</option>
                                {activeAccounts.map((a) => (
                                    <option key={a.id} value={a.id}>
                                        {a.name}
                                    </option>
                                ))}
                            </select>
                        </div>
                    )}

                    {shape.needsCategory && (
                        <div className="field">
                            <label className="field__label" htmlFor="rt-category">
                                Category<span className="field__req">*</span>
                            </label>
                            <select
                                id="rt-category"
                                className="select"
                                value={categoryId}
                                onChange={(e) => setCategoryId(e.target.value)}
                            >
                                <option value="">Select a category</option>
                                {categoryOptions.map((c) => (
                                    <option key={c.id} value={c.id}>
                                        {c.name}
                                    </option>
                                ))}
                            </select>
                        </div>
                    )}

                    <div className="field">
                        <label className="field__label" htmlFor="rt-start-date">
                            Start Date<span className="field__req">*</span>
                        </label>
                        <input
                            id="rt-start-date"
                            className="input"
                            type="date"
                            value={startDate}
                            onChange={(e) => setStartDate(e.target.value)}
                        />
                    </div>
                </>
            )}

            <label className="field-checkbox">
                <input
                    type="checkbox"
                    checked={hasEndDate}
                    onChange={(e) => setHasEndDate(e.target.checked)}
                />
                Set an end date
            </label>

            {hasEndDate && (
                <div className="field">
                    <label className="field__label" htmlFor="rt-end-date">
                        End Date
                    </label>
                    <input
                        id="rt-end-date"
                        className="input"
                        type="date"
                        value={endDate}
                        onChange={(e) => setEndDate(e.target.value)}
                    />
                </div>
            )}

            <label className="field-checkbox">
                <input
                    type="checkbox"
                    checked={autoGenerate}
                    onChange={(e) => setAutoGenerate(e.target.checked)}
                />
                Generate automatically when due
            </label>
            <span className="field__hint">
                {autoGenerate
                    ? "Included when you click \"Run due transactions\"."
                    : "Due occurrences appear under \"Due Now\" for you to confirm manually."}
            </span>

            <div className="field">
                <label className="field__label" htmlFor="rt-description">
                    Description
                </label>
                <input
                    id="rt-description"
                    className="input"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    maxLength={255}
                    placeholder="Optional label"
                />
            </div>

            <div className="field">
                <label className="field__label" htmlFor="rt-notes">
                    Notes
                </label>
                <textarea
                    id="rt-notes"
                    className="textarea"
                    value={notes}
                    onChange={(e) => setNotes(e.target.value)}
                    placeholder="Optional notes"
                />
            </div>

            <div className="form-actions">
                <button type="submit" className="btn btn--primary" disabled={mutation.isPending}>
                    {mutation.isPending
                        ? "Saving…"
                        : isEditing
                          ? "Update Template"
                          : "Create Template"}
                </button>
            </div>
        </form>
    );
}
