import { useState } from "react";
import type { FormEvent } from "react";
import { Link, useSearchParams } from "react-router-dom";

import { resetPassword } from "../../services/auth.service";
import { errorMessage } from "../../lib/errorMessage";

export default function ResetPasswordPage() {
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token") ?? "";

    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [isPending, setIsPending] = useState(false);
    const [isDone, setIsDone] = useState(false);

    async function submit(e: FormEvent) {
        e.preventDefault();

        if (password.length < 8) {
            setError("Password must be at least 8 characters.");
            return;
        }

        if (password !== confirmPassword) {
            setError("Passwords do not match.");
            return;
        }

        setError(null);
        setIsPending(true);

        try {
            await resetPassword(token, password);
            setIsDone(true);
        } catch (err) {
            setError(errorMessage(err, "This reset link is invalid or has expired."));
        } finally {
            setIsPending(false);
        }
    }

    if (!token) {
        return (
            <>
                <div className="auth-header">
                    <h1 className="auth-title">Invalid reset link</h1>
                    <p className="auth-subtitle">
                        This link is missing its token. Request a new one to continue.
                    </p>
                </div>

                <p className="auth-footer">
                    <Link className="auth-link" to="/forgot-password">
                        Request a new link
                    </Link>
                </p>
            </>
        );
    }

    if (isDone) {
        return (
            <>
                <div className="auth-header">
                    <h1 className="auth-title">Password updated</h1>
                    <p className="auth-subtitle">
                        Your password has been changed and all other sessions were signed
                        out. You can sign in now.
                    </p>
                </div>

                <p className="auth-footer">
                    <Link className="auth-link" to="/login">Go to sign in</Link>
                </p>
            </>
        );
    }

    return (
        <>
            <div className="auth-header">
                <h1 className="auth-title">Choose a new password</h1>
                <p className="auth-subtitle">
                    Signing in elsewhere will require the new password.
                </p>
            </div>

            <form className="form" onSubmit={submit} noValidate>
                {error && (
                    <div className="form-error" role="alert">
                        <span>⚠</span>
                        <span>{error}</span>
                    </div>
                )}

                <div className="field">
                    <label className="field__label" htmlFor="reset-password">
                        New Password<span className="field__req">*</span>
                    </label>
                    <input
                        id="reset-password"
                        className="input"
                        type="password"
                        autoComplete="new-password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="At least 8 characters"
                        autoFocus
                    />
                </div>

                <div className="field">
                    <label className="field__label" htmlFor="reset-confirm">
                        Confirm Password<span className="field__req">*</span>
                    </label>
                    <input
                        id="reset-confirm"
                        className="input"
                        type="password"
                        autoComplete="new-password"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        placeholder="Repeat your new password"
                    />
                </div>

                <div className="form-actions form-actions--stretch">
                    <button
                        type="submit"
                        className="btn btn--primary btn--block"
                        disabled={isPending}
                    >
                        {isPending ? "Updating…" : "Update Password"}
                    </button>
                </div>
            </form>

            <p className="auth-footer">
                <Link className="auth-link" to="/login">Back to sign in</Link>
            </p>
        </>
    );
}
