import api from "../api/api";
import type {
    Backup,
    BackupConfiguration,
    BackupOperation,
    CreateBackupRequest,
    GoogleDriveConnection,
} from "../types/backup";

const BASE_URL = "/backups";

export async function getBackupHistory(operation?: BackupOperation): Promise<Backup[]> {
    const response = await api.get<Backup[]>(BASE_URL, {
        params: operation ? { operation } : undefined,
    });
    return response.data;
}

export async function getBackupConfiguration(): Promise<BackupConfiguration> {
    const response = await api.get<BackupConfiguration>(`${BASE_URL}/configuration`);
    return response.data;
}

export async function createBackup(request: CreateBackupRequest = {}): Promise<Backup> {
    const response = await api.post<Backup>(BASE_URL, request);
    return response.data;
}

export async function deleteBackup(id: string): Promise<void> {
    await api.delete(`${BASE_URL}/${id}`);
}

export interface RestorePayload {
    id: string;
    confirmation: string;
}

export async function restoreBackup({ id, confirmation }: RestorePayload): Promise<Backup> {
    const response = await api.post<Backup>(`${BASE_URL}/${id}/restore`, { confirmation });
    return response.data;
}

export interface RestoreUploadPayload {
    file: File;
    confirmation: string;
}

export async function restoreFromUpload({
    file,
    confirmation,
}: RestoreUploadPayload): Promise<Backup> {
    const form = new FormData();
    form.append("file", file);
    form.append("confirmation", confirmation);

    const response = await api.post<Backup>(`${BASE_URL}/restore`, form, {
        // Let the browser set the multipart boundary; the axios instance
        // defaults every request to application/json.
        headers: { "Content-Type": undefined },
        // A restore reads, wipes and reloads the whole ledger, which can
        // outlast the client's 10s default.
        timeout: 0,
    });

    return response.data;
}

/**
 * Downloads an archive and hands it to the browser as a file.
 *
 * Fetched through axios rather than a plain link so the Authorization
 * header is attached — the endpoint is authenticated, and a bare anchor
 * would get a 401.
 */
export async function downloadBackup(id: string, fileName: string): Promise<void> {
    const response = await api.get<Blob>(`${BASE_URL}/${id}/download`, {
        responseType: "blob",
        timeout: 0,
    });

    const url = URL.createObjectURL(response.data);

    try {
        const link = document.createElement("a");
        link.href = url;
        link.download = fileName;
        link.click();
    } finally {
        // Revoking immediately is safe: click() has already started the
        // download, and holding the object URL leaks the whole archive.
        URL.revokeObjectURL(url);
    }
}

// ---------------------------------------------------------------------------
// Google Drive
// ---------------------------------------------------------------------------

export async function getGoogleDriveStatus(): Promise<GoogleDriveConnection> {
    const response = await api.get<GoogleDriveConnection>(`${BASE_URL}/google-drive`);
    return response.data;
}

/** Returns the Google consent URL for the browser to navigate to. */
export async function beginGoogleDriveAuthorization(): Promise<string> {
    const response = await api.post<{ authorizationUrl: string }>(
        `${BASE_URL}/google-drive/authorize`,
    );
    return response.data.authorizationUrl;
}

export interface GoogleDriveCallbackPayload {
    code: string;
    state: string;
}

export async function completeGoogleDriveAuthorization(
    payload: GoogleDriveCallbackPayload,
): Promise<GoogleDriveConnection> {
    const response = await api.post<GoogleDriveConnection>(
        `${BASE_URL}/google-drive/callback`,
        null,
        { params: payload },
    );
    return response.data;
}

export async function disconnectGoogleDrive(): Promise<void> {
    await api.delete(`${BASE_URL}/google-drive`);
}
