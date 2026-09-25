import { useState } from "react";
import type { ChangeEvent, FormEvent } from "react";
import { useNavigate, useParams } from "react-router-dom";
import FormPage from "../../../../components/form-page/FormPage";
import type { CreateQuestionRequest } from "../../../../models/CreateQuestionRequest";
import type { MappedEntity } from "../../../../models/types/MappedEntity";
import type { QuestionType } from "../../../../models/types/QuestionTypes";
import { useGetListNamesQuery } from "../../../../redux/ChoiceApi";
import { useGetMappableFieldsQuery } from "../../../../redux/MappedFieldApi";
import { useCreateQuestionMutation } from "../../../../redux/QuestionApi";
import "./create-question.css";

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
    // Only used when type === GEOPOINT — holds the longitude field's
    // selection while mappedField holds latitude; combined into a single
    // comma-separated mappedField string on submit.
    mappedFieldSecondary: string;
}

const initialForm: QuestionFormState = {
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
    mappedFieldSecondary: "",
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
    { value: "SELECT_HOUSEHOLD_MEMBER", label: "Select household member" },
    { value: "NOTE", label: "Note (no answer)" },
    { value: "CALCULATE", label: "Calculation" },
];

const TYPES_WITH_CHOICE_LIST: QuestionType[] = [
    "SELECT_ONE",
    "SELECT_MULTIPLE",
];

// SELECT_HOUSEHOLD_MEMBER's options come from already-synced Individual
// records in the current household, not from Choice rows — no
// choiceListName applies to it, and its sex/adult-age filter is
// resolved in the Android app from mappedField's name (e.g.
// "motherClientId" -> FEMALE, "fatherClientId" -> MALE), not stored here.
const HOUSEHOLD_MEMBER_SELECT: QuestionType = "SELECT_HOUSEHOLD_MEMBER";

// Matches the backend MappedEntity enum.
const MAPPED_ENTITY_OPTIONS: { value: MappedEntity; label: string }[] = [
    { value: "NONE", label: "None — not mapped to a record field" },
    { value: "HOUSEHOLD", label: "Household" },
    { value: "INDIVIDUAL", label: "Individual" },
    { value: "MEMBERSHIP", label: "Membership" },
];

