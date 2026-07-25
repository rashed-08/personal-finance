import api from "../api/api";
import type { CreateFundRequest, Fund, UpdateFundRequest } from "../types/fund";

const BASE_URL = "/funds";

export async function getFunds(activeOnly = false): Promise<Fund[]> {
    const response = await api.get<Fund[]>(BASE_URL, { params: { activeOnly } });
    return response.data;
}

export async function createFund(request: CreateFundRequest): Promise<Fund> {
    const response = await api.post<Fund>(BASE_URL, request);
    return response.data;
}

export interface UpdateFundPayload extends UpdateFundRequest {
    id: string;
}

export async function updateFund({ id, ...request }: UpdateFundPayload): Promise<Fund> {
    const response = await api.put<Fund>(`${BASE_URL}/${id}`, request);
    return response.data;
}

export async function activateFund(id: string): Promise<Fund> {
    const response = await api.patch<Fund>(`${BASE_URL}/${id}/activate`);
    return response.data;
}

export async function deactivateFund(id: string): Promise<Fund> {
    const response = await api.patch<Fund>(`${BASE_URL}/${id}/deactivate`);
    return response.data;
}
