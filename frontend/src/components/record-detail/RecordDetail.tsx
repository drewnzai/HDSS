import type { ReactNode } from "react";
import { ArrowLeft } from "lucide-react";
import PageContainer from "../PageContainer";
import "./record-detail.css"

export interface RecordDetailField {
    label: string;
    value: ReactNode;
}

export interface RecordDetailSection {
    eyebrow?: string;
    title?: string;
    fields: RecordDetailField[];
}

interface RecordDetailProps {
    eyebrow?: string;
    title: string;
    description?: string;

    sections: RecordDetailSection[];

    isLoading?: boolean;
    loadingMessage?: string;

    error?: unknown;
    errorMessage?: string;

    backLabel?: string;
    onBack: () => void;
}

function RecordDetail({
    eyebrow,
    title,
    description,
    sections,
    isLoading = false,
    loadingMessage = "Loading...",
    error,
    errorMessage = "Couldn't find that record.",
    backLabel = "Back",
    onBack,
}: RecordDetailProps) {
    if (isLoading) {
        return (
            <PageContainer>
                <div className="record-detail__state">
                    <div className="record-detail__loading">
                        <span
                            className="record-detail__spinner"
                            aria-hidden="true"
                        />
                        <span>{loadingMessage}</span>
                    </div>
                </div>
            </PageContainer>
        );
    }

    if (error) {
        return (
            <PageContainer>
                <div className="record-detail__state">
                    <div className="record-detail__error">
                        <span className="record-detail__eyebrow">
                            Record unavailable
                        </span>

                        <h1 className="record-detail__state-title">
                            {errorMessage}
                        </h1>

                        <button
                            type="button"
                            className="record-detail__back"
                            onClick={onBack}
                        >
                            <ArrowLeft size={17} aria-hidden="true" />
                            {backLabel}
                        </button>
                    </div>
                </div>
            </PageContainer>
        );
    }

    return (
        <PageContainer>
            <div className="record-detail">
                <button
                    type="button"
                    className="record-detail__back"
                    onClick={onBack}
                >
                    <ArrowLeft size={17} aria-hidden="true" />
                    {backLabel}
                </button>

                <header className="record-detail__header">
                    {eyebrow && (
                        <span className="record-detail__eyebrow">
                            {eyebrow}
                        </span>
                    )}

                    <h1 className="record-detail__title">{title}</h1>

                    {description && (
                        <p className="record-detail__description">
                            {description}
                        </p>
                    )}
                </header>

                <div className="record-detail__sections">
                    {sections.map((section, sectionIndex) => (
                        <section
                            key={section.title ?? section.eyebrow ?? sectionIndex}
                            className="record-detail__section"
                        >
                            {(section.eyebrow || section.title) && (
                                <header className="record-detail__section-header">
                                    {section.eyebrow && (
                                        <span className="record-detail__eyebrow">
                                            {section.eyebrow}
                                        </span>
                                    )}

                                    {section.title && (
                                        <h2 className="record-detail__section-title">
                                            {section.title}
                                        </h2>
                                    )}
                                </header>
                            )}

                            <div className="record-detail__fields">
                                {section.fields.map((field) => (
                                    <div
                                        key={field.label}
                                        className="record-detail__field"
                                    >
                                        <span className="record-detail__field-label">
                                            {field.label}
                                        </span>

                                        <div className="record-detail__field-value">
                                            {field.value}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </section>
                    ))}
                </div>
            </div>
        </PageContainer>
    );
}

export default RecordDetail;