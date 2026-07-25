import api from "../api/api";
import type {
    CreateRecurringTransactionRequest,
    RecurringTransaction,
    RecurringTransactionExecution,
    UpdateRecurringTransactionRequest,
} from "../types/recurringTransaction";

const BASE_URL = "/recurring-transactions";

export async function getRecurringTransactions(activeOnly = false): Promise<RecurringTransaction[]> {
    const response = await api.get<RecurringTransaction[]>(BASE_URL, { params: { activeOnly } });
    return response.data;
}

export async function getDueRecurringTransactions(): Promise<RecurringTransaction[]> {
    const response = await api.get<RecurringTransaction[]>(`${BASE_URL}/due`);
    return response.data;
}

export async function getRecurringTransactionExecutions(id: string): Promise<RecurringTransactionExecution[]> {
    const response = await api.get<RecurringTransactionExecution[]>(`${BASE_URL}/${id}/executions`);
    return response.data;
}

export async function createRecurringTransaction(
    request: CreateRecurringTransactionRequest,
): Promise<RecurringTransaction> {
    const response = await api.post<RecurringTransaction>(BASE_URL, request);
    return response.data;
}

export interface UpdateRecurringTransactionPayload extends UpdateRecurringTransactionRequest {
    id: string;
}

export async function updateRecurringTransaction({
    id,
    ...request
}: UpdateRecurringTransactionPayload): Promise<RecurringTransaction> {
    const response = await api.put<RecurringTransaction>(`${BASE_URL}/${id}`, request);
    return response.data;
}

export async function activateRecurringTransaction(id: string): Promise<RecurringTransaction> {
    const response = await api.patch<RecurringTransaction>(`${BASE_URL}/${id}/activate`);
    return response.data;
}

export async function deactivateRecurringTransaction(id: string): Promise<RecurringTransaction> {
    const response = await api.patch<RecurringTransaction>(`${BASE_URL}/${id}/deactivate`);
    return response.data;
}

export async function generateRecurringTransactionNow(id: string): Promise<RecurringTransaction> {
    const response = await api.post<RecurringTransaction>(`${BASE_URL}/${id}/generate-now`);
    return response.data;
}

export async function runDueRecurringTransactions(): Promise<RecurringTransaction[]> {
    const response = await api.post<RecurringTransaction[]>(`${BASE_URL}/run-due`);
    return response.data;
}

export async function deleteRecurringTransaction(id: string): Promise<void> {
    await api.delete(`${BASE_URL}/${id}`);
}
