import { ArrowLeft, Save } from "lucide-react";
import { useEffect, useState, type FormEvent } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import Flash from "../../../../components/flash/Flash";
import PageContainer from "../../../../components/PageContainer";
import type { UpdateChoiceRequest } from "../../../../models/UpdateChoiceRequest";
import {
    useGetChoicesByListNameQuery,
    useUpdateChoiceMutation,
} from "../../../../redux/ChoiceApi";
import "./choice-edit.css";

function ChoiceEdit() {
    const { listName = "", choiceId = "" } = useParams<{
        listName: string;
        choiceId: string;
    }>();

    const numericChoiceId = Number(choiceId);
    const navigate = useNavigate();

    const listPath = `/admin/choices/${encodeURIComponent(listName)}`;

    const {
        data: choices,
        isLoading,
        error,
    } = useGetChoicesByListNameQuery(listName, {
        skip: !listName,
    });

    const choice = choices?.find((item) => item.id === numericChoiceId);

    const [updateChoice, { isLoading: isSaving }] = useUpdateChoiceMutation();

    const [label, setLabel] = useState("");
    const [orderIndex, setOrderIndex] = useState("0");
    const [submitError, setSubmitError] = useState<string | null>(null);
    const [validationError, setValidationError] = useState<string | null>(null);

    useEffect(() => {
        if (choice) {
            setLabel(choice.label);
            setOrderIndex(String(choice.orderIndex));
        }
        // Depend on the id so refetches don't overwrite in-progress edits.
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [choice?.id]);

    const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        if (!choice) return;

        const trimmedLabel = label.trim();
        const parsedOrder = Number(orderIndex);

        if (!trimmedLabel) {
            setValidationError("Label is required.");
            return;
        }
        if (!Number.isInteger(parsedOrder) || parsedOrder < 0) {
            setValidationError("Order must be a whole number, 0 or greater.");
            return;
        }

        setValidationError(null);
        setSubmitError(null);

        const body: UpdateChoiceRequest = {
            name: choice.name,
            label: trimmedLabel,
            orderIndex: parsedOrder,
        };

        try {
            await updateChoice({ choiceId: choice.id, body }).unwrap();

            navigate(listPath, {
                state: {
                    flash: "Choice updated successfully.",
                    flashType: "success",
                },
            });
        } catch {
            setSubmitError("Unable to save the choice. Please try again.");
        }
    };

    return (
        <PageContainer size="default">
            <div className="choice-edit">
                <header className="choice-edit__header">
                    <span className="choice-edit__eyebrow">
                        Administration ·{" "}
                        <Link to="/admin/choices">Choices</Link> ·{" "}
                        <Link to={listPath}>{listName}</Link>
                    </span>
                    <h1 className="choice-edit__title">
                        {choice ? `Edit ${choice.label}` : "Edit choice"}
                    </h1>
                </header>

                <Flash />

                {error ? (
                    <div className="choice-edit__state">
                        <h2>Unable to load choice</h2>
                        <p>Something went wrong while retrieving this choice.</p>
                    </div>
                ) : isLoading ? (
                    <div className="choice-edit__state">Loading choice…</div>
                ) : !choice ? (
                    <div className="choice-edit__state">
                        <h2>Choice not found</h2>
                        <p>It may have been deleted.</p>
                        <Link to={listPath} className="choice-edit__back">
                            <ArrowLeft size={16} aria-hidden="true" />
                            Back to {listName}
                        </Link>
                    </div>
                ) : (
                    <form className="choice-edit__form" onSubmit={handleSubmit} noValidate>
                        <div className="choice-edit__field">
                            <label htmlFor="choice-name">Name</label>
                            <input id="choice-name" type="text" value={choice.name} disabled />
                            <span className="choice-edit__hint">
                                The name is the stored value and can't be changed.
                            </span>
                        </div>

                        <div className="choice-edit__field">
                            <label htmlFor="choice-label">Label</label>
                            <input
                                id="choice-label"
                                type="text"
                                value={label}
                                onChange={(e) => setLabel(e.target.value)}
                                aria-invalid={validationError === "Label is required."}
                                autoFocus
                            />
                        </div>

                        <div className="choice-edit__field">
                            <label htmlFor="choice-order">Order</label>
                            <input
                                id="choice-order"
                                type="number"
                                min={0}
                                step={1}
                                value={orderIndex}
                                onChange={(e) => setOrderIndex(e.target.value)}
                            />
                        </div>

                        {validationError && (
                            <p className="choice-edit__error" role="alert">
                                {validationError}
                            </p>
                        )}
                        {submitError && (
                            <p className="choice-edit__error" role="alert">
                                {submitError}
                            </p>
                        )}

                        <div className="choice-edit__actions">
                            <Link to={listPath} className="choice-edit__cancel">
                                Cancel
                            </Link>
                            <button
                                type="submit"
                                className="choice-edit__submit"
                                disabled={isSaving}
                            >
                                <Save size={16} aria-hidden="true" />
                                {isSaving ? "Saving…" : "Save changes"}
                            </button>
                        </div>
                    </form>
                )}
            </div>
        </PageContainer>
    );
}

export default ChoiceEdit;