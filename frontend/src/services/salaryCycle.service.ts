import api from "../api/api";
import type {
    CarryForward,
    CreateSalaryCycleRequest,
    SalaryCycle,
    UpdateSalaryCycleRequest,
} from "../types/salaryCycle";

const BASE_URL = "/salary-cycles";

export async function getSalaryCycles(): Promise<SalaryCycle[]> {
    const response = await api.get<SalaryCycle[]>(BASE_URL);
    return response.data;
}

export async function getCurrentSalaryCycle(): Promise<SalaryCycle> {
    const response = await api.get<SalaryCycle>(`${BASE_URL}/current`);
    return response.data;
}

export async function createSalaryCycle(
    request: CreateSalaryCycleRequest,
): Promise<SalaryCycle> {
    const response = await api.post<SalaryCycle>(BASE_URL, request);
    return response.data;
}

export async function updateSalaryCycle(
    id: string,
    request: UpdateSalaryCycleRequest,
): Promise<SalaryCycle> {
    const response = await api.put<SalaryCycle>(`${BASE_URL}/${id}`, request);
    return response.data;
}

export async function closeSalaryCycle(
    id: string,
    endDate: string,
): Promise<SalaryCycle> {
    const response = await api.patch<SalaryCycle>(`${BASE_URL}/${id}/close`, { endDate });
    return response.data;
}

export async function reopenSalaryCycle(id: string): Promise<SalaryCycle> {
    const response = await api.patch<SalaryCycle>(`${BASE_URL}/${id}/reopen`);
    return response.data;
}

export async function getCarryForward(id: string): Promise<CarryForward> {
    const response = await api.get<CarryForward>(`${BASE_URL}/${id}/carry-forward`);
    return response.data;
}
