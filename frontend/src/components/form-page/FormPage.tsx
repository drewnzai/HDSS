import type { FormEvent, ReactNode } from "react";
import { ArrowLeft } from "lucide-react";
import PageContainer from "../PageContainer"
import "./form-page.css";

export interface FormPageSection {
    eyebrow?: string;
    title?: string;
    children: ReactNode;
}

interface FormPageProps {
    eyebrow?: string;
    title: string;
    description?: string;

    sections: FormPageSection[];

    error?: ReactNode;

    isLoading?: boolean;
    submitLabel: string;
    loadingLabel?: string;

    cancelLabel?: string;
    onCancel: () => void;

    onSubmit: (event: FormEvent<HTMLFormElement>) => void;
    after?: ReactNode;
}

function FormPage({
    eyebrow,
    title,
    description,
    sections,
    error,
    isLoading = false,
    submitLabel,
    loadingLabel = "Saving…",
    cancelLabel = "Cancel",
    onCancel,
    onSubmit,
    after
}: FormPageProps) {
    return (
        <PageContainer>
            <div className="form-page">
                <button
                    type="button"
                    className="form-page__back"
                    onClick={onCancel}
                >
                    <ArrowLeft size={17} aria-hidden="true" />
                    {cancelLabel}
                </button>

                <header className="form-page__header">
                    {eyebrow && (
                        <span className="form-page__eyebrow">
                            {eyebrow}
                        </span>
                    )}

                    <h1 className="form-page__title">{title}</h1>

                    {description && (
                        <p className="form-page__description">
                            {description}
                        </p>
                    )}
                </header>

                <form
                    className="form-page__form"
                    onSubmit={onSubmit}
                    noValidate
                >
                    <div className="form-page__sections">
                        {sections.map((section, index) => (
                            <section
                                key={
                                    section.title ??
                                    section.eyebrow ??
                                    index
                                }
                                className="form-page__section"
                            >
                                {(section.eyebrow || section.title) && (
                                    <header className="form-page__section-header">
                                        {section.eyebrow && (
                                            <span className="form-page__eyebrow">
                                                {section.eyebrow}
                                            </span>
                                        )}

                                        {section.title && (
                                            <h2 className="form-page__section-title">
                                                {section.title}
                                            </h2>
                                        )}
                                    </header>
                                )}

                                {section.children}
                            </section>
                        ))}
                    </div>

                    {error && (
                        <div
                            className="form-page__error"
                            role="alert"
                            aria-live="assertive"
                        >
                            {error}
                        </div>
                    )}

                    <div className="form-page__actions">
                        <button
                            type="submit"
                            className="form-page__submit"
                            disabled={isLoading}
                        >
                            {isLoading ? loadingLabel : submitLabel}
                        </button>

                        <button
                            type="button"
                            className="form-page__cancel"
                            onClick={onCancel}
                            disabled={isLoading}
                        >
                            Cancel
                        </button>
                    </div>
                </form>
                {after}
            </div>
        </PageContainer>
    );
}

export default FormPage;