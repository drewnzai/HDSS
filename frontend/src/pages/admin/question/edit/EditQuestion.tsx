import { useState, useEffect, type ChangeEvent, type FormEvent } from "react";
import { useNavigate, useParams } from "react-router-dom";
import FormPage from "../../../../components/form-page/FormPage";
import PageContainer from "../../../../components/PageContainer";
import type { MappedEntity } from "../../../../models/types/MappedEntity";
import type { QuestionType } from "../../../../models/types/QuestionTypes";
import type { UpdateQuestionRequest } from "../../../../models/UpdateQuestionRequest";
import { useGetQuestionsByFormQuery, useUpdateQuestionMutation } from "../../../../redux/QuestionApi";
import "./edit-question.css";

interface QuestionFormState {
    name: string;
    label: string;
    hint: string;
    type: QuestionType;
    required: boolean;
    relevant: string;
    constraint: string;
    constraintMessage: string;
    calculation: string;
    choiceListName: string;
    mappedEntity: MappedEntity;
    mappedField: string;
}

const emptyForm: QuestionFormState = {
    name: "",
    label: "",
    hint: "",
    type: "TEXT",
    required: false,
    relevant: "",
    constraint: "",
    constraintMessage: "",
    calculation: "",
    choiceListName: "",
    mappedEntity: "NONE",
    mappedField: "",
};

// ODK/XLSForm-aligned types, matching the Question domain model.
const TYPE_OPTIONS: { value: QuestionType; label: string }[] = [
    { value: "TEXT", label: "Text" },
    { value: "INTEGER", label: "Integer" },
    { value: "DECIMAL", label: "Decimal" },
    { value: "DATE", label: "Date" },
    { value: "DATETIME", label: "Date & time" },
    { value: "GEOPOINT", label: "GPS point" },
    { value: "SELECT_ONE", label: "Select one" },
    { value: "SELECT_MULTIPLE", label: "Select multiple" },
    { value: "NOTE", label: "Note (no answer)" },
    { value: "CALCULATE", label: "Calculation" },
];

const TYPES_WITH_CHOICE_LIST: QuestionType[] = [
    "SELECT_ONE",
    "SELECT_MULTIPLE",
];

// Matches the backend MappedEntity enum.
const MAPPED_ENTITY_OPTIONS: { value: MappedEntity; label: string }[] = [
    { value: "NONE", label: "None — not mapped to a record field" },
    { value: "HOUSEHOLD", label: "Household" },
    { value: "INDIVIDUAL", label: "Individual" },
    { value: "MEMBERSHIP", label: "Membership" },
];

