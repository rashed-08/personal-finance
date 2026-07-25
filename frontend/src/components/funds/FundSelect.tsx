import { useFunds } from "../../hooks/useFunds";

interface Props {
    id: string;
    value: string;
    onChange(fundId: string): void;
}

/** Dropdown for picking an active fund, used by fund-linked transfers. */
export default function FundSelect({ id, value, onChange }: Props) {
    const { data = [], isLoading } = useFunds(true);

    return (
        <div className="field">
            <label className="field__label" htmlFor={id}>
                Fund<span className="field__req">*</span>
            </label>
            <select
                id={id}
                className="select"
                value={value}
                onChange={(e) => onChange(e.target.value)}
                disabled={isLoading}
            >
                <option value="">
                    {isLoading ? "Loading…" : "Select a fund"}
                </option>
                {data.map((fund) => (
                    <option key={fund.id} value={fund.id}>
                        {fund.name}
                    </option>
                ))}
            </select>
            {!isLoading && data.length === 0 && (
                <span className="field__hint">
                    No active funds yet — create one on the Funds page.
                </span>
            )}
        </div>
    );
}
