import { AlertTriangle, X } from "lucide-react";
import type { ReactNode } from "react";
import "./confirm-dialog.css";

interface ConfirmDialogProps {
    open: boolean;
    title: string;
    description: ReactNode;
    confirmLoadingLabel?: string;

    confirmLabel?: string;
    cancelLabel?: string;

    isLoading?: boolean;

    onConfirm: () => void;
    onCancel: () => void;
}

function ConfirmDialog({
    open,
    title,
    description,
    confirmLabel = "Confirm",
    cancelLabel = "Cancel",
    confirmLoadingLabel = "Loading",
    isLoading = false,
    onConfirm,
    onCancel,
}: ConfirmDialogProps) {
    if (!open) {
        return null;
    }

    return (
        <div
            className="confirm-dialog__backdrop"
            role="presentation"
            onMouseDown={(event) => {
                if (event.target === event.currentTarget && !isLoading) {
                    onCancel();
                }
            }}
        >
            <div
                className="confirm-dialog"
                role="alertdialog"
                aria-modal="true"
                aria-labelledby="confirm-dialog-title"
                aria-describedby="confirm-dialog-description"
            >
                <div className="confirm-dialog__icon" aria-hidden="true">
                    <AlertTriangle size={20} />
                </div>

                <div className="confirm-dialog__content">
                    <div className="confirm-dialog__header">
                        <h2
                            id="confirm-dialog-title"
                            className="confirm-dialog__title"
                        >
                            {title}
                        </h2>

                        <button
                            type="button"
                            className="confirm-dialog__close"
                            onClick={onCancel}
                            disabled={isLoading}
                            aria-label="Close dialog"
                        >
                            <X size={18} aria-hidden="true" />
                        </button>
                    </div>

                    <div
                        id="confirm-dialog-description"
                        className="confirm-dialog__description"
                    >
                        {description}
                    </div>

                    <div className="confirm-dialog__actions">
                        <button
                            type="button"
                            className="confirm-dialog__cancel"
                            onClick={onCancel}
                            disabled={isLoading}
                        >
                            {cancelLabel}
                        </button>

                        <button
                            type="button"
                            className="confirm-dialog__confirm"
                            onClick={onConfirm}
                            disabled={isLoading}
                        >
                            {isLoading ? confirmLoadingLabel : confirmLabel}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default ConfirmDialog;