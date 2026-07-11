import { useEffect } from "react";
import type { ReactNode } from "react";

interface Props {
    open: boolean;
    title: string;
    children: ReactNode;
    onClose: () => void;
}

export default function AccountDialog({
    open,
    title,
    children,
    onClose,
}: Props) {
    useEffect(() => {
        if (!open) {
            return;
        }

        function onKeyDown(e: KeyboardEvent) {
            if (e.key === "Escape") {
                onClose();
            }
        }

        document.addEventListener("keydown", onKeyDown);
        return () => document.removeEventListener("keydown", onKeyDown);
    }, [open, onClose]);

    if (!open) {
        return null;
    }

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div
                className="modal"
                role="dialog"
                aria-modal="true"
                aria-label={title}
                onClick={(e) => e.stopPropagation()}
            >
                <div className="modal__header">
                    <h2 className="modal__title">{title}</h2>
                    <button
                        type="button"
                        className="modal__close"
                        aria-label="Close"
                        onClick={onClose}
                    >
                        ✕
                    </button>
                </div>

                <div className="modal__body">{children}</div>
            </div>
        </div>
    );
}
