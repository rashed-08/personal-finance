import { useState } from "react";

import { useAccounts } from "../../hooks/useAccounts";

import type { Account } from "../../types/account";

import AccountTable from "../../components/accounts/AccountTable";
import AccountDialog from "../../components/accounts/AccountDialog";
import AccountForm from "../../components/accounts/AccountForm";

export default function AccountsPage() {

    const { data = [], isLoading, error } = useAccounts();

    const [open, setOpen] = useState(false);

    const [selectedAccount, setSelectedAccount] =
        useState<Account | undefined>();


    function createNew() {

        setSelectedAccount(undefined);
        setOpen(true);

    }


    function editAccount(account: Account) {

        setSelectedAccount(account);
        setOpen(true);

    }


    function closeDialog() {

        setOpen(false);
        setSelectedAccount(undefined);

    }


    if (isLoading) {

        return <div>Loading...</div>;

    }

    if (error) {

        return <div>Something went wrong.</div>;

    }

    return (

        <>

            <div
                style={{
                    display: "flex",
                    justifyContent: "space-between",
                    marginBottom: 20
                }}
            >

                <h2>Accounts</h2>

                <button
                    onClick={createNew}
                >
                    New Account
                </button>

            </div>


            <AccountTable

                accounts={data}

                onEdit={editAccount}

            />


            <AccountDialog

                open={open}

                title={
                    selectedAccount
                        ? "Edit Account"
                        : "New Account"
                }

                onClose={closeDialog}

            >

                <AccountForm

                    account={selectedAccount}

                    onSuccess={closeDialog}

                />

            </AccountDialog>

        </>

    );

}