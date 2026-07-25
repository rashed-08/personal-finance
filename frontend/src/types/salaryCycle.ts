export interface SalaryCycle {
    id: string;

    name: string;

    startDate: string;

    /** Null while the cycle is still open (ongoing). */
    endDate: string | null;

    salaryDate: string;

    closed: boolean;

    description: string | null;

    createdAt: string;

    updatedAt: string;
}

export interface CreateSalaryCycleRequest {
    name: string;

    startDate: string;

    /** Omit to create an open, ongoing cycle. */
    endDate?: string;

    salaryDate: string;

    description?: string;
}

export interface UpdateSalaryCycleRequest {
    name: string;

    salaryDate: string;

    description?: string;
}

export interface CarryForward {
    salaryCycleId: string;

    openingBalance: number;

    income: number;

    expenses: number;

    adjustments: number;

    closingBalance: number;
}
