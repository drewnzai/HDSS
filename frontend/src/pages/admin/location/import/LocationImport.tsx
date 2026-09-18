import { useState } from "react";
import type { ChangeEvent, FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import "./location-import.css";
import FormPage from "../../../../components/form-page/FormPage";
import { type LocationType, LOCATION_TYPE_LABELS } from "../../../../models/Location";
import { useImportLocationsMutation } from "../../../../store/LocationApi";
import type { LocationImportResult } from "../../../../models/LocationImportResult";
import "./location-import.css";

function LocationImport() {
    const navigate = useNavigate();

    const [file, setFile] = useState<File | null>(null);

    const [
        importLocations,
        {
            data: result,
            isLoading,
            error,
        },
    ] = useImportLocationsMutation();

    const handleFileChange = (
        event: ChangeEvent<HTMLInputElement>
    ) => {
        setFile(event.target.files?.[0] ?? null);
    };

    const handleSubmit = async (
        event: FormEvent<HTMLFormElement>
    ) => {
        event.preventDefault();

        if (!file) return;

        const formData = new FormData();
        formData.append("file", file);

        try {
            await importLocations(formData).unwrap();
        } catch {
            // Mutation error state is displayed below.
        }
    };

    return (
        <FormPage
            eyebrow="Location Management"
            title="Import locations"
            description="Upload a completed location spreadsheet to add or reconcile locations in the system."
            sections={[
                {
                    eyebrow: "Location Import",
                    title: "Spreadsheet",
                    children: (
                        <div className="location-import__file-section">
                            <label
                                className="location-import__file-label"
                                htmlFor="location-import-file"
                            >
                                Location spreadsheet
                            </label>

                            <div
                                className={[
                                    "location-import__dropzone",
                                    file
                                        ? "location-import__dropzone--selected"
                                        : "",
                                ]
                                    .filter(Boolean)
                                    .join(" ")}
                            >
                                <input
                                    id="location-import-file"
                                    className="location-import__file-input"
                                    type="file"
                                    accept=".xlsx"
                                    onChange={handleFileChange}
                                    disabled={isLoading}
                                />

                                <div className="location-import__dropzone-content">
                                    <span className="location-import__dropzone-title">
                                        {file
                                            ? file.name
                                            : "Choose an .xlsx spreadsheet"}
                                    </span>

                                    <span className="location-import__dropzone-description">
                                        {file
                                            ? formatFileSize(file.size)
                                            : "Only Excel .xlsx files are supported."}
                                    </span>
                                </div>

                                <label
                                    className="location-import__browse"
                                    htmlFor="location-import-file"
                                >
                                    {file ? "Change file" : "Browse"}
                                </label>
                            </div>

                            <p className="location-import__hint">
                                Existing locations are matched and reused.
                                Repeating an import will not create
                                duplicates.
                            </p>

                            <a
                                href="/templates/HDSS_Location_Import_Template.xlsx"
                                download
                                className="location-import__template"
                            >
                                Download blank template
                            </a>
                        </div>
                    ),
                },
            ]}
            error={
                error ? (
                    <div>
                        <strong>Import failed.</strong>{" "}
                        Check that the spreadsheet uses the expected
                        template and try again.
                    </div>
                ) : undefined
            }
            submitLabel="Import locations"
            loadingLabel="Importing..."
            cancelLabel="Back to locations"
            isLoading={isLoading}
            onCancel={() => navigate("/admin/locations")}
            onSubmit={handleSubmit}
            after={
                result ? (
                    <ImportResult result={result} />
                ) : undefined
            }
        />
    );
}

interface ImportResultProps {
    result: LocationImportResult;
}

function ImportResult({ result }: ImportResultProps) {
    const locationTypes = Array.from(
        new Set([
            ...Object.keys(result.created),
            ...Object.keys(result.reused),
        ])
    ) as LocationType[];

    return (
        <section className="location-import__result">
            <header className="location-import__result-header">
                <span className="location-import__eyebrow">
                    § 02 — Result
                </span>

                <h2 className="location-import__result-title">
                    Import complete
                </h2>

                <p className="location-import__result-summary">
                    Processed{" "}
                    <strong>{result.rowsProcessed}</strong>{" "}
                    {result.rowsProcessed === 1 ? "row" : "rows"}.
                    {result.errors.length > 0 && (
                        <>
                            {" "}
                            <span className="location-import__error-count">
                                {result.errors.length}{" "}
                                {result.errors.length === 1
                                    ? "row"
                                    : "rows"}{" "}
                                had errors.
                            </span>
                        </>
                    )}
                </p>
            </header>

            <div className="location-import__summary">
                {locationTypes.map((type) => {
                    const createdCount =
                        result.created[type] ?? 0;
                    const reusedCount =
                        result.reused[type] ?? 0;

                    if (
                        createdCount === 0 &&
                        reusedCount === 0
                    ) {
                        return null;
                    }

                    return (
                        <div
                            key={type}
                            className="location-import__summary-row"
                        >
                            <span className="location-import__summary-label">
                                {LOCATION_TYPE_LABELS[type]}
                            </span>

                            <div className="location-import__summary-values">
                                {createdCount > 0 && (
                                    <span className="location-import__badge location-import__badge--success">
                                        {createdCount} created
                                    </span>
                                )}

                                {reusedCount > 0 && (
                                    <span className="location-import__badge location-import__badge--warning">
                                        {reusedCount} reused
                                    </span>
                                )}
                            </div>
                        </div>
                    );
                })}
            </div>

            {result.errors.length > 0 && (
                <div className="location-import__errors">
                    <header className="location-import__errors-header">
                        <span className="location-import__eyebrow">
                            § 03 — Errors
                        </span>

                        <h3 className="location-import__errors-title">
                            Rows requiring attention
                        </h3>
                    </header>

                    <ul className="location-import__error-list">
                        {result.errors.map((message, index) => (
                            <li
                                key={`${index} -${message} `}
                                className="location-import__error-item"
                            >
                                {message}
                            </li>
                        ))}
                    </ul>
                </div>
            )}
        </section>
    );
}

function formatFileSize(bytes: number): string {
    if (bytes < 1024) {
        return `${bytes} B`;
    }

    if (bytes < 1024 * 1024) {
        return `${(bytes / 1024).toFixed(1)} KB`;
    }

    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export default LocationImport;