import { transactionTypeLabel } from "../../lib/transactionTypes";

interface Props {
    type: string;
}

export default function TransactionTypeBadge({ type }: Props) {
    const variant = `badge--txn-${type.toLowerCase().replace(/_/g, "-")}`;

    return (
        <span className={`badge ${variant}`}>
            <span className="badge__dot" />
            {transactionTypeLabel(type)}
        </span>
    );
}
