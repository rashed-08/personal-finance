import { useCarryForward } from "../../hooks/useSalaryCycles";
import { formatCurrency } from "../../lib/format";

interface Props {
    salaryCycleId: string;
}

export default function CarryForwardPanel({ salaryCycleId }: Props) {
    const { data, isLoading, error } = useCarryForward(salaryCycleId);

    if (isLoading) {
        return <div className="carry-forward-panel">Calculating…</div>;
    }

    if (error || !data) {
        return (
            <div className="carry-forward-panel carry-forward-panel--error">
                Couldn’t calculate carry forward for this cycle.
            </div>
        );
    }

    return (
        <div className="carry-forward-panel">
            <div className="carry-forward-panel__item">
                <span>Opening Balance</span>
                <strong>{formatCurrency(data.openingBalance)}</strong>
            </div>
            <div className="carry-forward-panel__item cell-amount--increase">
                <span>+ Income</span>
                <strong>{formatCurrency(data.income)}</strong>
            </div>
            <div className="carry-forward-panel__item cell-amount--decrease">
                <span>− Expenses</span>
                <strong>{formatCurrency(data.expenses)}</strong>
            </div>
            <div className="carry-forward-panel__item">
                <span>± Adjustments</span>
                <strong>{formatCurrency(data.adjustments)}</strong>
            </div>
            <div className="carry-forward-panel__item carry-forward-panel__item--total">
                <span>Closing Balance / Carry Forward</span>
                <strong>{formatCurrency(data.closingBalance)}</strong>
            </div>
        </div>
    );
}
