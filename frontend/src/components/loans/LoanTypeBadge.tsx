import { loanTypeLabel } from "../../lib/loanTypes";

interface Props {
    type: string;
}

export default function LoanTypeBadge({ type }: Props) {
    const variant = `badge--loan-${type.toLowerCase()}`;

    return (
        <span className={`badge ${variant}`}>
            <span className="badge__dot" />
            {loanTypeLabel(type)}
        </span>
    );
}
