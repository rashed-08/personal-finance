import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { getSettings, updateSettings } from "../services/settings.service";

const QUERY_KEY = "settings";

export function useSettings() {
    return useQuery({
        queryKey: [QUERY_KEY],
        queryFn: getSettings,
    });
}

export function useUpdateSettings() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: updateSettings,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });

            // Backup settings live in the same table, so the backup
            // screen's configuration is stale after any settings change.
            queryClient.invalidateQueries({ queryKey: ["backups"] });
        },
    });
}
