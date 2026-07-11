import api from "../api/api";

import type {
    Category,
    CreateCategoryRequest,
    UpdateCategoryRequest,
} from "../types/category";

const BASE_URL = "/categories";

export async function getCategories(): Promise<Category[]> {

    const response = await api.get<Category[]>(BASE_URL);

    return response.data;

}

export async function getCategory(
    id: string
): Promise<Category> {

    const response =
        await api.get<Category>(
            `${BASE_URL}/${id}`
        );

    return response.data;

}

export async function createCategory(
    request: CreateCategoryRequest
): Promise<Category> {

    const response =
        await api.post<Category>(
            BASE_URL,
            request
        );

    return response.data;

}

export async function updateCategory(
    id: string,
    request: UpdateCategoryRequest
): Promise<Category> {

    const response =
        await api.put<Category>(
            `${BASE_URL}/${id}`,
            request
        );

    return response.data;

}

export async function activateCategory(
    id: string
): Promise<Category> {

    const response =
        await api.patch<Category>(
            `${BASE_URL}/${id}/activate`
        );

    return response.data;

}

export async function deactivateCategory(
    id: string
): Promise<Category> {

    const response =
        await api.patch<Category>(
            `${BASE_URL}/${id}/deactivate`
        );

    return response.data;

}

export async function deleteCategory(
    id: string
): Promise<void> {

    await api.delete(
        `${BASE_URL}/${id}`
    );

}