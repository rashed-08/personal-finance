import { useState } from "react";

import { useLoans } from "../../hooks/useLoans";
import type { Loan } from "../../types/loan";

import LoanTable from "../../components/loans/LoanTable";
import LoanDialog from "../../components/loans/LoanDialog";
import LoanForm from "../../components/loans/LoanForm";
import RepayLoanForm from "../../components/loans/RepayLoanForm";

export default function LoansPage() {
    const { data = [], isLoading, error } = useLoans();

    const [open, setOpen] = useState(false);
    const [selectedLoan, setSelectedLoan] = useState<Loan | undefined>();

    const [repayOpen, setRepayOpen] = useState(false);
    const [repayingLoan, setRepayingLoan] = useState<Loan | undefined>();

    function createNew() {
        setSelectedLoan(undefined);
        setOpen(true);
    }

    function editLoan(loan: Loan) {
        setSelectedLoan(loan);
        setOpen(true);
    }

    function closeDialog() {
        setOpen(false);
        setSelectedLoan(undefined);
    }

    function repayLoan(loan: Loan) {
        setRepayingLoan(loan);
        setRepayOpen(true);
    }

    function closeRepayDialog() {
        setRepayOpen(false);
        setRepayingLoan(undefined);
    }

    return (
        <>
            <div className="page-header">
                <div>
                    <h1 className="page-header__title">Loans</h1>
                    <p className="page-header__subtitle">
                        Money lent to or borrowed from others. Tracked independently from
                        income and expenses.
                    </p>
                </div>

                <button
                    type="button"
                    className="btn btn--primary"
                    onClick={createNew}
                >
                    + New Loan
                </button>
            </div>

            <div className="card">
                {isLoading ? (
                    <div className="state">
                        <div className="spinner" />
                        <div className="state__desc">Loading loans…</div>
                    </div>
                ) : error ? (
                    <div className="state">
                        <div className="state__icon">⚠</div>
                        <div className="state__title">Couldn’t load loans</div>
                        <div className="state__desc">
                            Check that the backend is running on
                            <code> localhost:8080</code> and try again.
                        </div>
                    </div>
                ) : data.length === 0 ? (
                    <div className="state">
                        <div className="state__icon">🤝</div>
                        <div className="state__title">No loans yet</div>
                        <div className="state__desc">
                            Record a loan you've given or received to start tracking it.
                        </div>
                        <button
                            type="button"
                            className="btn btn--primary"
                            onClick={createNew}
                        >
                            + New Loan
                        </button>
                    </div>
                ) : (
                    <LoanTable loans={data} onEdit={editLoan} onRepay={repayLoan} />
                )}
            </div>

            <LoanDialog
                open={open}
                title={selectedLoan ? "Edit Loan" : "New Loan"}
                onClose={closeDialog}
            >
                <LoanForm loan={selectedLoan} onSuccess={closeDialog} />
            </LoanDialog>

            <LoanDialog
                open={repayOpen}
                title={
                    repayingLoan?.loanType === "PAYABLE" ? "Record Repayment" : "Record Collection"
                }
                onClose={closeRepayDialog}
            >
                {repayingLoan && (
                    <RepayLoanForm loan={repayingLoan} onSuccess={closeRepayDialog} />
                )}
            </LoanDialog>
        </>
    );
}
