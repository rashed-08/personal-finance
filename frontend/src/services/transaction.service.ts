import api from "../api/api";
import type {
    CreateTransactionRequest,
    Page,
    Transaction,
    TransactionFilter,
    UpdateTransactionRequest,
} from "../types/transaction";

const BASE_URL = "/transactions";

export interface ListTransactionsParams extends TransactionFilter {
    page?: number;
    size?: number;
}

export async function getTransactions(
    params: ListTransactionsParams,
): Promise<Page<Transaction>> {
    const response = await api.get<Page<Transaction>>(BASE_URL, { params });
    return response.data;
}

export async function getTransaction(id: string): Promise<Transaction> {
    const response = await api.get<Transaction>(`${BASE_URL}/${id}`);
    return response.data;
}

export async function createTransaction(
    request: CreateTransactionRequest,
): Promise<Transaction> {
    const response = await api.post<Transaction>(BASE_URL, request);
    return response.data;
}

export async function updateTransaction(
    id: string,
    request: UpdateTransactionRequest,
): Promise<Transaction> {
    const response = await api.put<Transaction>(`${BASE_URL}/${id}`, request);
    return response.data;
}

export async function voidTransaction(id: string): Promise<Transaction> {
    const response = await api.patch<Transaction>(`${BASE_URL}/${id}/void`);
    return response.data;
}

export async function reverseTransaction(id: string): Promise<Transaction> {
    const response = await api.patch<Transaction>(`${BASE_URL}/${id}/reverse`);
    return response.data;
}
