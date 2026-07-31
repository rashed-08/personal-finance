import { useState } from "react";
import type { FormEvent } from "react";
import { Link } from "react-router-dom";

import { forgotPassword } from "../../services/auth.service";
import { errorMessage } from "../../lib/errorMessage";

export default function ForgotPasswordPage() {
    const [email, setEmail] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [isPending, setIsPending] = useState(false);
    const [isSent, setIsSent] = useState(false);

    async function submit(e: FormEvent) {
        e.preventDefault();

        if (!email.trim()) {
            setError("Email is required.");
            return;
        }

        setError(null);
        setIsPending(true);

        try {
            await forgotPassword(email.trim());
            setIsSent(true);
        } catch (err) {
            setError(errorMessage(err));
        } finally {
            setIsPending(false);
        }
    }

    // The backend answers identically whether or not the account exists,
    // so the confirmation is deliberately non-committal.
    if (isSent) {
        return (
            <>
                <div className="auth-header">
                    <h1 className="auth-title">Check your email</h1>
                    <p className="auth-subtitle">
                        If an account exists for <strong>{email.trim()}</strong>, we&apos;ve
                        sent a password reset link. It expires in one hour.
                    </p>
                </div>

                <p className="auth-footer">
                    <Link className="auth-link" to="/login">Back to sign in</Link>
                </p>
            </>
        );
    }

    return (
        <>
            <div className="auth-header">
                <h1 className="auth-title">Forgot your password?</h1>
                <p className="auth-subtitle">
                    Enter your email and we&apos;ll send you a reset link.
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
                    <label className="field__label" htmlFor="forgot-email">
                        Email<span className="field__req">*</span>
                    </label>
                    <input
                        id="forgot-email"
                        className="input"
                        type="email"
                        autoComplete="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        placeholder="you@example.com"
                        autoFocus
                    />
                </div>

                <div className="form-actions form-actions--stretch">
                    <button
                        type="submit"
                        className="btn btn--primary btn--block"
                        disabled={isPending}
                    >
                        {isPending ? "Sending…" : "Send Reset Link"}
                    </button>
                </div>
            </form>

            <p className="auth-footer">
                <Link className="auth-link" to="/login">Back to sign in</Link>
            </p>
        </>
    );
}
