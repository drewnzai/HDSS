import { useState } from "react";
import type {
    ChangeEvent,
    FormEvent,
} from "react";
import { useNavigate } from "react-router-dom";

import "./create-form.css";
import FormPage from "../../../../components/form-page/FormPage";
import { useCreateFormMutation } from "../../../../redux/FormApi";
import type { FormCategory, FormTarget } from "../../../../models/types/FormTypes";

interface FormState {
    name: string;
    title: string;
    category: FormCategory;
    target: FormTarget;
    description: string;
}

const initialForm: FormState = {
    name: "",
    title: "",
    category: "CORE",
    target: "HOUSEHOLD",
    description: "",
};

const CATEGORY_OPTIONS: {
    value: FormCategory;
    label: string;
}[] = [
        {
            value: "CORE",
            label: "Core",
        },
        {
            value: "EXTRA",
            label: "Extra",
        },
    ];

const TARGET_OPTIONS: {
    value: FormTarget;
    label: string;
}[] = [
        {
            value: "HOUSEHOLD",
            label: "Household",
        },
        {
            value: "INDIVIDUAL",
            label: "Individual",
        },
    ];

function CreateForm() {
    const navigate = useNavigate();

    const [form, setForm] = useState<FormState>(initialForm);
    const [validationError, setValidationError] = useState<string | null>(
        null
    );

    const [
        createForm,
        {
            isLoading,
            error,
        },
    ] = useCreateFormMutation();

    const handleChange = (
        event: ChangeEvent<
            HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
        >
    ) => {
        const { name, value } = event.target;

        setForm((current) => ({
            ...current,
            [name]: value,
        }));

        if (validationError) {
            setValidationError(null);
        }
    };

    const handleSubmit = async (
        event: FormEvent<HTMLFormElement>
    ) => {
        event.preventDefault();

        const name = form.name.trim();
        const title = form.title.trim();
        const description = form.description.trim();

        if (!name) {
            setValidationError("Name is required.");
            return;
        }

        if (!title) {
            setValidationError("Title is required.");
            return;
        }

        try {
            const message = await createForm({
                name,
                title,
                category: form.category,
                target: form.target,
                description,
            }).unwrap();

            navigate("/admin/forms", {
                state: {
                    flash: message ?? "Form created successfully.",
                    flashType: "success"
                },
            });
        } catch {
            // API error is exposed through `error`.
        }
    };

    return (
        <FormPage
            eyebrow="§ Admin — Form Management"
            title="Create form"
            description="Define a form that can be used to collect structured field data."
            sections={[
                {
                    eyebrow: "Definition",
                    title: "Form details",
                    children: (
                        <div className="form-fields">
                            <div className="form-field">
                                <label
                                    className="form-field__label"
                                    htmlFor="name"
                                >
                                    Name
                                </label>

                                <input
                                    id="name"
                                    name="name"
                                    type="text"
                                    className="form-field__input"
                                    value={form.name}
                                    onChange={handleChange}
                                    placeholder="e.g. household-registration"
                                    autoComplete="off"
                                    disabled={isLoading}
                                />

                                <p className="form-field__hint">
                                    The internal name used to identify the
                                    form.
                                </p>
                            </div>

                            <div className="form-field">
                                <label
                                    className="form-field__label"
                                    htmlFor="title"
                                >
                                    Title
                                </label>

                                <input
                                    id="title"
                                    name="title"
                                    type="text"
                                    className="form-field__input"
                                    value={form.title}
                                    onChange={handleChange}
                                    placeholder="e.g. Household Registration"
                                    disabled={isLoading}
                                />

                                <p className="form-field__hint">
                                    The title displayed to users.
                                </p>
                            </div>

                            <div className="form-field">
                                <label
                                    className="form-field__label"
                                    htmlFor="category"
                                >
                                    Category
                                </label>

                                <select
                                    id="category"
                                    name="category"
                                    className="form-field__input"
                                    value={form.category}
                                    onChange={handleChange}
                                    disabled={isLoading}
                                >
                                    {CATEGORY_OPTIONS.map((option) => (
                                        <option
                                            key={option.value}
                                            value={option.value}
                                        >
                                            {option.label}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className="form-field">
                                <label
                                    className="form-field__label"
                                    htmlFor="target"
                                >
                                    Target
                                </label>

                                <select
                                    id="target"
                                    name="target"
                                    className="form-field__input"
                                    value={form.target}
                                    onChange={handleChange}
                                    disabled={isLoading}
                                >
                                    {TARGET_OPTIONS.map((option) => (
                                        <option
                                            key={option.value}
                                            value={option.value}
                                        >
                                            {option.label}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className="form-field form-field--full">
                                <label
                                    className="form-field__label"
                                    htmlFor="description"
                                >
                                    Description
                                </label>

                                <textarea
                                    id="description"
                                    name="description"
                                    className="form-field__input form-field__textarea"
                                    value={form.description}
                                    onChange={handleChange}
                                    placeholder="Describe what this form is used for..."
                                    rows={5}
                                    disabled={isLoading}
                                />
                            </div>
                        </div>
                    ),
                },
            ]}
            error={
                validationError ??
                (error
                    ? "Unable to create the form. Please check the details and try again."
                    : undefined)
            }
            isLoading={isLoading}
            submitLabel="Create form"
            loadingLabel="Creating…"
            cancelLabel="Cancel"
            onCancel={() => navigate("/admin/forms")}
            onSubmit={handleSubmit}
        />
    );
}

export default CreateForm;