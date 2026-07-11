import { useState } from "react";

import type { Category } from "../../types/category";

import CategoryDialog from "./CategoryDialog";
import CategoryTypeBadge from "./CategoryTypeBadge";

import {

    useActivateCategory,
    useDeactivateCategory,
    useDeleteCategory,

} from "../../hooks/useCategories";

interface Props {

    categories: Category[];

}

export default function CategoryTable({

    categories,

}: Props) {

    const activateMutation =
        useActivateCategory();

    const deactivateMutation =
        useDeactivateCategory();

    const deleteMutation =
        useDeleteCategory();

    const [

        editing,

        setEditing,

    ] = useState<Category | undefined>();

    const [

        dialogOpen,

        setDialogOpen,

    ] = useState(false);

    function edit(category: Category) {

        setEditing(category);

        setDialogOpen(true);

    }

    function closeDialog() {

        setEditing(undefined);

        setDialogOpen(false);

    }

    return (

        <>

            <div className="card">

                <div className="table-wrap">

                    <table className="table">

                        <thead>

                        <tr>

                            <th>Name</th>

                            <th>Type</th>

                            <th>Status</th>

                            <th>Description</th>

                            <th></th>

                        </tr>

                        </thead>

                        <tbody>

                        {

                            categories.map(category => (

                                <tr key={category.id}>

                                    <td>

                                        <div className="cell-name">

                                            {category.name}

                                        </div>

                                    </td>

                                    <td>

                                        <CategoryTypeBadge
                                            type={
                                                category.categoryType
                                            }
                                        />

                                    </td>

                                    <td>

                                        {

                                            category.active

                                                ? (

                                                    <span className="pill pill--active">

                                                        <span className="pill__dot"/>

                                                        Active

                                                    </span>

                                                )

                                                : (

                                                    <span className="pill pill--inactive">

                                                        <span className="pill__dot"/>

                                                        Inactive

                                                    </span>

                                                )

                                        }

                                    </td>

                                    <td>

                                        {category.description}

                                    </td>

                                    <td>

                                        <div className="row-actions">

                                            <button

                                                className="btn btn--ghost btn--sm"

                                                onClick={() =>
                                                    edit(category)
                                                }

                                            >

                                                Edit

                                            </button>

                                            {

                                                category.active

                                                    ? (

                                                        <button

                                                            className="btn btn--ghost btn--sm"

                                                            onClick={() =>

                                                                deactivateMutation.mutate(

                                                                    category.id

                                                                )

                                                            }

                                                        >

                                                            Deactivate

                                                        </button>

                                                    )

                                                    : (

                                                        <button

                                                            className="btn btn--ghost btn--sm"

                                                            onClick={() =>

                                                                activateMutation.mutate(

                                                                    category.id

                                                                )

                                                            }

                                                        >

                                                            Activate

                                                        </button>

                                                    )

                                            }

                                            {

                                                !category.systemDefined && (

                                                    <button

                                                        className="btn btn--danger btn--sm"

                                                        onClick={() => {

                                                            if (

                                                                confirm(

                                                                    `Delete "${category.name}"?`

                                                                )

                                                            ) {

                                                                deleteMutation.mutate(

                                                                    category.id

                                                                );

                                                            }

                                                        }}

                                                    >

                                                        Delete

                                                    </button>

                                                )

                                            }

                                        </div>

                                    </td>

                                </tr>

                            ))

                        }

                        </tbody>

                    </table>

                </div>

            </div>

            <CategoryDialog

                open={dialogOpen}

                category={editing}

                onClose={closeDialog}

            />

        </>

    );

}