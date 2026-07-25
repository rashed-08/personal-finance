import { useEffect, useState } from "react";
import type { FormEvent } from "react";

import { useAccounts } from "../../hooks/useAccounts";
import { useCategories } from "../../hooks/useCategories";
import { useCreateTransaction, useUpdateTransaction } from "../../hooks/useTransactions";
import {
    ADJUSTMENT_REASONS,
    TRANSACTION_TYPES,
    shapeFor,
} from "../../lib/transactionTypes";
import type {
    AdjustmentReason,
    CreateTransactionRequest,
    Transaction,
    TransactionType,
} from "../../types/transaction";
import SalaryCycleSelect from "../salarycycles/SalaryCycleSelect";

interface Props {
    transaction?: Transaction;
    onSuccess: () => void;
}

type AdjustmentDirection = "increase" | "decrease" | "none";

function errorMessage(err: unknown): string {
    const detail = (err as { response?: { data?: { detail?: string } } })
        ?.response?.data?.detail;
    return detail ?? "Something went wrong. Please try again.";
}

function today(): string {
    return new Date().toISOString().slice(0, 10);
}

export default function TransactionForm({ transaction, onSuccess }: Props) {
    const isEditing = Boolean(transaction);

    const { data: accounts = [] } = useAccounts();
    const { data: categories = [] } = useCategories();

    const createMutation = useCreateTransaction();
    const updateMutation = useUpdateTransaction();
    const mutation = isEditing ? updateMutation : createMutation;

    const [transactionType, setTransactionType] = useState<TransactionType>("EXPENSE");
    const [transactionDate, setTransactionDate] = useState(today());
    const [amount, setAmount] = useState("");
    const [fromAccountId, setFromAccountId] = useState("");
    const [toAccountId, setToAccountId] = useState("");
    const [categoryId, setCategoryId] = useState("");
    const [salaryCycleId, setSalaryCycleId] = useState("");
    const [description, setDescription] = useState("");
    const [notes, setNotes] = useState("");
    const [adjustmentReason, setAdjustmentReason] = useState<AdjustmentReason | "">("");
    const [adjustmentDirection, setAdjustmentDirection] =
        useState<AdjustmentDirection>("decrease");
    const [adjustmentAccountId, setAdjustmentAccountId] = useState("");
    const [migrationBatchId, setMigrationBatchId] = useState("");
    const [formError, setFormError] = useState<string | null>(null);

    useEffect(() => {
        if (!transaction) {
            setTransactionType("EXPENSE");
            setTransactionDate(today());
            setAmount("");
            setFromAccountId("");
            setToAccountId("");
            setCategoryId("");
            setSalaryCycleId("");
            setDescription("");
            setNotes("");
            setAdjustmentReason("");
            setAdjustmentDirection("decrease");
            setAdjustmentAccountId("");
            setMigrationBatchId("");
            setFormError(null);
            return;
        }

        setTransactionType(transaction.transactionType);
        setTransactionDate(transaction.transactionDate);
        setAmount(String(transaction.amount));
        setFromAccountId(transaction.fromAccountId ?? "");
        setToAccountId(transaction.toAccountId ?? "");
        setCategoryId(transaction.categoryId ?? "");
        setSalaryCycleId(transaction.salaryCycleId ?? "");
        setDescription(transaction.description ?? "");
        setNotes(transaction.notes ?? "");
        setFormError(null);
    }, [transaction]);

    const shape = shapeFor(transactionType);
    const needsCategory = isEditing
        ? transaction!.transactionType === "INCOME" ||
          transaction!.transactionType === "EXPENSE"
        : shape.needsCategory;

    const categoryOptions = categories.filter(
        (c) =>
            c.categoryType === (transactionType === "INCOME" ? "INCOME" : "EXPENSE") &&
            (c.active || c.id === categoryId),
    );

    const activeAccounts = accounts.filter((a) => a.active);

    function submit(e: FormEvent) {
        e.preventDefault();
        setFormError(null);

        const parsedAmount = Number(amount);
        if (!amount || !(parsedAmount > 0)) {
            setFormError("Amount must be greater than zero.");
            return;
        }

        if (isEditing) {
            if (needsCategory && !categoryId) {
                setFormError("Category is required.");
                return;
            }

            updateMutation.mutate(
                {
                    id: transaction!.id,
                    amount: parsedAmount,
                    categoryId: needsCategory ? categoryId : undefined,
                    description: description.trim() || undefined,
                    notes: notes.trim() || undefined,
                },
                { onSuccess },
            );
            return;
        }

        const request: CreateTransactionRequest = {
            transactionType,
            transactionDate,
            amount: parsedAmount,
            description: description.trim() || undefined,
            notes: notes.trim() || undefined,
        };

        if (transactionType === "ADJUSTMENT") {
            if (!adjustmentReason) {
                setFormError("Adjustment reason is required.");
                return;
            }
            if (
                adjustmentReason === "MANUAL_CORRECTION" &&
                !notes.trim()
            ) {
                setFormError("Manual correction requires notes explaining the adjustment.");
                return;
            }
            if (adjustmentDirection !== "none" && !adjustmentAccountId) {
                setFormError("Select which account this adjustment affects.");
                return;
            }

            request.adjustmentReason = adjustmentReason;
            if (adjustmentDirection === "increase") {
                request.toAccountId = adjustmentAccountId;
            } else if (adjustmentDirection === "decrease") {
                request.fromAccountId = adjustmentAccountId;
            }
        } else {
            if (shape.needsFromAccount && !fromAccountId) {
                setFormError("Source account is required.");
                return;
            }
            if (shape.needsToAccount && !toAccountId) {
                setFormError("Destination account is required.");
                return;
            }
            if (
                transactionType === "TRANSFER" &&
                fromAccountId &&
                fromAccountId === toAccountId
            ) {
                setFormError("Source and destination account cannot be the same.");
                return;
            }
            if (shape.needsCategory && !categoryId) {
                setFormError("Category is required.");
                return;
            }
            if (shape.needsSalaryCycle && !salaryCycleId) {
                setFormError("Salary cycle is required.");
                return;
            }
            if (transactionType === "MIGRATION" && !migrationBatchId.trim()) {
                setFormError("Migration batch id is required.");
                return;
            }

            if (shape.needsFromAccount) request.fromAccountId = fromAccountId;
            if (shape.needsToAccount) request.toAccountId = toAccountId;
            if (shape.needsCategory) request.categoryId = categoryId;
            if (shape.needsSalaryCycle) request.salaryCycleId = salaryCycleId;
            if (transactionType === "MIGRATION") {
                request.migrationBatchId = migrationBatchId.trim();
            }
        }

        createMutation.mutate(request, { onSuccess });
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

            {!isEditing && (
                <div className="field">
                    <label className="field__label" htmlFor="txn-type">
                        Transaction Type<span className="field__req">*</span>
                    </label>
                    <select
                        id="txn-type"
                        className="select"
                        value={transactionType}
                        onChange={(e) =>
                            setTransactionType(e.target.value as TransactionType)
                        }
                    >
                        {TRANSACTION_TYPES.map((t) => (
                            <option key={t.value} value={t.value}>
                                {t.label}
                            </option>
                        ))}
                    </select>
                </div>
            )}

            <div className="form-row">
                <div className="field">
                    <label className="field__label" htmlFor="txn-date">
                        Date<span className="field__req">*</span>
                    </label>
                    <input
                        id="txn-date"
                        type="date"
                        className="input"
                        value={transactionDate}
                        onChange={(e) => setTransactionDate(e.target.value)}
                        disabled={isEditing}
                    />
                </div>

                <div className="field">
                    <label className="field__label" htmlFor="txn-amount">
                        Amount<span className="field__req">*</span>
                    </label>
                    <div className="input-affix">
                        <span className="input-affix__prefix">৳</span>
                        <input
                            id="txn-amount"
                            className="input"
                            type="number"
                            min="0.01"
                            step="0.01"
                            value={amount}
                            onChange={(e) => setAmount(e.target.value)}
                            autoFocus
                        />
                    </div>
                    {isEditing && (
                        <span className="field__hint">
                            Changing this records a linked adjustment instead of
                            rewriting the original transaction.
                        </span>
                    )}
                </div>
            </div>

            {!isEditing && shape.needsFromAccount && (
                <div className="field">
                    <label className="field__label" htmlFor="txn-from-account">
                        {transactionType === "TRANSFER" ? "From Account" : "Account"}
                        <span className="field__req">*</span>
                    </label>
                    <select
                        id="txn-from-account"
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

            {!isEditing && shape.needsToAccount && (
                <div className="field">
                    <label className="field__label" htmlFor="txn-to-account">
                        {transactionType === "TRANSFER" ? "To Account" : "Account"}
                        <span className="field__req">*</span>
                    </label>
                    <select
                        id="txn-to-account"
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

            {!isEditing && transactionType === "ADJUSTMENT" && (
                <>
                    <div className="field">
                        <label className="field__label" htmlFor="txn-adjustment-reason">
                            Reason<span className="field__req">*</span>
                        </label>
                        <select
                            id="txn-adjustment-reason"
                            className="select"
                            value={adjustmentReason}
                            onChange={(e) =>
                                setAdjustmentReason(e.target.value as AdjustmentReason)
                            }
                        >
                            <option value="">Select a reason</option>
                            {ADJUSTMENT_REASONS.map((r) => (
                                <option key={r.value} value={r.value}>
                                    {r.label}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="form-row">
                        <div className="field">
                            <label className="field__label" htmlFor="txn-adjustment-direction">
                                Effect on Balance
                            </label>
                            <select
                                id="txn-adjustment-direction"
                                className="select"
                                value={adjustmentDirection}
                                onChange={(e) =>
                                    setAdjustmentDirection(
                                        e.target.value as AdjustmentDirection,
                                    )
                                }
                            >
                                <option value="decrease">Decrease account balance</option>
                                <option value="increase">Increase account balance</option>
                                <option value="none">Not tied to an account</option>
                            </select>
                        </div>

                        {adjustmentDirection !== "none" && (
                            <div className="field">
                                <label className="field__label" htmlFor="txn-adjustment-account">
                                    Account<span className="field__req">*</span>
                                </label>
                                <select
                                    id="txn-adjustment-account"
                                    className="select"
                                    value={adjustmentAccountId}
                                    onChange={(e) => setAdjustmentAccountId(e.target.value)}
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
                    </div>
                </>
            )}

            {!isEditing && transactionType === "MIGRATION" && (
                <div className="field">
                    <label className="field__label" htmlFor="txn-migration-batch">
                        Migration Batch Id<span className="field__req">*</span>
                    </label>
                    <input
                        id="txn-migration-batch"
                        className="input"
                        value={migrationBatchId}
                        onChange={(e) => setMigrationBatchId(e.target.value)}
                        placeholder="e.g. google-keep-2026-07"
                    />
                </div>
            )}

            {needsCategory && (
                <div className="field">
                    <label className="field__label" htmlFor="txn-category">
                        Category<span className="field__req">*</span>
                    </label>
                    <select
                        id="txn-category"
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

            {!isEditing && shape.needsSalaryCycle && (
                <SalaryCycleSelect
                    id="txn-salary-cycle"
                    value={salaryCycleId}
                    onChange={setSalaryCycleId}
                />
            )}

            <div className="field">
                <label className="field__label" htmlFor="txn-description">
                    Description
                </label>
                <input
                    id="txn-description"
                    className="input"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    maxLength={255}
                    placeholder="e.g. Groceries, ATM withdrawal"
                />
            </div>

            <div className="field">
                <label className="field__label" htmlFor="txn-notes">
                    Notes
                </label>
                <textarea
                    id="txn-notes"
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
                          ? "Update Transaction"
                          : "Create Transaction"}
                </button>
            </div>
        </form>
    );
}