function EditQuestion() {
    const navigate = useNavigate();
    const { formId, questionId } = useParams<{
        formId: string;
        questionId: string;
    }>();
    const numericFormId = Number(formId);
    const numericQuestionId = Number(questionId);

    // No getQuestionById endpoint exists yet — reuse the per-form list
    // query (already cached from QuestionManagement in the common case)
    // and pick the one being edited out of it.
    const {
        data: questions,
        isLoading: isQuestionLoading,
        error: loadError,
    } = useGetQuestionsByFormQuery(numericFormId, {
        skip: Number.isNaN(numericFormId),
    });

    const existingQuestion = questions?.find(
        (q) => q.id === numericQuestionId
    );

    const [form, setForm] = useState<QuestionFormState>(emptyForm);
    const [isPrefilled, setIsPrefilled] = useState(false);
    const [validationError, setValidationError] = useState<string | null>(
        null
    );

    useEffect(() => {
        if (existingQuestion && !isPrefilled) {
            setForm({
                name: existingQuestion.name,
                label: existingQuestion.label,
                hint: existingQuestion.hint,
                type: existingQuestion.type,
                required: existingQuestion.required,
                relevant: existingQuestion.relevant,
                constraint: existingQuestion.constraint,
                constraintMessage: existingQuestion.constraintMessage,
                calculation: existingQuestion.calculation,
                choiceListName: existingQuestion.choiceListName,
                mappedEntity: existingQuestion.mappedEntity,
                mappedField: existingQuestion.mappedField,
            });
            setIsPrefilled(true);
        }
    }, [existingQuestion, isPrefilled]);

    const [
        updateQuestion,
        { isLoading: isSaving, error: saveError },
    ] = useUpdateQuestionMutation();

    const handleChange = (
        event: ChangeEvent<
            HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
        >
    ) => {
        const { name, value, type } = event.target;
        const checked =
            type === "checkbox"
                ? (event.target as HTMLInputElement).checked
                : undefined;

        setForm((current) => ({
            ...current,
            [name]: type === "checkbox" ? checked : value,
        }));

        if (validationError) {
            setValidationError(null);
        }
    };

    const requiresChoiceList = TYPES_WITH_CHOICE_LIST.includes(form.type);
    const isCalculation = form.type === "CALCULATE";
    const isMapped = form.mappedEntity !== "NONE";

    const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();

        const name = form.name.trim();
        const label = form.label.trim();

        if (!name) {
            setValidationError("Name is required.");
            return;
        }

        if (!label) {
            setValidationError("Label is required.");
            return;
        }

        if (requiresChoiceList && !form.choiceListName.trim()) {
            setValidationError(
                "Choice list is required for select-type questions."
            );
            return;
        }

        if (isCalculation && !form.calculation.trim()) {
            setValidationError(
                "Calculation expression is required for calculate-type questions."
            );
            return;
        }

        if (isMapped && !form.mappedField.trim()) {
            setValidationError(
                "Mapped field is required when a target record is selected."
            );
            return;
        }

        if (isMapped && form.type === "GEOPOINT") {
            const fieldCount = form.mappedField
                .split(",")
                .map((f) => f.trim())
                .filter(Boolean).length;

            if (fieldCount !== 2) {
                setValidationError(
                    "A GEOPOINT question must map to exactly two comma-separated fields (latitude,longitude)."
                );
                return;
            }
        }

        const body: UpdateQuestionRequest = {
            name,
            label,
            hint: form.hint.trim(),
            type: form.type,
            required: form.required,
            relevant: form.relevant.trim(),
            constraint: form.constraint.trim(),
            constraintMessage: form.constraintMessage.trim(),
            calculation: isCalculation ? form.calculation.trim() : "",
            choiceListName: requiresChoiceList
                ? form.choiceListName.trim()
                : "",
            mappedEntity: form.mappedEntity,
            mappedField: isMapped ? form.mappedField.trim() : "",
        };

        try {
            const updated = await updateQuestion({
                formId: numericFormId,
                questionId: numericQuestionId,
                body,
            }).unwrap();

            navigate(`/admin/forms/${formId}/questions`, {
                state: {
                    flash: `Question "${updated.label}" updated successfully.`,
                },
            });
        } catch {
            // API error is exposed through `saveError`.
        }
    };

    if (isQuestionLoading || !isPrefilled) {
        if (loadError) {
            return (
                <PageState
                    title="Unable to load question"
                    description="Something went wrong while retrieving this question."
                />
            );
        }

        return <PageState title="Loading…" description="" />;
    }

    return (
        <FormPage
            eyebrow="§ Admin — Form Management"
            title="Edit question"
            description="Update this question's definition."
            sections={[
                {
                    eyebrow: "Definition",
                    title: "Question details",
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
                                    placeholder="e.g. household_code"
                                    autoComplete="off"
                                    disabled={isSaving}
                                />

                                <p className="form-field__hint">
                                    The internal field name (used as the
                                    ODK/XLSForm column name).
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
                                    placeholder="e.g. Household code"
                                    disabled={isSaving}
                                />

                                <p className="form-field__hint">
                                    The question text shown to enumerators.
                                </p>
                            </div>

                            <div className="form-field">
                                <label
                                    className="form-field__label"
                                    htmlFor="type"
                                >
                                    Type
                                </label>

                                <select
                                    id="type"
                                    name="type"
                                    className="form-field__input"
                                    value={form.type}
                                    onChange={handleChange}
                                    disabled={isSaving}
                                >
                                    {TYPE_OPTIONS.map((option) => (
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
                                    htmlFor="required"
                                >
                                    Required
                                </label>

                                <label className="form-field__checkbox">
                                    <input
                                        id="required"
                                        name="required"
                                        type="checkbox"
                                        checked={form.required}
                                        onChange={handleChange}
                                        disabled={isSaving}
                                    />
                                    Enumerator must answer this question
                                </label>
                            </div>

                            {requiresChoiceList && (
                                <div className="form-field">
                                    <label
                                        className="form-field__label"
                                        htmlFor="choiceListName"
                                    >
                                        Choice list
                                    </label>

                                    <input
                                        id="choiceListName"
                                        name="choiceListName"
                                        type="text"
                                        className="form-field__input"
                                        value={form.choiceListName}
                                        onChange={handleChange}
                                        placeholder="e.g. sex"
                                        disabled={isSaving}
                                    />

                                    <p className="form-field__hint">
                                        Name of the shared choice list this
                                        question draws its options from.
                                    </p>
                                </div>
                            )}

                            <div className="form-field">
                                <label
                                    className="form-field__label"
                                    htmlFor="mappedEntity"
                                >
                                    Maps to
                                </label>

                                <select
                                    id="mappedEntity"
                                    name="mappedEntity"
                                    className="form-field__input"
                                    value={form.mappedEntity}
                                    onChange={handleChange}
                                    disabled={isSaving}
                                >
                                    {MAPPED_ENTITY_OPTIONS.map((option) => (
                                        <option
                                            key={option.value}
                                            value={option.value}
                                        >
                                            {option.label}
                                        </option>
                                    ))}
                                </select>

                                <p className="form-field__hint">
                                    If set, this question's answer populates
                                    a field on the record directly, instead
                                    of only being stored as an Answer.
                                </p>
                            </div>

                            {isMapped && (
                                <div className="form-field">
                                    <label
                                        className="form-field__label"
                                        htmlFor="mappedField"
                                    >
                                        Mapped field
                                        {form.type === "GEOPOINT" ? "s" : ""}
                                    </label>

                                    <input
                                        id="mappedField"
                                        name="mappedField"
                                        type="text"
                                        className="form-field__input"
                                        value={form.mappedField}
                                        onChange={handleChange}
                                        placeholder={
                                            form.type === "GEOPOINT"
                                                ? "e.g. latitude,longitude"
                                                : "e.g. firstName"
                                        }
                                        disabled={isSaving}
                                    />

                                    <p className="form-field__hint">
                                        {form.type === "GEOPOINT"
                                            ? `A comma-separated pair of field names on the ${form.mappedEntity.toLowerCase()} record — the captured point's latitude and longitude are written to these, in order.`
                                            : `Exact field name on the ${form.mappedEntity.toLowerCase()} record. For a question whose answer must populate more than one field, separate field names with commas.`}
                                    </p>
                                </div>
                            )}

                            <div className="form-field form-field--full">
                                <label
                                    className="form-field__label"
                                    htmlFor="hint"
                                >
                                    Hint
                                </label>

                                <input
                                    id="hint"
                                    name="hint"
                                    type="text"
                                    className="form-field__input"
                                    value={form.hint}
                                    onChange={handleChange}
                                    placeholder="Optional guidance shown below the question"
                                    disabled={isSaving}
                                />
                            </div>

                            <div className="form-field form-field--full">
                                <label
                                    className="form-field__label"
                                    htmlFor="relevant"
                                >
                                    Relevant (skip logic)
                                </label>

                                <input
                                    id="relevant"
                                    name="relevant"
                                    type="text"
                                    className="form-field__input"
                                    value={form.relevant}
                                    onChange={handleChange}
                                    placeholder="e.g. ${dob_known} = 'no'"
                                    disabled={isSaving}
                                />

                                <p className="form-field__hint">
                                    XLSForm-style expression controlling
                                    when this question is shown.
                                </p>
                            </div>

                            <div className="form-field form-field--full">
                                <label
                                    className="form-field__label"
                                    htmlFor="constraint"
                                >
                                    Constraint (validation)
                                </label>

                                <input
                                    id="constraint"
                                    name="constraint"
                                    type="text"
                                    className="form-field__input"
                                    value={form.constraint}
                                    onChange={handleChange}
                                    placeholder="e.g. . &gt;= 0 and . &lt; 120"
                                    disabled={isSaving}
                                />
                            </div>

                            <div className="form-field form-field--full">
                                <label
                                    className="form-field__label"
                                    htmlFor="constraintMessage"
                                >
                                    Constraint message
                                </label>

                                <input
                                    id="constraintMessage"
                                    name="constraintMessage"
                                    type="text"
                                    className="form-field__input"
                                    value={form.constraintMessage}
                                    onChange={handleChange}
                                    placeholder="Shown to the enumerator when the constraint fails"
                                    disabled={isSaving}
                                />
                            </div>

                            {isCalculation && (
                                <div className="form-field form-field--full">
                                    <label
                                        className="form-field__label"
                                        htmlFor="calculation"
                                    >
                                        Calculation expression
                                    </label>

                                    <input
                                        id="calculation"
                                        name="calculation"
                                        type="text"
                                        className="form-field__input"
                                        value={form.calculation}
                                        onChange={handleChange}
                                        placeholder="e.g. ${dob_known} = 'no'"
                                        disabled={isSaving}
                                    />
                                </div>
                            )}
                        </div>
                    ),
                },
            ]}
            error={
                validationError ??
                (saveError
                    ? "Unable to update the question. Please check the details and try again."
                    : undefined)
            }
            isLoading={isSaving}
            submitLabel="Save changes"
            loadingLabel="Saving…"
            cancelLabel="Cancel"
            onCancel={() => navigate(`/admin/forms/${formId}/questions`)}
            onSubmit={handleSubmit}
        />
    );
}

function PageState({
    title,
    description,
}: {
    title: string;
    description: string;
}) {
    return (
        <PageContainer size="wide">
            <div className="question-management__state">
                <h2>{title}</h2>
                {description && <p>{description}</p>}
            </div>
        </PageContainer>
    );
}

export default EditQuestion;