import {
    useEffect,
    useState
} from "react";

import type { Account } from "../../types/account";

import {
    useCreateAccount,
    useUpdateAccount
} from "../../hooks/useAccounts";

interface Props {

    account?: Account;

    onSuccess: () => void;

}

export default function AccountForm({

    account,
    onSuccess

}: Props) {

    const createMutation =
        useCreateAccount();

    const updateMutation =
        useUpdateAccount();

    const [name,setName] =
        useState("");

    const [type,setType] =
        useState("CASH");

    const [balance,setBalance] =
        useState(0);

    const [description,setDescription] =
        useState("");

    useEffect(()=>{

        if(!account){

            setName("");
            setType("CASH");
            setBalance(0);
            setDescription("");

            return;
        }

        setName(account.name);
        setType(account.accountType);
        setBalance(account.openingBalance);
        setDescription(account.description ?? "");

    },[account]);

    function submit(
        e:React.FormEvent
    ){

        e.preventDefault();

        const payload={

            name,

            accountType:type,

            openingBalance:balance,

            description

        };

        if(account){

            updateMutation.mutate({

                id:account.id,

                ...payload

            },{

                onSuccess

            });

        }else{

            createMutation.mutate(

                payload,

                {

                    onSuccess

                }

            );

        }

    }

    return(

        <form onSubmit={submit}>

            <input
                value={name}
                onChange={e=>setName(e.target.value)}
                placeholder="Name"
            />

            <br/><br/>

            <select
                value={type}
                onChange={e=>setType(e.target.value)}
            >

                <option value="CASH">Cash</option>
                <option value="BANK">Bank</option>
                <option value="MOBILE_BANKING">
                    Mobile Banking
                </option>

            </select>

            <br/><br/>

            <input
                type="number"
                value={balance}
                onChange={
                    e=>
                        setBalance(
                            Number(e.target.value)
                        )
                }
            />

            <br/><br/>

            <textarea
                value={description}
                onChange={
                    e=>
                        setDescription(
                            e.target.value
                        )
                }
            />

            <br/><br/>

            <button
                type="submit"
            >

                {
                    account
                    ? "Update Account"
                    : "Create Account"
                }

            </button>

        </form>

    );

}