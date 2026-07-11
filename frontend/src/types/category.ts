export type CategoryType =
    | "INCOME"
    | "EXPENSE";

export interface Category {

    id: string;

    name: string;

    categoryType: CategoryType;

    systemDefined: boolean;

    active: boolean;

    description: string | null;

    createdAt: string;

    updatedAt: string;

}

export interface CreateCategoryRequest {

    name: string;

    categoryType: CategoryType;

    systemDefined: boolean;

    description: string;

}

export interface UpdateCategoryRequest {

    name: string;

    categoryType: CategoryType;

    description: string;

}