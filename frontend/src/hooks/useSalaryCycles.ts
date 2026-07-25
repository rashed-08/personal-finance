import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import {
    closeSalaryCycle,
    createSalaryCycle,
    getCarryForward,
    getCurrentSalaryCycle,
    getSalaryCycles,
    reopenSalaryCycle,
    updateSalaryCycle,
} from "../services/salaryCycle.service";
import type {
    CreateSalaryCycleRequest,
    UpdateSalaryCycleRequest,
} from "../types/salaryCycle";

const QUERY_KEY = ["salary-cycles"];

export function useSalaryCycles() {
    return useQuery({
        queryKey: QUERY_KEY,
        queryFn: getSalaryCycles,
    });
}

export function useCurrentSalaryCycle() {
    return useQuery({
        queryKey: [...QUERY_KEY, "current"],
        queryFn: getCurrentSalaryCycle,
        retry: false,
    });
}

export function useCarryForward(id: string | undefined) {
    return useQuery({
        queryKey: [...QUERY_KEY, id, "carry-forward"],
        queryFn: () => getCarryForward(id as string),
        enabled: Boolean(id),
    });
}

export function useCreateSalaryCycle() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (request: CreateSalaryCycleRequest) => createSalaryCycle(request),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: QUERY_KEY });
        },
    });
}

export function useUpdateSalaryCycle() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ id, ...request }: { id: string } & UpdateSalaryCycleRequest) =>
            updateSalaryCycle(id, request),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: QUERY_KEY });
        },
    });
}

export function useCloseSalaryCycle() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ id, endDate }: { id: string; endDate: string }) =>
            closeSalaryCycle(id, endDate),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: QUERY_KEY });
        },
    });
}

export function useReopenSalaryCycle() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (id: string) => reopenSalaryCycle(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: QUERY_KEY });
        },
    });
}
