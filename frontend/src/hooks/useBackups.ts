import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import {
    beginGoogleDriveAuthorization,
    completeGoogleDriveAuthorization,
    createBackup,
    deleteBackup,
    disconnectGoogleDrive,
    getBackupConfiguration,
    getBackupHistory,
    restoreBackup,
    restoreFromUpload,
} from "../services/backup.service";
import type { BackupOperation } from "../types/backup";

const QUERY_KEY = "backups";

export function useBackupHistory(operation?: BackupOperation) {
    return useQuery({
        queryKey: [QUERY_KEY, "history", operation ?? "all"],
        queryFn: () => getBackupHistory(operation),
    });
}

export function useBackupConfiguration() {
    return useQuery({
        queryKey: [QUERY_KEY, "configuration"],
        queryFn: getBackupConfiguration,
    });
}

export function useCreateBackup() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: createBackup,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
        },
    });
}

export function useDeleteBackup() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: deleteBackup,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
        },
    });
}

/**
 * A successful restore replaces every account, transaction, fund, loan and
 * setting, so the whole cache is dropped rather than a few keys
 * invalidated — anything still held is from the database that was just
 * overwritten.
 */
function useRestoreMutation<TVariables>(
    mutationFn: (variables: TVariables) => Promise<unknown>,
) {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn,
        onSuccess: () => {
            queryClient.clear();
        },
    });
}

export function useRestoreBackup() {
    return useRestoreMutation(restoreBackup);
}

export function useRestoreFromUpload() {
    return useRestoreMutation(restoreFromUpload);
}

// ---------------------------------------------------------------------------
// Google Drive
// ---------------------------------------------------------------------------

export function useBeginGoogleDriveAuthorization() {
    return useMutation({
        mutationFn: beginGoogleDriveAuthorization,
    });
}

export function useCompleteGoogleDriveAuthorization() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: completeGoogleDriveAuthorization,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
        },
    });
}

export function useDisconnectGoogleDrive() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: disconnectGoogleDrive,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
        },
    });
}
