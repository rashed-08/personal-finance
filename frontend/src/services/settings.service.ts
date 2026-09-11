import api from "../api/api";
import type { Setting, UpdateSettingsRequest } from "../types/settings";

const BASE_URL = "/settings";

export async function getSettings(): Promise<Setting[]> {
    const response = await api.get<Setting[]>(BASE_URL);
    return response.data;
}

/** Applies a batch of changes; returns only the settings that changed. */
export async function updateSettings(request: UpdateSettingsRequest): Promise<Setting[]> {
    const response = await api.put<Setting[]>(BASE_URL, request);
    return response.data;
}
