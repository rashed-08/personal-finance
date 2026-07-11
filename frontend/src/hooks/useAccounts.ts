import {
    useMutation,
    useQuery,
    useQueryClient
} from "@tanstack/react-query";

import {
    getAccounts,
    createAccount,
    updateAccount
} from "../services/account.service";

import api from "../api/api";


export function useAccounts() {

    return useQuery({
        queryKey: ["accounts"],
        queryFn: getAccounts,
    });

}



export function useCreateAccount() {

    const queryClient = useQueryClient();


    return useMutation({

        mutationFn: createAccount,


        onSuccess: () => {

            queryClient.invalidateQueries({
                queryKey: ["accounts"],
            });

        },

    });

}



export function useDeactivateAccount() {

    const queryClient = useQueryClient();


    return useMutation({

        mutationFn: (id: string) =>
            api.patch(`/accounts/${id}/deactivate`),


        onSuccess: () => {

            queryClient.invalidateQueries({
                queryKey: ["accounts"],
            });

        },

    });

}



export function useDeleteAccount() {

    const queryClient = useQueryClient();


    return useMutation({

        mutationFn: (id: string) =>
            api.delete(`/accounts/${id}`),


        onSuccess: () => {

            queryClient.invalidateQueries({
                queryKey: ["accounts"],
            });

        },

    });

}

export function useUpdateAccount(){

    const queryClient=
        useQueryClient();

    return useMutation({

        mutationFn:updateAccount,

        onSuccess(){

            queryClient.invalidateQueries({

                queryKey:["accounts"]

            });

        }

    });

}