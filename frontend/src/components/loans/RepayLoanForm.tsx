import { useState } from "react";
import type { FormEvent } from "react";

import { useAccounts } from "../../hooks/useAccounts";
import { useRecordRepayment } from "../../hooks/useLoans";
import { formatCurrency } from "../../lib/format";
import type { Loan } from "../../types/loan";
import SalaryCycleSelect from "../salarycycles/SalaryCycleSelect";

interface Props {
    loan: Loan;
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

export default function RepayLoanForm({ loan, onSuccess }: Props) {
    const { data: accounts = [] } = useAccounts();
    const activeAccounts = accounts.filter((a) => a.active);
    const mutation = useRecordRepayment();

    const [accountId, setAccountId] = useState("");
    const [amount, setAmount] = useState("");
    const [paymentDate, setPaymentDate] = useState(today());
    const [salaryCycleId, setSalaryCycleId] = useState("");
    const [description, setDescription] = useState("");
    const [formError, setFormError] = useState<string | null>(null);

    const isReceivable = loan.loanType === "RECEIVABLE";

    function submit(e: FormEvent) {
        e.preventDefault();
        setFormError(null);

        const parsedAmount = Number(amount);
        if (!amount || !(parsedAmount > 0)) {
            setFormError("Amount must be greater than zero.");
            return;
        }
        if (parsedAmount > loan.outstandingBalance) {
            setFormError(
                `Amount cannot exceed the outstanding balance of ${formatCurrency(loan.outstandingBalance)}.`,
            );
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

        mutation.mutate(
            {
                loanId: loan.id,
                accountId,
                amount: parsedAmount,
                paymentDate,
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

            <p className="field__hint">
                Outstanding balance: <strong>{formatCurrency(loan.outstandingBalance)}</strong>
            </p>

            <div className="form-row">
                <div className="field">
                    <label className="field__label" htmlFor="repay-amount">
                        Amount<span className="field__req">*</span>
                    </label>
                    <div className="input-affix">
                        <span className="input-affix__prefix">৳</span>
                        <input
                            id="repay-amount"
                            className="input"
                            type="number"
                            min="0.01"
                            step="0.01"
                            value={amount}
                            onChange={(e) => setAmount(e.target.value)}
                            autoFocus
                        />
                    </div>
                </div>

                <div className="field">
                    <label className="field__label" htmlFor="repay-date">
                        Date<span className="field__req">*</span>
                    </label>
                    <input
                        id="repay-date"
                        className="input"
                        type="date"
                        value={paymentDate}
                        onChange={(e) => setPaymentDate(e.target.value)}
                    />
                </div>
            </div>

            <div className="field">
                <label className="field__label" htmlFor="repay-account">
                    {isReceivable ? "To Account" : "From Account"}
                    <span className="field__req">*</span>
                </label>
                <select
                    id="repay-account"
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
                    {isReceivable
                        ? "Money enters this account when the borrower repays."
                        : "Money leaves this account when you repay the lender."}
                </span>
            </div>

            <SalaryCycleSelect
                id="repay-salary-cycle"
                value={salaryCycleId}
                onChange={setSalaryCycleId}
            />

            <div className="field">
                <label className="field__label" htmlFor="repay-description">
                    Description
                </label>
                <input
                    id="repay-description"
                    className="input"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    maxLength={255}
                    placeholder="Optional notes"
                />
            </div>

            <div className="form-actions">
                <button type="submit" className="btn btn--primary" disabled={mutation.isPending}>
                    {mutation.isPending ? "Recording…" : "Record Repayment"}
                </button>
            </div>
        </form>
    );
}
