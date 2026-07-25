import { useState } from "react";

import { useSalaryCycles } from "../../hooks/useSalaryCycles";
import type { SalaryCycle } from "../../types/salaryCycle";

import SalaryCycleDialog from "../../components/salarycycles/SalaryCycleDialog";
import SalaryCycleForm from "../../components/salarycycles/SalaryCycleForm";
import SalaryCycleTable from "../../components/salarycycles/SalaryCycleTable";

export default function SalaryCyclesPage() {
    const { data = [], isLoading, error } = useSalaryCycles();

    const [open, setOpen] = useState(false);
    const [selectedCycle, setSelectedCycle] = useState<SalaryCycle | undefined>();

    function createNew() {
        setSelectedCycle(undefined);
        setOpen(true);
    }

    function editCycle(cycle: SalaryCycle) {
        setSelectedCycle(cycle);
        setOpen(true);
    }

    function closeDialog() {
        setOpen(false);
        setSelectedCycle(undefined);
    }

    const sorted = [...data].sort((a, b) => b.startDate.localeCompare(a.startDate));

    return (
        <>
            <div className="page-header">
                <div>
                    <h1 className="page-header__title">Salary Cycles</h1>
                    <p className="page-header__subtitle">
                        Reporting periods that follow your salary, not the calendar.
                        Cycles normally open and close automatically from income
                        transactions — create one manually only to backfill history.
                    </p>
                </div>

                <button type="button" className="btn btn--primary" onClick={createNew}>
                    + New Salary Cycle
                </button>
            </div>

            <div className="card">
                {isLoading ? (
                    <div className="state">
                        <div className="spinner" />
                        <div className="state__desc">Loading salary cycles…</div>
                    </div>
                ) : error ? (
                    <div className="state">
                        <div className="state__icon">⚠</div>
                        <div className="state__title">Couldn’t load salary cycles</div>
                        <div className="state__desc">
                            Check that the backend is running on
                            <code> localhost:8080</code> and try again.
                        </div>
                    </div>
                ) : sorted.length === 0 ? (
                    <div className="state">
                        <div className="state__icon">📅</div>
                        <div className="state__title">No salary cycles yet</div>
                        <div className="state__desc">
                            Record an income transaction with "start a new salary
                            cycle" checked, or create one manually to backfill
                            history.
                        </div>
                        <button
                            type="button"
                            className="btn btn--primary"
                            onClick={createNew}
                        >
                            + New Salary Cycle
                        </button>
                    </div>
                ) : (
                    <SalaryCycleTable salaryCycles={sorted} onEdit={editCycle} />
                )}
            </div>

            <SalaryCycleDialog
                open={open}
                title={selectedCycle ? "Edit Salary Cycle" : "New Salary Cycle"}
                onClose={closeDialog}
            >
                <SalaryCycleForm salaryCycle={selectedCycle} onSuccess={closeDialog} />
            </SalaryCycleDialog>
        </>
    );
}
