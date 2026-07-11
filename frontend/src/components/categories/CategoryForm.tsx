import {
    useEffect,
    useState,
} from "react";

import type {
    FormEvent,
} from "react";

import type {
    Category,
} from "../../types/category";

import {
    CATEGORY_TYPES,
} from "../../lib/categoryTypes";

import {
    useCreateCategory,
    useUpdateCategory,
} from "../../hooks/useCategories";

interface Props {

    category?: Category;

    onSuccess: () => void;

}

function errorMessage(
    err: unknown
): string {

    const detail = (
        err as {
            response?: {
                data?: {
                    detail?: string;
                };
            };
        }
    )?.response?.data?.detail;

    return detail ??
        "Something went wrong. Please try again.";

}

export default function CategoryForm({

    category,

    onSuccess,

}: Props) {

    const createMutation =
        useCreateCategory();

    const updateMutation =
        useUpdateCategory();

    const [name, setName] =
        useState("");

    const [type, setType] =
        useState("EXPENSE");

    const [description, setDescription] =
        useState("");

    const [nameError, setNameError] =
        useState<string | null>(null);

    useEffect(() => {

        if (!category) {

            setName("");
            setType("EXPENSE");
            setDescription("");
            setNameError(null);

            return;

        }

        setName(category.name);
        setType(category.categoryType);
        setDescription(category.description ?? "");
        setNameError(null);

    }, [category]);

    const isEditing =
        Boolean(category);

    const mutation =
        isEditing
            ? updateMutation
            : createMutation;

    const isPending =
        mutation.isPending;

    function submit(
        e: FormEvent
    ) {

        e.preventDefault();

        if (!name.trim()) {

            setNameError(
                "Category name is required."
            );

            return;

        }

        setNameError(null);

        if (category) {

            updateMutation.mutate(

                {

                    id: category.id,

                    name: name.trim(),

                    categoryType: type as any,

                    description: description.trim(),

                },

                {

                    onSuccess,

                }

            );

        } else {

            createMutation.mutate(

                {

                    name: name.trim(),

                    categoryType: type as any,

                    systemDefined: false,

                    description: description.trim(),

                },

                {

                    onSuccess,

                }

            );

        }

    }

    return (

        <form
            className="form"
            onSubmit={submit}
            noValidate
        >

            {

                mutation.isError && (

                    <div
                        className="form-error"
                        role="alert"
                    >

                        <span>⚠</span>

                        <span>

                            {

                                errorMessage(
                                    mutation.error
                                )

                            }

                        </span>

                    </div>

                )

            }

            <div className="field">

                <label
                    className="field__label"
                    htmlFor="category-name"
                >

                    Name

                    <span className="field__req">
                        *
                    </span>

                </label>

                <input
                    id="category-name"
                    className={
                        nameError
                            ? "input input--invalid"
                            : "input"
                    }
                    value={name}
                    onChange={e =>
                        setName(
                            e.target.value
                        )
                    }
                    maxLength={100}
                    autoFocus
                />

                {

                    nameError && (

                        <span className="field__error">

                            {nameError}

                        </span>

                    )

                }

            </div>

            <div className="field">

                <label
                    className="field__label"
                    htmlFor="category-type"
                >

                    Type

                    <span className="field__req">
                        *
                    </span>

                </label>

                <select
                    id="category-type"
                    className="select"
                    value={type}
                    onChange={e =>
                        setType(
                            e.target.value
                        )
                    }
                    disabled={isEditing}
                >

                    {

                        CATEGORY_TYPES.map(
                            option => (

                                <option
                                    key={option.value}
                                    value={option.value}
                                >

                                    {option.label}

                                </option>

                            )
                        )

                    }

                </select>

            </div>

            <div className="field">

                <label
                    className="field__label"
                    htmlFor="category-description"
                >

                    Description

                </label>

                <textarea
                    id="category-description"
                    className="textarea"
                    value={description}
                    onChange={e =>
                        setDescription(
                            e.target.value
                        )
                    }
                    maxLength={500}
                />

            </div>

            <div className="form-actions">

                <button
                    type="submit"
                    className="btn btn--primary"
                    disabled={isPending}
                >

                    {

                        isPending

                            ? "Saving..."

                            : isEditing

                                ? "Update Category"

                                : "Create Category"

                    }

                </button>

            </div>

        </form>

    );

}