import { categoryTypeLabel } from "../../lib/categoryTypes";
import type { CategoryType } from "../../types/category";

interface Props {
    type: CategoryType;
}

export default function CategoryTypeBadge({
    type,
}: Props) {

    return (
        <span
            className={`badge badge--${type.toLowerCase()}`}
        >
            <span className="badge__dot" />

            {categoryTypeLabel(type)}
        </span>
    );

}