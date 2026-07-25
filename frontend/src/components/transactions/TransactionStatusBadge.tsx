import { transactionStatusLabel } from "../../lib/transactionTypes";

interface Props {
    status: string;
}

export default function TransactionStatusBadge({ status }: Props) {
    const variant =
        status === "POSTED"
            ? "pill--active"
            : status === "VOID"
              ? "pill--void"
              : "pill--reversed";

    return (
        <span className={`pill ${variant}`}>
            <span className="pill__dot" />
            {transactionStatusLabel(status)}
        </span>
    );
}
