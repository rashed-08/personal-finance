import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import {
    createTransaction,
    getTransaction,
    getTransactions,
    reverseTransaction,
    updateTransaction,
    voidTransaction,
} from "../services/transaction.service";
import type { ListTransactionsParams } from "../services/transaction.service";
import type {
    CreateTransactionRequest,
    UpdateTransactionRequest,
} from "../types/transaction";

const QUERY_KEY = "transactions";

export function useTransactions(params: ListTransactionsParams) {
    return useQuery({
        queryKey: [QUERY_KEY, params],
        queryFn: () => getTransactions(params),
        placeholderData: (previous) => previous,
    });
}

export function useTransaction(id: string | undefined) {
    return useQuery({
        queryKey: [QUERY_KEY, id],
        queryFn: () => getTransaction(id as string),
        enabled: Boolean(id),
    });
}

export function useCreateTransaction() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (request: CreateTransactionRequest) => createTransaction(request),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
        },
    });
}

export function useUpdateTransaction() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ id, ...request }: { id: string } & UpdateTransactionRequest) =>
            updateTransaction(id, request),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
        },
    });
}

export function useVoidTransaction() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (id: string) => voidTransaction(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
        },
    });
}

export function useReverseTransaction() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (id: string) => reverseTransaction(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
        },
    });
}
