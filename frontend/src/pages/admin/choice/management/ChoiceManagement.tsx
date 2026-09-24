import { useParams, Link } from "react-router-dom";
import DataTable, { type DataTableColumn } from "../../../../components/data/DataTable";
import PageContainer from "../../../../components/PageContainer";
import type { ChoiceDto } from "../../../../models/ChoiceDto";
import { useGetChoicesByListNameQuery, useDeleteChoiceMutation } from "../../../../redux/ChoiceApi";
import "./choice-management.css";

function ChoiceManagement() {
    const { listName } = useParams<{ listName: string }>();
    const decodedListName = listName ? decodeURIComponent(listName) : "";

    const {
        data: choices,
        isLoading,
        isFetching,
        error,
        refetch,
    } = useGetChoicesByListNameQuery(decodedListName, {
        skip: !decodedListName,
    });

    const [deleteChoice, { isLoading: isDeleting }] =
        useDeleteChoiceMutation();

    const handleDelete = async (choice: ChoiceDto) => {
        const confirmed = window.confirm(
            `Delete choice "${choice.label}" from "${decodedListName}"? Any question referencing this option will lose it.`
        );

        if (!confirmed) {
            return;
        }

        try {
            await deleteChoice({
                choiceId: choice.id,
                listName: decodedListName,
            }).unwrap();
        } catch {
            // API error surfaced via the error state below on next fetch.
        }
    };

    const columns: DataTableColumn<ChoiceDto>[] = [
        {
            key: "orderIndex",
            header: "#",
            render: (choice) => (
                <span className="choice-management__order">
                    {choice.orderIndex}
                </span>
            ),
        },
        {
            key: "name",
            header: "Stored value",
            render: (choice) => (
                <span className="choice-management__name">
                    {choice.name}
                </span>
            ),
        },
        {
            key: "label",
            header: "Label",
            render: (choice) => choice.label,
        },
        {
            key: "actions",
            header: "",
            render: (choice) => (
                <div className="choice-management__actions">
                    <Link
                        to={`/admin/choices/${listName}/${choice.id}/edit`}
                        className="choice-management__edit"
                    >
                        Edit
                    </Link>

                    <button
                        type="button"
                        className="choice-management__delete"
                        onClick={() => handleDelete(choice)}
                        disabled={isDeleting}
                    >
                        Delete
                    </button>
                </div>
            ),
        },
    ];

    // Sorted client-side by orderIndex, same convention as choices are
    // rendered in an enumerator-facing select.
    const sortedChoices = [...(choices ?? [])].sort(
        (a, b) => a.orderIndex - b.orderIndex
    );

    return (
        <PageContainer size="wide">
            <div className="choice-management">
                <header className="choice-management__header">
                    <div className="choice-management__heading">
                        <span className="choice-management__eyebrow">
                            Administration ·{" "}
                            <Link to="/admin/choices">Choice lists</Link>
                        </span>

                        <h1 className="choice-management__title">
                            {decodedListName}
                        </h1>

                        <p className="choice-management__description">
                            Options available to any question with
                            choiceListName "{decodedListName}."
                        </p>
                    </div>

                    <Link
                        to={`/admin/choices/${listName}/create`}
                        className="choice-management__create"
                    >
                        Add choice
                    </Link>
                </header>

                {error ? (
                    <div className="choice-management__state">
                        <div>
                            <h2>Unable to load choices</h2>
                            <p>
                                Something went wrong while retrieving
                                this choice list.
                            </p>
                        </div>

                        <button
                            type="button"
                            className="choice-management__retry"
                            onClick={refetch}
                        >
                            Try again
                        </button>
                    </div>
                ) : (
                    <section className="choice-management__table-section">
                        <DataTable<ChoiceDto>
                            columns={columns}
                            data={sortedChoices}
                            getRowKey={(choice) => choice.id}
                            isLoading={isLoading || isFetching}
                            emptyMessage="No choices exist for this list yet."
                            loadingMessage="Loading choices"
                        />
                    </section>
                )}
            </div>
        </PageContainer>
    );
}

export default ChoiceManagement;