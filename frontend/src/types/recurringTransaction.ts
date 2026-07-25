export type Frequency = "DAILY" | "WEEKLY" | "MONTHLY" | "YEARLY";

export type RecurringExecutionStatus = "GENERATED" | "SKIPPED";

export interface RecurringTransaction {
    id: string;

    name: string;

    transactionType: "EXPENSE" | "INCOME" | "TRANSFER";

    fromAccountId: string | null;
    toAccountId: string | null;
    categoryId: string | null;

    amount: number;

    frequency: Frequency;

    startDate: string;
    endDate: string | null;

    nextExecutionDate: string;
    lastExecutionDate: string | null;

    autoGenerate: boolean;

    active: boolean;

    description: string | null;
    notes: string | null;

    createdAt: string;
    updatedAt: string;
}

export interface RecurringTransactionExecution {
    id: string;
    recurringTransactionId: string;
    scheduledDate: string;
    status: RecurringExecutionStatus;
    transactionId: string | null;
    reason: string | null;
    createdAt: string;
}

export interface CreateRecurringTransactionRequest {
    name: string;
    transactionType: "EXPENSE" | "INCOME" | "TRANSFER";
    fromAccountId?: string;
    toAccountId?: string;
    categoryId?: string;
    amount: number;
    frequency: Frequency;
    startDate: string;
    endDate?: string;
    autoGenerate: boolean;
    description?: string;
    notes?: string;
}

export interface UpdateRecurringTransactionRequest {
    name: string;
    amount: number;
    frequency: Frequency;
    endDate?: string;
    autoGenerate: boolean;
    description?: string;
    notes?: string;
}
