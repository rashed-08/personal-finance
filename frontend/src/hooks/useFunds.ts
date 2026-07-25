import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import {
    activateFund,
    createFund,
    deactivateFund,
    getFunds,
    updateFund,
} from "../services/fund.service";

const QUERY_KEY = "funds";

export function useFunds(activeOnly = false) {
    return useQuery({
        queryKey: [QUERY_KEY, activeOnly],
        queryFn: () => getFunds(activeOnly),
    });
}

export function useCreateFund() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: createFund,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
        },
    });
}

export function useUpdateFund() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: updateFund,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
        },
    });
}

export function useActivateFund() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: activateFund,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
        },
    });
}

export function useDeactivateFund() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: deactivateFund,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
        },
    });
}
