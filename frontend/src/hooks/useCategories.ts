import {
    useMutation,
    useQuery,
    useQueryClient,
} from "@tanstack/react-query";

import {
    activateCategory,
    createCategory,
    deactivateCategory,
    deleteCategory,
    getCategories,
    updateCategory,
} from "../services/category.service";

import type {
    CreateCategoryRequest,
    UpdateCategoryRequest,
} from "../types/category";

const QUERY_KEY = ["categories"];

export function useCategories() {

    return useQuery({

        queryKey: QUERY_KEY,

        queryFn: getCategories,

    });

}

export function useCreateCategory() {

    const queryClient =
        useQueryClient();

    return useMutation({

        mutationFn: (
            request: CreateCategoryRequest
        ) => createCategory(request),

        onSuccess: () => {

            queryClient.invalidateQueries({
                queryKey: QUERY_KEY,
            });

        },

    });

}

export function useUpdateCategory() {

    const queryClient =
        useQueryClient();

    return useMutation({

        mutationFn: ({
            id,
            ...request
        }: {
            id: string;
        } & UpdateCategoryRequest) =>
            updateCategory(id, request),

        onSuccess: () => {

            queryClient.invalidateQueries({
                queryKey: QUERY_KEY,
            });

        },

    });

}

export function useActivateCategory() {

    const queryClient =
        useQueryClient();

    return useMutation({

        mutationFn: (
            id: string
        ) => activateCategory(id),

        onSuccess: () => {

            queryClient.invalidateQueries({
                queryKey: QUERY_KEY,
            });

        },

    });

}

export function useDeactivateCategory() {

    const queryClient =
        useQueryClient();

    return useMutation({

        mutationFn: (
            id: string
        ) => deactivateCategory(id),

        onSuccess: () => {

            queryClient.invalidateQueries({
                queryKey: QUERY_KEY,
            });

        },

    });

}

export function useDeleteCategory() {

    const queryClient =
        useQueryClient();

    return useMutation({

        mutationFn: (
            id: string
        ) => deleteCategory(id),

        onSuccess: () => {

            queryClient.invalidateQueries({
                queryKey: QUERY_KEY,
            });

        },

    });

}