import { useState } from "react";
import type { ChangeEvent, FormEvent } from "react";
import { useImportLocationsMutation } from "../../store/LocationApi";
import { LOCATION_TYPE_LABELS } from "../../models/Location";
import type { LocationType } from "../../models/Location";

function LocationImport() {
    const [file, setFile] = useState<File | null>(null);
    const [importLocations, { data: result, isLoading, error }] = useImportLocationsMutation();

    const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
        setFile(e.target.files?.[0] ?? null);
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        if (!file) return;

        const formData = new FormData();
        formData.append("file", file);

        try {
            await importLocations(formData).unwrap();
        } catch {
            // error state below reflects the failure
        }
    };

    return (
    <div className="card">
      <span className="ledger-section__eyebrow">§ Admin — Import Locations</span>
      <h1>Import from spreadsheet</h1>
      <p>
        Upload a completed location template. Existing locations are matched and reused —
        nothing is duplicated on a repeat import.
      </p>

      <a
        href="/templates/HDSS_Location_Import_Template.xlsx"
        download
        className="btn btn--ghost"
        style={{ display: "inline-block", marginBottom: "var(--space-5)" }}
      >
        Download blank template
      </a>

      <form onSubmit={handleSubmit} noValidate>
        <div className="ledger-section">
          <span className="ledger-section__eyebrow">§ 01 — File</span>
          <div className="field">
            <label className="field__label" htmlFor="file">
              Spreadsheet (.xlsx)
            </label>
            <input
              id="file"
              type="file"
              accept=".xlsx"
              onChange={handleFileChange}
              required
            />
          </div>
        </div>

        {error && (
          <p className="field__error" style={{ marginTop: "var(--space-4)" }}>
            Import failed. Check the file format and try again.
          </p>
        )}

        <button
          type="submit"
          className="btn btn--primary"
          disabled={!file || isLoading}
          style={{ marginTop: "var(--space-5)" }}
        >
          {isLoading ? "Importing…" : "Import"}
        </button>
      </form>

      {
        result && (
            <div className="ledger-section">
                <span className="ledger-section__eyebrow">§ 02 — Result</span>

                <p>
                    Processed <strong>{result.rowsProcessed}</strong> rows
                    {result.errors.length > 0 && (
                        <> — <span style={{ color: "var(--color-danger)" }}>{result.errors.length} had errors</span></>
                    )}
                    .
                </p>

                <div className="import-summary">
                    {(Object.keys(result.created) as LocationType[]).map((type) => {
                        const createdCount = result.created[type] ?? 0;
                        const reusedCount = result.reused[type] ?? 0;
                        if (createdCount === 0 && reusedCount === 0) return null;
                        return (
                            <div key={type} className="import-summary__row">
                                <span className="import-summary__label">{LOCATION_TYPE_LABELS[type]}</span>
                                <span className="badge badge--success">{createdCount} created</span>
                                {reusedCount > 0 && <span className="badge badge--warn">{reusedCount} reused</span>}
                            </div>
                        );
                    })}
                </div>

                {result.errors.length > 0 && (
                    <div className="ledger-section">
                        <span className="ledger-section__eyebrow">§ 03 — Errors</span>
                        <ul className="import-errors">
                            {result.errors.map((err, i) => (
                                <li key={i} className="field__error">
                                    {err}
                                </li>
                            ))}
                        </ul>
                    </div>
                )}
            </div>
        )
    }
    </div >
  );
}

export default LocationImport;