import { useState, type ChangeEvent, type FormEvent } from "react";
import { useNavigate, useParams } from "react-router-dom";
import FormPage from "../../../../components/form-page/FormPage";
import type { CreateChoiceRequest } from "../../../../models/CreateChoiceRequest";
import { useCreateChoiceMutation } from "../../../../redux/ChoiceApi";
import "./create-choice.css";

interface ChoiceFormState {
    name: string;
    label: string;
    orderIndex: string;
}

const initialForm: ChoiceFormState = {
    name: "",
    label: "",
    orderIndex: "",
};

function CreateChoice() {
    const navigate = useNavigate();
    const { listName } = useParams<{ listName: string }>();
    const decodedListName = listName ? decodeURIComponent(listName) : "";

    const [form, setForm] = useState<ChoiceFormState>(initialForm);
    const [validationError, setValidationError] = useState<string | null>(
        null
    );

    const [
        createChoice,
        { isLoading, error },
    ] = useCreateChoiceMutation();

    const handleChange = (
        event: ChangeEvent<HTMLInputElement>
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

    const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();

        const name = form.name.trim();
        const label = form.label.trim();
        const orderIndex = form.orderIndex.trim();

        if (!name) {
            setValidationError("Stored value is required.");
            return;
        }

        if (!label) {
            setValidationError("Label is required.");
            return;
        }

        if (!orderIndex || Number.isNaN(Number(orderIndex))) {
            setValidationError("Order must be a number.");
            return;
        }

        const body: CreateChoiceRequest = {
            listName: decodedListName,
            name,
            label,
            orderIndex: Number(orderIndex),
        };

        try {
            await createChoice({ body }).unwrap();

            navigate(`/admin/choices/${listName}`, {
                state: {
                    flash: `Choice "${label}" added to "${decodedListName}."`,
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
            title={`Add choice to "${decodedListName}"`}
            description="Define one option within this shared choice list."
            sections={[
                {
                    eyebrow: "Definition",
                    title: "Choice details",
                    children: (
                        <div className="form-fields">
                            <div className="form-field">
                                <label
                                    className="form-field__label"
                                    htmlFor="name"
                                >
                                    Stored value
                                </label>

                                <input
                                    id="name"
                                    name="name"
                                    type="text"
                                    className="form-field__input"
                                    value={form.name}
                                    onChange={handleChange}
                                    placeholder="e.g. MALE"
                                    autoComplete="off"
                                    disabled={isLoading}
                                />

                                <p className="form-field__hint">
                                    This exact string is written to an
                                    Answer when picked. If a question on
                                    this list is mapped to an enum-typed
                                    entity field (mappedEntity/mappedField),
                                    it must match that enum's constant name
                                    precisely (e.g. "MALE," not "Male").
                                </p>
                            </div>

                            <div className="form-field">
                                <label
                                    className="form-field__label"
                                    htmlFor="label"
                                >
                                    Label
                                </label>

                                <input
                                    id="label"
                                    name="label"
                                    type="text"
                                    className="form-field__input"
                                    value={form.label}
                                    onChange={handleChange}
                                    placeholder="e.g. Male"
                                    disabled={isLoading}
                                />

                                <p className="form-field__hint">
                                    The text enumerators see and select.
                                </p>
                            </div>

                            <div className="form-field">
                                <label
                                    className="form-field__label"
                                    htmlFor="orderIndex"
                                >
                                    Order
                                </label>

                                <input
                                    id="orderIndex"
                                    name="orderIndex"
                                    type="number"
                                    className="form-field__input"
                                    value={form.orderIndex}
                                    onChange={handleChange}
                                    placeholder="e.g. 1"
                                    disabled={isLoading}
                                />

                                <p className="form-field__hint">
                                    Position of this option within the "
                                    {decodedListName}" list.
                                </p>
                            </div>
                        </div>
                    ),
                },
            ]}
            error={
                validationError ??
                (error
                    ? "Unable to create the choice. Please check the details and try again."
                    : undefined)
            }
            isLoading={isLoading}
            submitLabel="Add choice"
            loadingLabel="Adding…"
            cancelLabel="Cancel"
            onCancel={() => navigate(`/admin/choices/${listName}`)}
            onSubmit={handleSubmit}
        />
    );
}

export default CreateChoice;