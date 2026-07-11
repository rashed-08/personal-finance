import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
    createAccount,
    updateAccount,
    type CreateAccountPayload
} from "../services/account.service";


export function useCreateAccount() {

    const queryClient = useQueryClient();

    return useMutation({

        mutationFn: (
            data: CreateAccountPayload
        ) => createAccount(data),


        onSuccess: () => {

            queryClient.invalidateQueries({
                queryKey: ["accounts"],
            });

        },

    });
}

export function useUpdateAccount() {

    const queryClient = useQueryClient();

    return useMutation({

        mutationFn: updateAccount,

        onSuccess() {

            queryClient.invalidateQueries({
                queryKey: ["accounts"]
            });

        }

    });

}