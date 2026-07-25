import { fundTypeLabel } from "../../lib/fundTypes";

interface Props {
    type: string;
}

export default function FundTypeBadge({ type }: Props) {
    const variant = `badge--fund-${type.toLowerCase()}`;

    return (
        <span className={`badge ${variant}`}>
            <span className="badge__dot" />
            {fundTypeLabel(type)}
        </span>
    );
}
