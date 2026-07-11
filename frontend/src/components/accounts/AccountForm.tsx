import { useEffect, useState } from "react";
import type { FormEvent } from "react";

import type { Account } from "../../types/account";
import { ACCOUNT_TYPES } from "../../lib/accountTypes";
import { useCreateAccount, useUpdateAccount } from "../../hooks/useAccounts";

interface Props {
    account?: Account;
    onSuccess: () => void;
}

function errorMessage(err: unknown): string {
    // Backend returns an RFC-7807 ProblemDetail with a "detail" field.
    const detail = (err as { response?: { data?: { detail?: string } } })
        ?.response?.data?.detail;
    return detail ?? "Something went wrong. Please try again.";
}

export default function AccountForm({ account, onSuccess }: Props) {
    const createMutation = useCreateAccount();
    const updateMutation = useUpdateAccount();

    const [name, setName] = useState("");
    const [type, setType] = useState("CASH");
    const [balance, setBalance] = useState("0");
    const [description, setDescription] = useState("");
    const [nameError, setNameError] = useState<string | null>(null);

    useEffect(() => {
        if (!account) {
            setName("");
            setType("CASH");
            setBalance("0");
            setDescription("");
            setNameError(null);
            return;
        }

        setName(account.name);
        setType(account.accountType);
        setBalance(String(account.openingBalance));
        setDescription(account.description ?? "");
        setNameError(null);
    }, [account]);

    const isEditing = Boolean(account);
    const mutation = isEditing ? updateMutation : createMutation;
    const isPending = mutation.isPending;

    function submit(e: FormEvent) {
        e.preventDefault();

        if (!name.trim()) {
            setNameError("Account name is required.");
            return;
        }
        setNameError(null);

        const payload = {
            name: name.trim(),
            accountType: type,
            openingBalance: Number(balance) || 0,
            description: description.trim(),
        };

        if (account) {
            updateMutation.mutate(
                { id: account.id, ...payload },
                { onSuccess },
            );
        } else {
            createMutation.mutate(payload, { onSuccess });
        }
    }

    return (
        <form className="form" onSubmit={submit} noValidate>
            {mutation.isError && (
                <div className="form-error" role="alert">
                    <span>⚠</span>
                    <span>{errorMessage(mutation.error)}</span>
                </div>
            )}

            <div className="field">
                <label className="field__label" htmlFor="account-name">
                    Name<span className="field__req">*</span>
                </label>
                <input
                    id="account-name"
                    className={nameError ? "input input--invalid" : "input"}
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="e.g. BRAC Bank, Cash Wallet, bKash"
                    maxLength={100}
                    autoFocus
                />
                {nameError && <span className="field__error">{nameError}</span>}
            </div>

            <div className="field">
                <label className="field__label" htmlFor="account-type">
                    Type<span className="field__req">*</span>
                </label>
                <select
                    id="account-type"
                    className="select"
                    value={type}
                    onChange={(e) => setType(e.target.value)}
                >
                    {ACCOUNT_TYPES.map((opt) => (
                        <option key={opt.value} value={opt.value}>
                            {opt.label}
                        </option>
                    ))}
                </select>
            </div>

            <div className="field">
                <label className="field__label" htmlFor="account-balance">
                    Opening Balance
                </label>
                <div className="input-affix">
                    <span className="input-affix__prefix">৳</span>
                    <input
                        id="account-balance"
                        className="input"
                        type="number"
                        min="0"
                        step="0.01"
                        value={balance}
                        onChange={(e) => setBalance(e.target.value)}
                    />
                </div>
                <span className="field__hint">
                    Starting balance when this account was opened. Current balance
                    is derived from transactions.
                </span>
            </div>

            <div className="field">
                <label className="field__label" htmlFor="account-description">
                    Description
                </label>
                <textarea
                    id="account-description"
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
                          ? "Update Account"
                          : "Create Account"}
                </button>
            </div>
        </form>
    );
}