function CreateQuestion() {
    const navigate = useNavigate();
    const { formId } = useParams<{ formId: string }>();
    const numericFormId = Number(formId);

    const [form, setForm] = useState<QuestionFormState>(initialForm);
    const [validationError, setValidationError] = useState<string | null>(
        null
    );

    const [
        createQuestion,
        { isLoading, error },
    ] = useCreateQuestionMutation();

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

        setForm((current) => {
            const next = {
                ...current,
                [name]: type === "checkbox" ? checked : value,
            };

            // A field selected under the old entity/type may not exist
            // under the new one — clear rather than carry over a stale,
            // now-invalid mappedField.
            if (name === "mappedEntity" || name === "type") {
                next.mappedField = "";
                next.mappedFieldSecondary = "";
            }

            return next;
        });

        if (validationError) {
            setValidationError(null);
        }
    };

    const requiresChoiceList = TYPES_WITH_CHOICE_LIST.includes(form.type);
    const isCalculation = form.type === "CALCULATE";
    const isMapped = form.mappedEntity !== "NONE";

    const {
        data: mappableFields = [],
        isFetching: isLoadingFields,
    } = useGetMappableFieldsQuery(form.mappedEntity, { skip: !isMapped });

    const {
        data: listNames = [],
        isFetching: isLoadingListNames,
    } = useGetListNamesQuery(undefined, { skip: !requiresChoiceList });

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

        if (isMapped && form.type !== "GEOPOINT" && !form.mappedField.trim()) {
            setValidationError(
                "Mapped field is required when a target record is selected."
            );
            return;
        }

        if (isMapped && form.type === "GEOPOINT") {
            if (!form.mappedField || !form.mappedFieldSecondary) {
                setValidationError(
                    "Both a latitude field and a longitude field are required for a GEOPOINT question."
                );
                return;
            }

            if (form.mappedField === form.mappedFieldSecondary) {
                setValidationError(
                    "Latitude and longitude must map to two different fields."
                );
                return;
            }
        }

        const mappedField = !isMapped
            ? ""
            : form.type === "GEOPOINT"
                ? `${form.mappedField},${form.mappedFieldSecondary}`
                : form.mappedField;

        const body: CreateQuestionRequest = {
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
            mappedField,
        };

        try {
            const created = await createQuestion({
                formId: numericFormId,
                body,
            }).unwrap();

            navigate(`/admin/forms/${formId}/questions`, {
                state: {
                    flash: `Question "${created.label}" created successfully.`,
                },
            });
        } catch {
            // API error is exposed through `error`.
        }
    };

    return (
        <FormPage
            eyebrow="§ Admin — Form Management"
            title="Add question"
            description="Define a question to collect within this form."
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
                                    disabled={isLoading}
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
                                    disabled={isLoading}
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
                                    disabled={isLoading}
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
                                        disabled={isLoading}
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

                                    <select
                                        id="choiceListName"
                                        name="choiceListName"
                                        className="form-field__input"
                                        value={form.choiceListName}
                                        onChange={handleChange}
                                        disabled={isLoading || isLoadingListNames}
                                    >
                                        <option value="">
                                            {isLoadingListNames
                                                ? "Loading lists…"
                                                : "Select a list…"}
                                        </option>
                                        {listNames.map((name) => (
                                            <option key={name} value={name}>
                                                {name}
                                            </option>
                                        ))}
                                    </select>

                                    <p className="form-field__hint">
                                        Name of the shared choice list this
                                        question draws its options from. To
                                        add a new list, create its choices
                                        first from the Choice lists page.
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
                                    disabled={isLoading}
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

                            {isMapped && form.type === "GEOPOINT" && (
                                <>
                                    <div className="form-field">
                                        <label
                                            className="form-field__label"
                                            htmlFor="mappedField"
                                        >
                                            Latitude field
                                        </label>

                                        <select
                                            id="mappedField"
                                            name="mappedField"
                                            className="form-field__input"
                                            value={form.mappedField}
                                            onChange={handleChange}
                                            disabled={isLoading || isLoadingFields}
                                        >
                                            <option value="">
                                                {isLoadingFields
                                                    ? "Loading fields…"
                                                    : "Select a field…"}
                                            </option>
                                            {mappableFields.map((option) => (
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
                                            htmlFor="mappedFieldSecondary"
                                        >
                                            Longitude field
                                        </label>

                                        <select
                                            id="mappedFieldSecondary"
                                            name="mappedFieldSecondary"
                                            className="form-field__input"
                                            value={form.mappedFieldSecondary}
                                            onChange={handleChange}
                                            disabled={isLoading || isLoadingFields}
                                        >
                                            <option value="">
                                                {isLoadingFields
                                                    ? "Loading fields…"
                                                    : "Select a field…"}
                                            </option>
                                            {mappableFields.map((option) => (
                                                <option
                                                    key={option.value}
                                                    value={option.value}
                                                >
                                                    {option.label}
                                                </option>
                                            ))}
                                        </select>

                                        <p className="form-field__hint">
                                            The captured point's latitude and
                                            longitude are written to these two
                                            fields.
                                        </p>
                                    </div>
                                </>
                            )}

                            {isMapped && form.type !== "GEOPOINT" && (
                                <div className="form-field">
                                    <label
                                        className="form-field__label"
                                        htmlFor="mappedField"
                                    >
                                        Mapped field
                                    </label>

                                    <select
                                        id="mappedField"
                                        name="mappedField"
                                        className="form-field__input"
                                        value={form.mappedField}
                                        onChange={handleChange}
                                        disabled={isLoading || isLoadingFields}
                                    >
                                        <option value="">
                                            {isLoadingFields
                                                ? "Loading fields…"
                                                : "Select a field…"}
                                        </option>
                                        {mappableFields.map((option) => (
                                            <option
                                                key={option.value}
                                                value={option.value}
                                            >
                                                {option.label}
                                            </option>
                                        ))}
                                    </select>

                                    {form.type === HOUSEHOLD_MEMBER_SELECT && (
                                        <p className="form-field__hint">
                                            The app derives the sex/age
                                            filter for the household-member
                                            picker from which field is
                                            chosen here (e.g. Mother →
                                            female, Father → male).
                                        </p>
                                    )}
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
                                    disabled={isLoading}
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
                                    disabled={isLoading}
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
                                    disabled={isLoading}
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
                                    disabled={isLoading}
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
                                        disabled={isLoading}
                                    />
                                </div>
                            )}
                        </div>
                    ),
                },
            ]}
            error={
                validationError ??
                (error
                    ? "Unable to create the question. Please check the details and try again."
                    : undefined)
            }
            isLoading={isLoading}
            submitLabel="Add question"
            loadingLabel="Adding…"
            cancelLabel="Cancel"
            onCancel={() => navigate(`/admin/forms/${formId}/questions`)}
            onSubmit={handleSubmit}
        />
    );
}

export default CreateQuestion;