import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import {
    activateRecurringTransaction,
    createRecurringTransaction,
    deactivateRecurringTransaction,
    deleteRecurringTransaction,
    generateRecurringTransactionNow,
    getDueRecurringTransactions,
    getRecurringTransactionExecutions,
    getRecurringTransactions,
    runDueRecurringTransactions,
    updateRecurringTransaction,
} from "../services/recurringTransaction.service";

const QUERY_KEY = "recurringTransactions";
const DUE_QUERY_KEY = "recurringTransactionsDue";

export function useRecurringTransactions(activeOnly = false) {
    return useQuery({
        queryKey: [QUERY_KEY, activeOnly],
        queryFn: () => getRecurringTransactions(activeOnly),
    });
}

export function useDueRecurringTransactions() {
    return useQuery({
        queryKey: [DUE_QUERY_KEY],
        queryFn: getDueRecurringTransactions,
    });
}

export function useRecurringTransactionExecutions(id: string | undefined) {
    return useQuery({
        queryKey: [QUERY_KEY, id, "executions"],
        queryFn: () => getRecurringTransactionExecutions(id as string),
        enabled: Boolean(id),
    });
}

function invalidateAll(queryClient: ReturnType<typeof useQueryClient>) {
    queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
    queryClient.invalidateQueries({ queryKey: [DUE_QUERY_KEY] });
}

export function useCreateRecurringTransaction() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: createRecurringTransaction,
        onSuccess: () => invalidateAll(queryClient),
    });
}

export function useUpdateRecurringTransaction() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: updateRecurringTransaction,
        onSuccess: () => invalidateAll(queryClient),
    });
}

export function useActivateRecurringTransaction() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: activateRecurringTransaction,
        onSuccess: () => invalidateAll(queryClient),
    });
}

export function useDeactivateRecurringTransaction() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: deactivateRecurringTransaction,
        onSuccess: () => invalidateAll(queryClient),
    });
}

export function useGenerateRecurringTransactionNow() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: generateRecurringTransactionNow,
        onSuccess: () => invalidateAll(queryClient),
    });
}

export function useRunDueRecurringTransactions() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: runDueRecurringTransactions,
        onSuccess: () => invalidateAll(queryClient),
    });
}

export function useDeleteRecurringTransaction() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: deleteRecurringTransaction,
        onSuccess: () => invalidateAll(queryClient),
    });
}
