import { useState } from "react";
import type { FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";

import { useAuth } from "../../hooks/useAuth";
import { errorMessage } from "../../lib/errorMessage";
import GoogleSignInButton from "../../components/auth/GoogleSignInButton";

export default function RegisterPage() {
    const { register, loginWithGoogle } = useAuth();
    const navigate = useNavigate();

    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [isPending, setIsPending] = useState(false);

    async function submit(e: FormEvent) {
        e.preventDefault();

        if (!name.trim() || !email.trim() || !password) {
            setError("Name, email and password are required.");
            return;
        }

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
            await register(email.trim(), password, name.trim());
            navigate("/dashboard", { replace: true });
        } catch (err) {
            setError(errorMessage(err));
        } finally {
            setIsPending(false);
        }
    }

    async function handleGoogleCredential(idToken: string) {
        setError(null);
        setIsPending(true);

        try {
            await loginWithGoogle(idToken);
            navigate("/dashboard", { replace: true });
        } catch (err) {
            setError(errorMessage(err, "Google sign-in failed."));
        } finally {
            setIsPending(false);
        }
    }

    return (
        <>
            <div className="auth-header">
                <h1 className="auth-title">Create your account</h1>
                <p className="auth-subtitle">Start tracking your money in a minute.</p>
            </div>

            <form className="form" onSubmit={submit} noValidate>
                {error && (
                    <div className="form-error" role="alert">
                        <span>⚠</span>
                        <span>{error}</span>
                    </div>
                )}

                <div className="field">
                    <label className="field__label" htmlFor="register-name">
                        Name<span className="field__req">*</span>
                    </label>
                    <input
                        id="register-name"
                        className="input"
                        autoComplete="name"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        placeholder="Your name"
                        maxLength={100}
                        autoFocus
                    />
                </div>

                <div className="field">
                    <label className="field__label" htmlFor="register-email">
                        Email<span className="field__req">*</span>
                    </label>
                    <input
                        id="register-email"
                        className="input"
                        type="email"
                        autoComplete="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        placeholder="you@example.com"
                        maxLength={255}
                    />
                </div>

                <div className="field">
                    <label className="field__label" htmlFor="register-password">
                        Password<span className="field__req">*</span>
                    </label>
                    <input
                        id="register-password"
                        className="input"
                        type="password"
                        autoComplete="new-password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="At least 8 characters"
                    />
                    <span className="field__hint">Use 8–72 characters.</span>
                </div>

                <div className="field">
                    <label className="field__label" htmlFor="register-confirm">
                        Confirm Password<span className="field__req">*</span>
                    </label>
                    <input
                        id="register-confirm"
                        className="input"
                        type="password"
                        autoComplete="new-password"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        placeholder="Repeat your password"
                    />
                </div>

                <div className="form-actions form-actions--stretch">
                    <button
                        type="submit"
                        className="btn btn--primary btn--block"
                        disabled={isPending}
                    >
                        {isPending ? "Creating account…" : "Create Account"}
                    </button>
                </div>
            </form>

            <GoogleSignInButton
                onCredential={handleGoogleCredential}
                disabled={isPending}
            />

            <p className="auth-footer">
                Already have an account? <Link className="auth-link" to="/login">Sign in</Link>
            </p>
        </>
    );
}
