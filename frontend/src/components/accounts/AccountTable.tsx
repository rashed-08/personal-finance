import type { Account } from "../../types/account";

import {

    useDeleteAccount,

    useDeactivateAccount

} from "../../hooks/useAccounts";


interface Props {

    accounts: Account[];

    onEdit(account: Account): void;

}

export default function AccountTable({

    accounts,

    onEdit

}: Props) {

    const deactivate =
        useDeactivateAccount();

    const remove =
        useDeleteAccount();

    return (

        <table
            border={1}
            cellPadding={8}
            width="100%"
        >

            <thead>

            <tr>

                <th>Name</th>

                <th>Type</th>

                <th>Balance</th>

                <th>Status</th>

                <th>Actions</th>

            </tr>

            </thead>

            <tbody>

            {

                accounts.map(account => (

                    <tr
                        key={account.id}
                    >

                        <td>

                            {account.name}

                        </td>

                        <td>

                            {account.accountType}

                        </td>

                        <td>

                            {account.openingBalance}

                        </td>

                        <td>

                            {

                                account.active

                                    ? "Active"

                                    : "Inactive"

                            }

                        </td>

                        <td>

                            <button
                                onClick={() =>
                                    onEdit(account)
                                }
                            >
                                Edit
                            </button>

                            {" "}

                            <button

                                disabled={!account.active}

                                onClick={() =>
                                    deactivate.mutate(account.id)
                                }

                            >
                                Deactivate
                            </button>

                            {" "}

                            <button
                                onClick={() =>
                                    remove.mutate(account.id)
                                }
                            >
                                Delete
                            </button>

                        </td>

                    </tr>

                ))

            }

            </tbody>

        </table>

    );

}