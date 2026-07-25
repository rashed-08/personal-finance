import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { createSalaryCycle, getSalaryCycles } from "../services/salaryCycle.service";
import type { CreateSalaryCycleRequest } from "../types/salaryCycle";

const QUERY_KEY = ["salary-cycles"];

export function useSalaryCycles() {
    return useQuery({
        queryKey: QUERY_KEY,
        queryFn: getSalaryCycles,
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
