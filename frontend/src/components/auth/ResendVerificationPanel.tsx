import { useState } from "react";

import { resendVerification } from "../../services/auth.service";
import { errorMessage } from "../../lib/errorMessage";

export default function ResendVerificationPanel({ email }: { email: string }) {
    const [isPending, setIsPending] = useState(false);
    const [isSent, setIsSent] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function resend() {
        setError(null);
        setIsPending(true);

        try {
            await resendVerification(email);
            setIsSent(true);
        } catch (err) {
            setError(errorMessage(err));
        } finally {
            setIsPending(false);
        }
    }

    return (
        <div className="inline-panel">
            {error && (
                <div className="form-error" role="alert">
                    <span>⚠</span>
                    <span>{error}</span>
                </div>
            )}

            <div className="form-warning">
                <span>ℹ</span>
                <span>
                    {isSent
                        ? "Verification email sent — check your inbox."
                        : "Your email address is not verified yet."}
                </span>
            </div>

            {!isSent && (
                <div className="inline-panel__actions">
                    <button
                        type="button"
                        className="btn btn--ghost btn--sm"
                        onClick={resend}
                        disabled={isPending}
                    >
                        {isPending ? "Sending…" : "Resend verification email"}
                    </button>
                </div>
            )}
        </div>
    );
}
