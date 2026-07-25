import { useMutation, useQueryClient } from "@tanstack/react-query";

import { importGoogleKeep } from "../services/migration.service";

export function useImportGoogleKeep() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: importGoogleKeep,
        onSuccess: () => {
            // A successful import creates accounts, salary cycles, and transactions,
            // and changes every total/graph derived from them.
            queryClient.invalidateQueries({ queryKey: ["accounts"] });
            queryClient.invalidateQueries({ queryKey: ["salary-cycles"] });
            queryClient.invalidateQueries({ queryKey: ["transactions"] });
            queryClient.invalidateQueries({ queryKey: ["reports"] });
        },
    });
}
