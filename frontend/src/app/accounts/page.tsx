import { useState } from "react";

import { useAccounts } from "../../hooks/useAccounts";
import type { Account } from "../../types/account";

import AccountTable from "../../components/accounts/AccountTable";
import AccountDialog from "../../components/accounts/AccountDialog";
import AccountForm from "../../components/accounts/AccountForm";

export default function AccountsPage() {
    const { data = [], isLoading, error } = useAccounts();

    const [open, setOpen] = useState(false);
    const [selectedAccount, setSelectedAccount] = useState<Account | undefined>();

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

    return (
        <>
            <div className="page-header">
                <div>
                    <h1 className="page-header__title">Accounts</h1>
                    <p className="page-header__subtitle">
                        Your cash, bank, and mobile-banking accounts. Balances are
                        derived from transactions.
                    </p>
                </div>

                <button
                    type="button"
                    className="btn btn--primary"
                    onClick={createNew}
                >
                    + New Account
                </button>
            </div>

            <div className="card">
                {isLoading ? (
                    <div className="state">
                        <div className="spinner" />
                        <div className="state__desc">Loading accounts…</div>
                    </div>
                ) : error ? (
                    <div className="state">
                        <div className="state__icon">⚠</div>
                        <div className="state__title">Couldn’t load accounts</div>
                        <div className="state__desc">
                            Check that the backend is running on
                            <code> localhost:8080</code> and try again.
                        </div>
                    </div>
                ) : data.length === 0 ? (
                    <div className="state">
                        <div className="state__icon">🏦</div>
                        <div className="state__title">No accounts yet</div>
                        <div className="state__desc">
                            Create your first account to start tracking where your
                            money lives.
                        </div>
                        <button
                            type="button"
                            className="btn btn--primary"
                            onClick={createNew}
                        >
                            + New Account
                        </button>
                    </div>
                ) : (
                    <AccountTable accounts={data} onEdit={editAccount} />
                )}
            </div>

            <AccountDialog
                open={open}
                title={selectedAccount ? "Edit Account" : "New Account"}
                onClose={closeDialog}
            >
                <AccountForm account={selectedAccount} onSuccess={closeDialog} />
            </AccountDialog>
        </>
    );
}
