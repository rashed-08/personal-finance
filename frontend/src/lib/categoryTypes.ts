import type { CategoryType } from "../types/category";

export interface CategoryTypeOption {

    value: CategoryType;

    label: string;

}

export const CATEGORY_TYPES: CategoryTypeOption[] = [

    {
        value: "EXPENSE",
        label: "Expense",
    },

    {
        value: "INCOME",
        label: "Income",
    },

];

const LABELS: Record<CategoryType, string> = {

    EXPENSE: "Expense",

    INCOME: "Income",

};

export function categoryTypeLabel(
    type: CategoryType
): string {

    return LABELS[type];

}