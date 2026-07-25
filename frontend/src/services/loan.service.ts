import api from "../api/api";
import type {
    CreateLoanRequest,
    Loan,
    RecordRepaymentRequest,
    UpdateLoanRequest,
} from "../types/loan";

const BASE_URL = "/loans";

export async function getLoans(activeOnly = false): Promise<Loan[]> {
    const response = await api.get<Loan[]>(BASE_URL, { params: { activeOnly } });
    return response.data;
}

export async function createLoan(request: CreateLoanRequest): Promise<Loan> {
    const response = await api.post<Loan>(BASE_URL, request);
    return response.data;
}

export interface UpdateLoanPayload extends UpdateLoanRequest {
    id: string;
}

export async function updateLoan({ id, ...request }: UpdateLoanPayload): Promise<Loan> {
    const response = await api.put<Loan>(`${BASE_URL}/${id}`, request);
    return response.data;
}

export interface RecordRepaymentPayload extends RecordRepaymentRequest {
    loanId: string;
}

export async function recordRepayment({ loanId, ...request }: RecordRepaymentPayload): Promise<Loan> {
    const response = await api.patch<Loan>(`${BASE_URL}/${loanId}/repay`, request);
    return response.data;
}

export async function closeLoan(id: string): Promise<Loan> {
    const response = await api.patch<Loan>(`${BASE_URL}/${id}/close`);
    return response.data;
}
