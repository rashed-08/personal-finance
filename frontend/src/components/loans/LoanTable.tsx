import type { Loan } from "../../types/loan";

import { useCloseLoan } from "../../hooks/useLoans";
import { formatCurrency } from "../../lib/format";
import LoanTypeBadge from "./LoanTypeBadge";

interface Props {
    loans: Loan[];
    onEdit(loan: Loan): void;
    onRepay(loan: Loan): void;
}

function statusPillClass(status: Loan["loanStatus"]): string {
    switch (status) {
        case "ACTIVE":
            return "pill pill--active";
        case "CANCELLED":
            return "pill pill--reversed";
        default:
            return "pill pill--inactive";
    }
}

function statusLabel(status: Loan["loanStatus"]): string {
    switch (status) {
        case "ACTIVE":
            return "Active";
        case "CANCELLED":
            return "Cancelled";
        default:
            return "Closed";
    }
}

export default function LoanTable({ loans, onEdit, onRepay }: Props) {
    const closeMutation = useCloseLoan();

    return (
        <div className="table-wrap">
            <table className="table">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Type</th>
                        <th className="col-right">Principal</th>
                        <th className="col-right">Outstanding</th>
                        <th>Status</th>
                        <th className="col-right">Actions</th>
                    </tr>
                </thead>

                <tbody>
                    {loans.map((loan) => {
                        const isClosing =
                            closeMutation.isPending && closeMutation.variables === loan.id;

                        return (
                            <tr key={loan.id}>
                                <td>
                                    <div className="cell-name">{loan.name}</div>
                                    {loan.description && (
                                        <div className="cell-desc">{loan.description}</div>
                                    )}
                                </td>

                                <td>
                                    <LoanTypeBadge type={loan.loanType} />
                                </td>

                                <td className="col-right cell-amount">
                                    {formatCurrency(loan.principalAmount)}
                                </td>

                                <td className="col-right cell-amount">
                                    {formatCurrency(loan.outstandingBalance)}
                                </td>

                                <td>
                                    <span className={statusPillClass(loan.loanStatus)}>
                                        <span className="pill__dot" />
                                        {statusLabel(loan.loanStatus)}
                                    </span>
                                </td>

                                <td>
                                    <div className="row-actions">
                                        <button
                                            type="button"
                                            className="btn btn--ghost btn--sm"
                                            onClick={() => onEdit(loan)}
                                        >
                                            Edit
                                        </button>

                                        {loan.loanStatus === "ACTIVE" && (
                                            <>
                                                <button
                                                    type="button"
                                                    className="btn btn--ghost btn--sm"
                                                    disabled={loan.outstandingBalance <= 0}
                                                    onClick={() => onRepay(loan)}
                                                >
                                                    Repay
                                                </button>

                                                <button
                                                    type="button"
                                                    className="btn btn--ghost btn--sm"
                                                    disabled={
                                                        loan.outstandingBalance !== 0 ||
                                                        isClosing
                                                    }
                                                    title={
                                                        loan.outstandingBalance !== 0
                                                            ? "A loan can only be closed while its outstanding balance is zero."
                                                            : undefined
                                                    }
                                                    onClick={() =>
                                                        closeMutation.mutate(loan.id)
                                                    }
                                                >
                                                    {isClosing ? "Closing…" : "Close"}
                                                </button>
                                            </>
                                        )}
                                    </div>
                                </td>
                            </tr>
                        );
                    })}
                </tbody>
            </table>
        </div>
    );
}
