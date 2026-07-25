import api from "../api/api";
import type { CreateSalaryCycleRequest, SalaryCycle } from "../types/salaryCycle";

const BASE_URL = "/salary-cycles";

export async function getSalaryCycles(): Promise<SalaryCycle[]> {
    const response = await api.get<SalaryCycle[]>(BASE_URL);
    return response.data;
}

export async function createSalaryCycle(
    request: CreateSalaryCycleRequest,
): Promise<SalaryCycle> {
    const response = await api.post<SalaryCycle>(BASE_URL, request);
    return response.data;
}
