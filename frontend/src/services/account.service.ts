import api from "../api/api";
import type { Account } from "../types/account";


export interface CreateAccountPayload {
    name: string;
    accountType: string;
    openingBalance: number;
    description?: string;
}


export async function getAccounts(): Promise<Account[]> {

    const response = await api.get<Account[]>("/accounts");

    return response.data;
}


export async function createAccount(
    payload: CreateAccountPayload
): Promise<Account> {

    const response = await api.post<Account>(
        "/accounts",
        payload
    );

    return response.data;
}

export interface UpdateAccountPayload{

    id:string;

    name:string;

    accountType:string;

    openingBalance:number;

    description?:string;

}

export async function updateAccount({

    id,

    ...payload

}:UpdateAccountPayload){

    const response=
        await api.put(

            `/accounts/${id}`,

            payload

        );

    return response.data;

}