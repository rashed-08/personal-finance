import api from "../api/api";
import type { GoogleKeepMigrationResult } from "../types/migration";

const BASE_URL = "/migrations";

export async function importGoogleKeep(content: string): Promise<GoogleKeepMigrationResult> {
    const response = await api.post<GoogleKeepMigrationResult>(`${BASE_URL}/google-keep`, { content });
    return response.data;
}
