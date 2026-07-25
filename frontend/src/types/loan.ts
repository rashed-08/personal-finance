export type LoanType = "RECEIVABLE" | "PAYABLE";

export type LoanStatus = "ACTIVE" | "CLOSED" | "CANCELLED";

export interface Loan {
    id: string;

    name: string;

    loanType: LoanType;

    principalAmount: number;

    startDate: string;

    dueDate: string | null;

    /** Derived from posted transactions — never stored. */
    outstandingBalance: number;

    loanStatus: LoanStatus;

    description: string | null;

    createdAt: string;

    updatedAt: string;
}

export interface CreateLoanRequest {
    name: string;
    loanType: LoanType;
    principalAmount: number;
    startDate: string;
    dueDate?: string;
    accountId: string;
    salaryCycleId: string;
    description?: string;
}

export interface UpdateLoanRequest {
    name: string;
    dueDate?: string;
    description?: string;
}

export interface RecordRepaymentRequest {
    accountId: string;
    amount: number;
    paymentDate: string;
    salaryCycleId: string;
    description?: string;
}
