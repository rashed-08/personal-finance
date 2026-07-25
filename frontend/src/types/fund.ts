export type FundType =
    | "EMERGENCY"
    | "SAVINGS"
    | "GOAL"
    | "ZAKAT"
    | "INVESTMENT"
    | "CUSTOM";

export interface Fund {
    id: string;

    name: string;

    fundType: FundType;

    targetAmount: number | null;

    targetDate: string | null;

    /** Derived from posted transactions — never stored. */
    balance: number;

    active: boolean;

    description: string | null;

    createdAt: string;

    updatedAt: string;
}

export interface CreateFundRequest {
    name: string;
    fundType: FundType;
    targetAmount?: number;
    targetDate?: string;
    description?: string;
}

export interface UpdateFundRequest {
    name: string;
    targetAmount?: number;
    targetDate?: string;
    description?: string;
}
