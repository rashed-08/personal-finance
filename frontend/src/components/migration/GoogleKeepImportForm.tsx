import { useState } from "react";
import type { FormEvent } from "react";

import { useImportGoogleKeep } from "../../hooks/useMigration";
import GoogleKeepImportResultPanel from "./GoogleKeepImportResultPanel";

function errorMessage(err: unknown): string {
    const detail = (err as { response?: { data?: { detail?: string } } })
        ?.response?.data?.detail;
    return detail ?? "Something went wrong. Please try again.";
}

export default function GoogleKeepImportForm() {
    const [content, setContent] = useState("");
    const mutation = useImportGoogleKeep();

    function handleSubmit(e: FormEvent) {
        e.preventDefault();

        if (!content.trim()) {
            return;
        }

        mutation.mutate(content);
    }

    return (
        <div className="card" style={{ padding: 20, display: "flex", flexDirection: "column", gap: 16 }}>
            <form className="form" onSubmit={handleSubmit}>
                <div className="field">
                    <label className="field__label" htmlFor="keep-content">
                        Google Keep export text<span className="field__req">*</span>
                    </label>
                    <textarea
                        id="keep-content"
                        className="textarea"
                        style={{ minHeight: 320, fontFamily: "var(--mono)", fontSize: 13 }}
                        placeholder={"০৪-২৬\n=========\n\nবাজার ১০৭৩৫ (৯০০+৭২৫+...)\n\n=৭৫৮০০"}
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                    />
                    <span className="field__hint">
                        Paste one or more months. Re-running the same text is safe — exact duplicates
                        (same salary cycle, category, and amount) are skipped automatically.
                    </span>
                </div>

                {mutation.isError && (
                    <div className="form-error" role="alert">
                        <span>⚠</span>
                        <span>{errorMessage(mutation.error)}</span>
                    </div>
                )}

                <div className="form-actions">
                    <button
                        type="submit"
                        className="btn btn--primary"
                        disabled={mutation.isPending || !content.trim()}
                    >
                        {mutation.isPending ? "Importing…" : "Import"}
                    </button>
                </div>
            </form>

            {mutation.isSuccess && <GoogleKeepImportResultPanel result={mutation.data} />}
        </div>
    );
}
