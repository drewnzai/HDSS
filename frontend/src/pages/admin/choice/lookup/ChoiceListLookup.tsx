import { useState, type ChangeEvent, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import PageContainer from "../../../../components/PageContainer";
import "./choice-list-lookup.css";

function ChoiceListLookup() {
    const navigate = useNavigate();
    const [listName, setListName] = useState("");

    const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
        setListName(event.target.value);
    };

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();

        const trimmed = listName.trim();
        if (!trimmed) {
            return;
        }

        navigate(`/admin/choices/${encodeURIComponent(trimmed)}`);
    };

    return (
        <PageContainer size="wide">
            <div className="choice-lookup">
                <header className="choice-lookup__header">
                    <div className="choice-lookup__heading">
                        <span className="choice-lookup__eyebrow">
                            Administration
                        </span>

                        <h1 className="choice-lookup__title">
                            Choice lists
                        </h1>

                        <p className="choice-lookup__description">
                            Choice lists are shared across forms and
                            questions. Enter a list name to view or manage
                            its options — the exact value used as a
                            question's "choiceListName."
                        </p>
                    </div>
                </header>

                <form
                    className="choice-lookup__form"
                    onSubmit={handleSubmit}
                >
                    <div className="form-field">
                        <label
                            className="form-field__label"
                            htmlFor="listName"
                        >
                            List name
                        </label>

                        <input
                            id="listName"
                            name="listName"
                            type="text"
                            className="form-field__input"
                            value={listName}
                            onChange={handleChange}
                            placeholder="e.g. sex"
                            autoComplete="off"
                        />

                        <p className="form-field__hint">
                            Existing lists include ones referenced from
                            question definitions — e.g. "sex,"
                            "relationship_to_head," "membership_start_type,"
                            "yes_no."
                        </p>
                    </div>

                    <button
                        type="submit"
                        className="choice-lookup__submit"
                        disabled={!listName.trim()}
                    >
                        View list
                    </button>
                </form>
            </div>
        </PageContainer>
    );
}

export default ChoiceListLookup;