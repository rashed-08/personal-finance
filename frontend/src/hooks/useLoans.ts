import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import {
    closeLoan,
    createLoan,
    getLoans,
    recordRepayment,
    updateLoan,
} from "../services/loan.service";

const QUERY_KEY = "loans";

export function useLoans(activeOnly = false) {
    return useQuery({
        queryKey: [QUERY_KEY, activeOnly],
        queryFn: () => getLoans(activeOnly),
    });
}

export function useCreateLoan() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: createLoan,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
        },
    });
}

export function useUpdateLoan() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: updateLoan,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
        },
    });
}

export function useRecordRepayment() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: recordRepayment,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
        },
    });
}

export function useCloseLoan() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: closeLoan,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
        },
    });
}
