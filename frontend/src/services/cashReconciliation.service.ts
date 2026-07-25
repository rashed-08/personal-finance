import api from "../api/api";
import type {
    CashReconciliation,
    RecordCashSnapshotRequest,
    StartReconciliationRequest,
} from "../types/cashReconciliation";

const BASE_URL = "/cash-reconciliations";

export async function getReconciliations(accountId?: string): Promise<CashReconciliation[]> {
    const response = await api.get<CashReconciliation[]>(BASE_URL, {
        params: accountId ? { accountId } : undefined,
    });
    return response.data;
}

export async function getReconciliation(id: string): Promise<CashReconciliation> {
    const response = await api.get<CashReconciliation>(`${BASE_URL}/${id}`);
    return response.data;
}

export async function startReconciliation(
    request: StartReconciliationRequest,
): Promise<CashReconciliation> {
    const response = await api.post<CashReconciliation>(BASE_URL, request);
    return response.data;
}

export async function recordCashSnapshot(
    id: string,
    request: RecordCashSnapshotRequest,
): Promise<CashReconciliation> {
    const response = await api.post<CashReconciliation>(`${BASE_URL}/${id}/snapshots`, request);
    return response.data;
}

export async function completeReconciliation(id: string): Promise<CashReconciliation> {
    const response = await api.patch<CashReconciliation>(`${BASE_URL}/${id}/complete`);
    return response.data;
}
