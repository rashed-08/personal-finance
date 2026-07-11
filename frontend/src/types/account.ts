export type AccountType =
    | "CASH"
    | "BANK"
    | "MOBILE_BANKING"
    | "CREDIT_CARD"
    | "SAVINGS"
    | "INVESTMENT"
    | "E_WALLET";

export interface Account {
    id: string;

    name: string;

    accountType: AccountType;

    openingBalance: number;

    active: boolean;

    description: string | null;

    createdAt: string;

    updatedAt: string;
}