import { accountTypeLabel } from "../../lib/accountTypes";

interface Props {
    type: string;
}

export default function AccountTypeBadge({ type }: Props) {
    const variant = `badge--${type.toLowerCase()}`;

    return (
        <span className={`badge ${variant}`}>
            <span className="badge__dot" />
            {accountTypeLabel(type)}
        </span>
    );
}
