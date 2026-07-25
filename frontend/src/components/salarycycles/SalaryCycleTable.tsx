import { Fragment, useState } from "react";

import { useCloseSalaryCycle, useReopenSalaryCycle } from "../../hooks/useSalaryCycles";
import type { SalaryCycle } from "../../types/salaryCycle";
import CarryForwardPanel from "./CarryForwardPanel";

interface Props {
    salaryCycles: SalaryCycle[];
    onEdit(salaryCycle: SalaryCycle): void;
}

export default function SalaryCycleTable({ salaryCycles, onEdit }: Props) {
    const closeMutation = useCloseSalaryCycle();
    const reopenMutation = useReopenSalaryCycle();

    const [closingId, setClosingId] = useState<string | null>(null);
    const [closeEndDate, setCloseEndDate] = useState("");
    const [expandedId, setExpandedId] = useState<string | null>(null);

    function startClosing(cycle: SalaryCycle) {
        setClosingId(cycle.id);
        setCloseEndDate("");
    }

    function confirmClose(id: string) {
        closeMutation.mutate(
            { id, endDate: closeEndDate },
            { onSettled: () => setClosingId(null) },
        );
    }

    return (
        <div className="table-wrap">
            <table className="table">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Start</th>
                        <th>End</th>
                        <th>Salary Date</th>
                        <th>Status</th>
                        <th className="col-right">Actions</th>
                    </tr>
                </thead>

                <tbody>
                    {salaryCycles.map((cycle) => {
                        const isClosingThis = closingId === cycle.id;
                        const isClosePending =
                            closeMutation.isPending &&
                            closeMutation.variables?.id === cycle.id;
                        const isReopenPending =
                            reopenMutation.isPending && reopenMutation.variables === cycle.id;

                        return (
                            <Fragment key={cycle.id}>
                                <tr>
                                    <td>
                                        <div className="cell-name">{cycle.name}</div>
                                        {cycle.description && (
                                            <div className="cell-desc">{cycle.description}</div>
                                        )}
                                    </td>
                                    <td>{cycle.startDate}</td>
                                    <td>{cycle.endDate ?? "Ongoing"}</td>
                                    <td>{cycle.salaryDate}</td>
                                    <td>
                                        <span
                                            className={
                                                cycle.closed ? "pill pill--inactive" : "pill pill--active"
                                            }
                                        >
                                            <span className="pill__dot" />
                                            {cycle.closed ? "Closed" : "Open"}
                                        </span>
                                    </td>
                                    <td>
                                        {isClosingThis ? (
                                            <div className="row-actions confirm">
                                                <input
                                                    type="date"
                                                    className="input"
                                                    aria-label="End date"
                                                    value={closeEndDate}
                                                    onChange={(e) => setCloseEndDate(e.target.value)}
                                                />
                                                <button
                                                    type="button"
                                                    className="btn btn--primary btn--sm"
                                                    disabled={!closeEndDate || isClosePending}
                                                    onClick={() => confirmClose(cycle.id)}
                                                >
                                                    {isClosePending ? "Closing…" : "Confirm"}
                                                </button>
                                                <button
                                                    type="button"
                                                    className="btn btn--ghost btn--sm"
                                                    onClick={() => setClosingId(null)}
                                                >
                                                    Cancel
                                                </button>
                                            </div>
                                        ) : (
                                            <div className="row-actions">
                                                <button
                                                    type="button"
                                                    className="btn btn--ghost btn--sm"
                                                    onClick={() => onEdit(cycle)}
                                                >
                                                    Edit
                                                </button>

                                                <button
                                                    type="button"
                                                    className="btn btn--ghost btn--sm"
                                                    onClick={() =>
                                                        setExpandedId(
                                                            expandedId === cycle.id ? null : cycle.id,
                                                        )
                                                    }
                                                >
                                                    {expandedId === cycle.id ? "Hide" : "Carry Forward"}
                                                </button>

                                                {cycle.closed ? (
                                                    <button
                                                        type="button"
                                                        className="btn btn--ghost btn--sm"
                                                        disabled={isReopenPending}
                                                        onClick={() => reopenMutation.mutate(cycle.id)}
                                                    >
                                                        {isReopenPending ? "Reopening…" : "Reopen"}
                                                    </button>
                                                ) : (
                                                    <button
                                                        type="button"
                                                        className="btn btn--ghost btn--sm"
                                                        onClick={() => startClosing(cycle)}
                                                    >
                                                        Close
                                                    </button>
                                                )}
                                            </div>
                                        )}
                                    </td>
                                </tr>

                                {expandedId === cycle.id && (
                                    <tr>
                                        <td colSpan={6} style={{ padding: 0 }}>
                                            <CarryForwardPanel salaryCycleId={cycle.id} />
                                        </td>
                                    </tr>
                                )}
                            </Fragment>
                        );
                    })}
                </tbody>
            </table>
        </div>
    );
}
