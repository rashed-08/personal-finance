import { useState } from "react";

import { useFunds } from "../../hooks/useFunds";
import type { Fund } from "../../types/fund";

import FundTable from "../../components/funds/FundTable";
import FundDialog from "../../components/funds/FundDialog";
import FundForm from "../../components/funds/FundForm";

export default function FundsPage() {
    const { data = [], isLoading, error } = useFunds();

    const [open, setOpen] = useState(false);
    const [selectedFund, setSelectedFund] = useState<Fund | undefined>();

    function createNew() {
        setSelectedFund(undefined);
        setOpen(true);
    }

    function editFund(fund: Fund) {
        setSelectedFund(fund);
        setOpen(true);
    }

    function closeDialog() {
        setOpen(false);
        setSelectedFund(undefined);
    }

    return (
        <>
            <div className="page-header">
                <div>
                    <h1 className="page-header__title">Funds</h1>
                    <p className="page-header__subtitle">
                        Money reserved for a purpose — savings goals, emergency
                        reserves, zakat. Balances are derived from linked transfers.
                    </p>
                </div>

                <button
                    type="button"
                    className="btn btn--primary"
                    onClick={createNew}
                >
                    + New Fund
                </button>
            </div>

            <div className="card">
                {isLoading ? (
                    <div className="state">
                        <div className="spinner" />
                        <div className="state__desc">Loading funds…</div>
                    </div>
                ) : error ? (
                    <div className="state">
                        <div className="state__icon">⚠</div>
                        <div className="state__title">Couldn’t load funds</div>
                        <div className="state__desc">
                            Check that the backend is running on
                            <code> localhost:8080</code> and try again.
                        </div>
                    </div>
                ) : data.length === 0 ? (
                    <div className="state">
                        <div className="state__icon">🎯</div>
                        <div className="state__title">No funds yet</div>
                        <div className="state__desc">
                            Create a fund to start setting aside money for a
                            purpose.
                        </div>
                        <button
                            type="button"
                            className="btn btn--primary"
                            onClick={createNew}
                        >
                            + New Fund
                        </button>
                    </div>
                ) : (
                    <FundTable funds={data} onEdit={editFund} />
                )}
            </div>

            <FundDialog
                open={open}
                title={selectedFund ? "Edit Fund" : "New Fund"}
                onClose={closeDialog}
            >
                <FundForm fund={selectedFund} onSuccess={closeDialog} />
            </FundDialog>
        </>
    );
}
