import { useMemo, useState } from "react";
import {
  Activity,
  AlertCircle,
  AlertTriangle,
  CheckCircle2,
  Database,
  Eye,
  Plus,
  RefreshCw,
  ShieldCheck,
  X,
  XCircle,
} from "lucide-react";

import "./round-management.css";
import type {
  Violation,
  CheckFailure,
  DeltaSummary,
  ValidationReport,
  RoundStatus,
  RoundDto,
  CreateRoundRequest,
  RoundDetailsDto,
} from "../../../models/types/Round";
import {
  useValidateMutation,
  useCreateRoundMutation,
  useGetRoundQuery,
  useGetRoundsQuery,
} from "../../../redux/RoundApi";

interface ApiError {
  status?: number | string;
  data?: unknown;
}

function isObject(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

function isViolation(value: unknown): value is Violation {
  if (!isObject(value)) return false;

  return (
    typeof value.check === "string" &&
    typeof value.ref === "string" &&
    typeof value.detail === "string"
  );
}

function isCheckFailure(value: unknown): value is CheckFailure {
  if (!isObject(value)) return false;

  return (
    typeof value.id === "string" &&
    typeof value.description === "string" &&
    typeof value.count === "number" &&
    typeof value.countIsLowerBound === "boolean" &&
    Array.isArray(value.samples) &&
    value.samples.every(isViolation)
  );
}

function isDeltaSummary(value: unknown): value is DeltaSummary {
  if (!isObject(value)) return false;

  const fields: (keyof DeltaSummary)[] = [
    "individuals",
    "households",
    "memberships",
    "visits",
    "formResponses",
    "answers",
    "skippedVisits",
    "skippedFormResponses",
    "skippedAnswers",
    "publishedForms",
    "questionsInPublishedForms",
  ];

  return fields.every((field) => typeof value[field] === "number");
}

function isValidationReport(value: unknown): value is ValidationReport {
  if (!isObject(value)) return false;

  return (
    typeof value.passed === "boolean" &&
    typeof value.checkedOn === "string" &&
    typeof value.checksRun === "number" &&
    isDeltaSummary(value.delta) &&
    Array.isArray(value.failures) &&
    value.failures.every(isCheckFailure)
  );
}

function getApiErrorMessage(error: unknown): string {
  if (!isObject(error)) {
    return "An unexpected error occurred.";
  }

  const apiError = error as ApiError;
  const data = apiError.data;

  if (isObject(data) && typeof data.detail === "string") {
    return data.detail;
  }

  if (typeof data === "string") {
    return data;
  }

  switch (apiError.status) {
    case 409:
      return "Another round creation is already in progress. Please wait.";
    case 503:
      return "Round creation is temporarily unavailable. Please try again.";
    case 422:
      return "Round validation failed. Review the validation report.";
    case 401:
      return "Your session has expired. Please sign in again.";
    default:
      return "Something went wrong while communicating with the server.";
  }
}

function formatDate(value?: string | null): string {
  if (!value) return "—";

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

function formatNumber(value: number): string {
  return value.toLocaleString();
}

function getMovableCount(delta: DeltaSummary): number {
  return (
    delta.individuals +
    delta.households +
    delta.memberships +
    delta.visits +
    delta.formResponses +
    delta.answers
  );
}

function StatusBadge({ status }: { status: RoundStatus }) {
  const config = {
    CREATING: {
      label: "Creating",
      icon: <RefreshCw size={13} />,
      className: "creating",
    },
    COMPLETED: {
      label: "Completed",
      icon: <CheckCircle2 size={13} />,
      className: "completed",
    },
    FAILED: {
      label: "Failed",
      icon: <XCircle size={13} />,
      className: "failed",
    },
  }[status];

  return (
    <span className={`round-status ${config.className}`}>
      {config.icon}
      {config.label}
    </span>
  );
}

function DeltaSummaryView({ delta }: { delta: DeltaSummary }) {
  const movableItems = [
    { label: "Individuals", value: delta.individuals },
    { label: "Households", value: delta.households },
    { label: "Memberships", value: delta.memberships },
    { label: "Visits", value: delta.visits },
    { label: "Form responses", value: delta.formResponses },
    { label: "Answers", value: delta.answers },
  ];

  const skippedItems = [
    { label: "Visits", value: delta.skippedVisits },
    { label: "Form responses", value: delta.skippedFormResponses },
    { label: "Answers", value: delta.skippedAnswers },
  ];

  const movable = getMovableCount(delta);

  return (
    <div className="delta-summary">
      <div className="delta-total">
        <div>
          <span className="delta-total-label">Movable records</span>

          <strong>{formatNumber(movable)}</strong>
        </div>

        <Database size={22} />
      </div>

      <div className="delta-section">
        <h4>Records to be copied</h4>

        <div className="delta-grid">
          {movableItems.map((item) => (
            <div className="delta-item" key={item.label}>
              <span>{item.label}</span>
              <strong>{formatNumber(item.value)}</strong>
            </div>
          ))}
        </div>
      </div>

      <div className="delta-section">
        <h4>Skipped records</h4>

        <div className="delta-grid">
          {skippedItems.map((item) => (
            <div className="delta-item" key={item.label}>
              <span>{item.label}</span>
              <strong>{formatNumber(item.value)}</strong>
            </div>
          ))}
        </div>
      </div>

      <div className="delta-section">
        <h4>Published forms</h4>

        <div className="delta-grid">
          <div className="delta-item">
            <span>Published forms</span>
            <strong>{formatNumber(delta.publishedForms)}</strong>
          </div>

          <div className="delta-item">
            <span>Questions in published forms</span>
            <strong>{formatNumber(delta.questionsInPublishedForms)}</strong>
          </div>
        </div>
      </div>
    </div>
  );
}

function ViolationView({ violation }: { violation: Violation }) {
  return (
    <div className="violation-item">
      <div className="violation-meta">
        <span>Check</span>
        <strong>{violation.check}</strong>
      </div>

      <div className="violation-meta">
        <span>Reference</span>
        <strong>{violation.ref || "—"}</strong>
      </div>

      <p>{violation.detail}</p>
    </div>
  );
}

function CheckFailureView({ failure }: { failure: CheckFailure }) {
  return (
    <div className="validation-failure-item">
      <div className="validation-failure-heading">
        <div>
          <strong>{failure.description}</strong>
          <span className="failure-id">{failure.id}</span>
        </div>

        <span className="failure-count">
          {failure.countIsLowerBound ? "At least " : ""}
          {formatNumber(failure.count)}
        </span>
      </div>

      {failure.samples.length > 0 && (
        <div className="failure-samples">
          <span>Sample violations ({failure.samples.length})</span>

          <div className="violation-list">
            {failure.samples.map((sample, index) => (
              <ViolationView
                key={`${failure.id}-${sample.check}-${sample.ref}-${index}`}
                violation={sample}
              />
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

function ValidationReportView({ report }: { report: ValidationReport }) {
  return (
    <div className="validation-report">
      <div
        className={`validation-result ${report.passed ? "passed" : "failed"}`}
      >
        {report.passed ? (
          <CheckCircle2 size={20} />
        ) : (
          <AlertTriangle size={20} />
        )}

        <div>
          <strong>
            {report.passed ? "Validation passed" : "Validation failed"}
          </strong>

          <span>{report.checksRun} checks performed</span>
        </div>
      </div>

      <div className="validation-meta">
        <div>
          <span>Checked on</span>
          <strong>{report.checkedOn}</strong>
        </div>

        <div>
          <span>Checks performed</span>
          <strong>{report.checksRun}</strong>
        </div>
      </div>

      <DeltaSummaryView delta={report.delta} />

      {report.failures.length > 0 && (
        <div className="validation-failures">
          <h4>
            <AlertCircle size={16} />
            Validation failures ({report.failures.length})
          </h4>

          <div className="validation-failure-list">
            {report.failures.map((failure, index) => (
              <CheckFailureView
                key={`${failure.id}-${index}`}
                failure={failure}
              />
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

interface CreateRoundDialogProps {
  onClose: () => void;
  onCreated: (round: RoundDto) => void;
}

function CreateRoundDialog({ onClose, onCreated }: CreateRoundDialogProps) {
  const [roundName, setRoundName] = useState("");

  const [validationReport, setValidationReport] =
    useState<ValidationReport | null>(null);

  const [creationValidationReport, setCreationValidationReport] =
    useState<ValidationReport | null>(null);

  const [formError, setFormError] = useState<string | null>(null);

  const [validate, { isLoading: isValidating }] = useValidateMutation();

  const [createRound, { isLoading: isCreating }] = useCreateRoundMutation();

  const isBusy = isValidating || isCreating;

  const handleValidate = async () => {
    setFormError(null);
    setValidationReport(null);
    setCreationValidationReport(null);

    try {
      const report = await validate().unwrap();
      setValidationReport(report);
    } catch (error) {
      setFormError(getApiErrorMessage(error));
    }
  };

  const handleCreate = async () => {
    const trimmedName = roundName.trim();

    if (!trimmedName) {
      setFormError("Please provide a round name.");
      return;
    }

    if (!validationReport?.passed) {
      setFormError("Run a successful validation before creating a round.");
      return;
    }

    setFormError(null);
    setCreationValidationReport(null);

    const request: CreateRoundRequest = {
      roundName: trimmedName,
    };

    try {
      const round = await createRound(request).unwrap();
      onCreated(round);
    } catch (error) {
      const apiError = error as ApiError;

      // The backend returns ValidationReport directly for HTTP 422.
      if (apiError.status === 422 && isValidationReport(apiError.data)) {
          setValidationReport(null);
          setCreationValidationReport(apiError.data);
        return;
      }

      setFormError(getApiErrorMessage(error));
    }
  };

  return (
    <div
      className="round-dialog-backdrop"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget && !isBusy) {
          onClose();
        }
      }}
    >
      <section
        className="round-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="create-round-title"
      >
        <header className="round-dialog-header">
          <div>
            <h2 id="create-round-title">Create a new round</h2>

            <p>
              Validate the current data before creating a database snapshot.
            </p>
          </div>

          <button
            type="button"
            className="icon-button"
            onClick={onClose}
            disabled={isBusy}
            aria-label="Close dialog"
          >
            <X size={20} />
          </button>
        </header>

        <div className="round-dialog-body">
          <div className="round-form-field">
            <label htmlFor="round-name">Round name</label>

            <input
              id="round-name"
              type="text"
              value={roundName}
              maxLength={100}
              placeholder="e.g. October 2026 Round"
              onChange={(event) => {
                setRoundName(event.target.value);
                setFormError(null);
              }}
              disabled={isBusy}
            />

            <span className="field-hint">Maximum 100 characters.</span>
          </div>

          <div className="validation-instructions">
            <ShieldCheck size={20} />

            <div>
              <strong>Pre-creation validation</strong>

              <p>
                The system checks data consistency and determines which records
                can be moved into the new round.
              </p>
            </div>
          </div>

          <button
            type="button"
            className="secondary-button validate-button"
            onClick={handleValidate}
            disabled={isBusy}
          >
            {isValidating ? (
              <>
                <RefreshCw className="spin" size={16} />
                Validating...
              </>
            ) : (
              <>
                <ShieldCheck size={16} />
                Run validation
              </>
            )}
          </button>

          {validationReport && (
            <ValidationReportView report={validationReport} />
          )}

          {creationValidationReport && (
            <div className="creation-validation">
              <h3>Validation changed during creation</h3>

              <p>
                The backend performed validation again before creating the
                round. It did not start the database copy because validation
                failed.
              </p>

              <ValidationReportView report={creationValidationReport} />
            </div>
          )}

          {formError && (
            <div className="round-error">
              <AlertCircle size={17} />
              <span>{formError}</span>
            </div>
          )}
        </div>

        <footer className="round-dialog-footer">
          <button
            type="button"
            className="secondary-button"
            onClick={onClose}
            disabled={isBusy}
          >
            Cancel
          </button>

          <button
            type="button"
            className="primary-button"
            onClick={handleCreate}
            disabled={isBusy || !roundName.trim() || !validationReport?.passed}
          >
            {isCreating ? (
              <>
                <RefreshCw className="spin" size={16} />
                Starting...
              </>
            ) : (
              <>
                <Plus size={16} />
                Create round
              </>
            )}
          </button>
        </footer>
      </section>
    </div>
  );
}

interface RoundDetailsPanelProps {
  roundId: number;
  onClose: () => void;
}

function RoundDetailsPanel({ roundId, onClose }: RoundDetailsPanelProps) {
  const {
    data: round,
    isLoading,
    isError,
    refetch,
  } = useGetRoundQuery(roundId, {
    pollingInterval: 3000,
  });

  return (
    <div className="round-dialog-backdrop">
      <section
        className="round-details-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="round-details-title"
      >
        <header className="round-dialog-header">
          <div>
            <h2 id="round-details-title">Round details</h2>

            <p>Round ID: {roundId}</p>
          </div>

          <button
            type="button"
            className="icon-button"
            onClick={onClose}
            aria-label="Close details"
          >
            <X size={20} />
          </button>
        </header>

        <div className="round-dialog-body">
          {isLoading && (
            <div className="round-loading">
              <RefreshCw className="spin" size={20} />
              Loading round details...
            </div>
          )}

          {isError && (
            <div className="round-error">
              <AlertCircle size={17} />
              Unable to load round details.
              <button type="button" onClick={() => refetch()}>
                Retry
              </button>
            </div>
          )}

          {round && <RoundDetailsContent round={round} />}
        </div>

        <footer className="round-dialog-footer">
          <button type="button" className="secondary-button" onClick={onClose}>
            Close
          </button>
        </footer>
      </section>
    </div>
  );
}

function RoundDetailsContent({ round }: { round: RoundDetailsDto }) {
  return (
    <>
      <div className="round-detail-heading">
        <div>
          <span className="detail-label">Round name</span>

          <h3>{round.roundName}</h3>
        </div>

        <StatusBadge status={round.status} />
      </div>

      <div className="round-detail-grid">
        <div>
          <span>Database</span>
          <strong>{round.databaseName}</strong>
        </div>

        <div>
          <span>Created</span>
          <strong>{formatDate(round.createdAt)}</strong>
        </div>

        <div>
          <span>Started</span>
          <strong>{formatDate(round.startedAt)}</strong>
        </div>

        <div>
          <span>Completed</span>
          <strong>{formatDate(round.completedAt)}</strong>
        </div>
      </div>

      {round.status === "CREATING" && (
        <div className="round-progress">
          <RefreshCw className="spin" size={18} />

          <div>
            <strong>Database snapshot in progress</strong>

            <p>
              The database is being copied. This page will refresh
              automatically.
            </p>
          </div>
        </div>
      )}

      {round.status === "FAILED" && (
        <div className="round-error">
          <AlertCircle size={17} />

          <span>
            {round.errorMessage ||
              "The database snapshot could not be completed."}
          </span>
        </div>
      )}

      {round.validationReport && (
        <>
          <h3 className="details-section-title">Validation report</h3>

          <ValidationReportView report={round.validationReport} />
        </>
      )}
    </>
  );
}

export default function RoundManagement() {
  const [showCreateDialog, setShowCreateDialog] = useState(false);

  const [selectedRoundId, setSelectedRoundId] = useState<number | null>(null);

  const {
    data: rounds = [],
    isLoading,
    isFetching,
    isError,
    refetch,
  } = useGetRoundsQuery(undefined, {
    pollingInterval: 5000,
  });

  const stats = useMemo(
    () => ({
      total: rounds.length,
      creating: rounds.filter((round) => round.status === "CREATING").length,
      completed: rounds.filter((round) => round.status === "COMPLETED").length,
      failed: rounds.filter((round) => round.status === "FAILED").length,
    }),
    [rounds],
  );

  const handleCreated = (round: RoundDto) => {
    setShowCreateDialog(false);
    setSelectedRoundId(round.id);
    refetch();
  };

  return (
    <main className="round-management">
      <div className="round-page-header">
        <div>
          <div className="round-page-eyebrow">
            <Database size={15} />
            DATA MANAGEMENT
          </div>

          <h1>Rounds</h1>

          <p>
            Manage surveillance rounds and create database snapshots from
            validated data.
          </p>
        </div>

        <button
          type="button"
          className="primary-button"
          onClick={() => setShowCreateDialog(true)}
        >
          <Plus size={17} />
          Create round
        </button>
      </div>

      <section className="round-stats">
        <div className="round-stat-card">
          <div className="round-stat-icon">
            <Database size={19} />
          </div>

          <div>
            <span>Total rounds</span>
            <strong>{stats.total}</strong>
          </div>
        </div>

        <div className="round-stat-card">
          <div className="round-stat-icon creating">
            <Activity size={19} />
          </div>

          <div>
            <span>In progress</span>
            <strong>{stats.creating}</strong>
          </div>
        </div>

        <div className="round-stat-card">
          <div className="round-stat-icon completed">
            <CheckCircle2 size={19} />
          </div>

          <div>
            <span>Completed</span>
            <strong>{stats.completed}</strong>
          </div>
        </div>

        <div className="round-stat-card">
          <div className="round-stat-icon failed">
            <AlertTriangle size={19} />
          </div>

          <div>
            <span>Failed</span>
            <strong>{stats.failed}</strong>
          </div>
        </div>
      </section>

      <section className="round-list-section">
        <div className="round-list-header">
          <div>
            <h2>All rounds</h2>

            <p>
              View round information and monitor database snapshot progress.
            </p>
          </div>

          <button
            type="button"
            className="icon-text-button"
            onClick={() => refetch()}
            disabled={isFetching}
          >
            <RefreshCw size={15} className={isFetching ? "spin" : ""} />
            Refresh
          </button>
        </div>

        {isLoading ? (
          <div className="round-empty-state">
            <RefreshCw className="spin" size={24} />
            <p>Loading rounds...</p>
          </div>
        ) : isError ? (
          <div className="round-empty-state error-state">
            <AlertCircle size={25} />

            <h3>Unable to load rounds</h3>

            <p>There was a problem retrieving the round list.</p>

            <button
              type="button"
              className="secondary-button"
              onClick={() => refetch()}
            >
              Try again
            </button>
          </div>
        ) : rounds.length === 0 ? (
          <div className="round-empty-state">
            <div className="empty-state-icon">
              <Database size={26} />
            </div>

            <h3>No rounds created yet</h3>

            <p>
              Create your first round to generate a snapshot of the current
              surveillance data.
            </p>

            <button
              type="button"
              className="primary-button"
              onClick={() => setShowCreateDialog(true)}
            >
              <Plus size={16} />
              Create first round
            </button>
          </div>
        ) : (
          <div className="round-table-wrapper">
            <table className="round-table">
              <thead>
                <tr>
                  <th>Round</th>
                  <th>Database</th>
                  <th>Status</th>
                  <th>Created</th>
                  <th>Completed</th>
                  <th></th>
                </tr>
              </thead>

              <tbody>
                {rounds.map((round) => (
                  <tr key={round.id}>
                    <td>
                      <div className="round-name-cell">
                        <strong>{round.roundName}</strong>

                        <span>Round #{round.id}</span>
                      </div>
                    </td>

                    <td>
                      <span className="database-name">
                        {round.databaseName}
                      </span>
                    </td>

                    <td>
                      <StatusBadge status={round.status} />
                    </td>

                    <td>{formatDate(round.createdAt)}</td>

                    <td>{formatDate(round.completedAt)}</td>

                    <td>
                      <button
                        type="button"
                        className="icon-text-button view-round-button"
                        onClick={() => setSelectedRoundId(round.id)}
                        title="View round details"
                      >
                        <Eye size={16} />
                        View
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {showCreateDialog && (
        <CreateRoundDialog
          onClose={() => setShowCreateDialog(false)}
          onCreated={handleCreated}
        />
      )}

      {selectedRoundId !== null && (
        <RoundDetailsPanel
          roundId={selectedRoundId}
          onClose={() => setSelectedRoundId(null)}
        />
      )}
    </main>
  );
}
