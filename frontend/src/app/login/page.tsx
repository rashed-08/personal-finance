import { useState } from "react";
import type { FormEvent } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";

import { useAuth } from "../../hooks/useAuth";
import { errorMessage } from "../../lib/errorMessage";
import GoogleSignInButton from "../../components/auth/GoogleSignInButton";

interface LocationState {
    from?: { pathname: string };
}

export default function LoginPage() {
    const { login, loginWithGoogle } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [isPending, setIsPending] = useState(false);

    const redirectTo =
        (location.state as LocationState | null)?.from?.pathname ?? "/dashboard";

    async function submit(e: FormEvent) {
        e.preventDefault();

        if (!email.trim() || !password) {
            setError("Email and password are required.");
            return;
        }

        setError(null);
        setIsPending(true);

        try {
            await login(email.trim(), password);
            navigate(redirectTo, { replace: true });
        } catch (err) {
            setError(errorMessage(err, "Invalid email or password."));
        } finally {
            setIsPending(false);
        }
    }

    async function handleGoogleCredential(idToken: string) {
        setError(null);
        setIsPending(true);

        try {
            await loginWithGoogle(idToken);
            navigate(redirectTo, { replace: true });
        } catch (err) {
            setError(errorMessage(err, "Google sign-in failed."));
        } finally {
            setIsPending(false);
        }
    }

    return (
        <>
            <div className="auth-header">
                <h1 className="auth-title">Welcome back</h1>
                <p className="auth-subtitle">Sign in to continue to your finances.</p>
            </div>

            <form className="form" onSubmit={submit} noValidate>
                {error && (
                    <div className="form-error" role="alert">
                        <span>⚠</span>
                        <span>{error}</span>
                    </div>
                )}

                <div className="field">
                    <label className="field__label" htmlFor="login-email">
                        Email<span className="field__req">*</span>
                    </label>
                    <input
                        id="login-email"
                        className="input"
                        type="email"
                        autoComplete="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        placeholder="you@example.com"
                        autoFocus
                    />
                </div>

                <div className="field">
                    <label className="field__label" htmlFor="login-password">
                        Password<span className="field__req">*</span>
                    </label>
                    <input
                        id="login-password"
                        className="input"
                        type="password"
                        autoComplete="current-password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="••••••••"
                    />
                    <Link className="auth-link auth-link--field" to="/forgot-password">
                        Forgot password?
                    </Link>
                </div>

                <div className="form-actions form-actions--stretch">
                    <button
                        type="submit"
                        className="btn btn--primary btn--block"
                        disabled={isPending}
                    >
                        {isPending ? "Signing in…" : "Sign In"}
                    </button>
                </div>
            </form>

            <GoogleSignInButton
                onCredential={handleGoogleCredential}
                disabled={isPending}
            />

            <p className="auth-footer">
                Don&apos;t have an account? <Link className="auth-link" to="/register">Create one</Link>
            </p>
        </>
    );
}
