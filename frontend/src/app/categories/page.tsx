import { useState } from "react";

import CategoryDialog from "../../components/categories/CategoryDialog";
import CategoryTable from "../../components/categories/CategoryTable";

import { useCategories } from "../../hooks/useCategories";

export default function CategoriesPage() {

    const {

        data: categories,

        isLoading,

        isError,

    } = useCategories();

    const [

        dialogOpen,

        setDialogOpen,

    ] = useState(false);

    if (isLoading) {

        return (

            <div className="state">

                <div className="spinner" />

                <div className="state__title">

                    Loading categories...

                </div>

            </div>

        );

    }

    if (isError) {

        return (

            <div className="state">

                <div className="state__icon">

                    ⚠

                </div>

                <div className="state__title">

                    Failed to load categories

                </div>

            </div>

        );

    }

    return (

        <>

            <div className="page-header">

                <div>

                    <h1 className="page-header__title">

                        Categories

                    </h1>

                    <p className="page-header__subtitle">

                        Manage income and expense categories.

                    </p>

                </div>

                <button

                    className="btn btn--primary"

                    onClick={() =>
                        setDialogOpen(true)
                    }

                >

                    + New Category

                </button>

            </div>

            {

                categories &&
                categories.length > 0

                    ? (

                        <CategoryTable
                            categories={categories}
                        />

                    )

                    : (

                        <div className="state">

                            <div className="state__icon">

                                📂

                            </div>

                            <div className="state__title">

                                No categories yet

                            </div>

                            <div className="state__desc">

                                Create your first category.

                            </div>

                        </div>

                    )

            }

            <CategoryDialog

                open={dialogOpen}

                onClose={() =>
                    setDialogOpen(false)
                }

            />

        </>

    );

}