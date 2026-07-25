import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import {
    completeReconciliation,
    getReconciliation,
    getReconciliations,
    recordCashSnapshot,
    startReconciliation,
} from "../services/cashReconciliation.service";
import type {
    RecordCashSnapshotRequest,
    StartReconciliationRequest,
} from "../types/cashReconciliation";

const QUERY_KEY = "cash-reconciliations";

export function useReconciliations(accountId?: string) {
    return useQuery({
        queryKey: [QUERY_KEY, accountId ?? "all"],
        queryFn: () => getReconciliations(accountId),
    });
}

export function useReconciliation(id: string | undefined) {
    return useQuery({
        queryKey: [QUERY_KEY, "detail", id],
        queryFn: () => getReconciliation(id as string),
        enabled: Boolean(id),
    });
}

export function useStartReconciliation() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (request: StartReconciliationRequest) => startReconciliation(request),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
        },
    });
}

export function useRecordCashSnapshot() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ id, ...request }: { id: string } & RecordCashSnapshotRequest) =>
            recordCashSnapshot(id, request),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
        },
    });
}

export function useCompleteReconciliation() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (id: string) => completeReconciliation(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
        },
    });
}
