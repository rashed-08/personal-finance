export interface SalaryCycle {
    id: string;

    name: string;

    startDate: string;

    endDate: string;

    salaryDate: string;

    closed: boolean;

    description: string | null;

    createdAt: string;

    updatedAt: string;
}

export interface CreateSalaryCycleRequest {
    name: string;

    startDate: string;

    endDate: string;

    salaryDate: string;

    description?: string;
}
