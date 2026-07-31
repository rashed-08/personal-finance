import { useEffect, useRef, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";

import { verifyEmail } from "../../services/auth.service";
import { errorMessage } from "../../lib/errorMessage";

type Status = "verifying" | "success" | "error";

export default function VerifyEmailPage() {
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token") ?? "";

    const [status, setStatus] = useState<Status>(token ? "verifying" : "error");
    const [message, setMessage] = useState(
        token ? "" : "This verification link is missing its token.",
    );

    // StrictMode double-invokes effects; the token is single-use, so the
    // second call would report a spurious failure.
    const hasRun = useRef(false);

    useEffect(() => {
        if (!token || hasRun.current) {
            return;
        }

        hasRun.current = true;

        verifyEmail(token)
            .then(() => setStatus("success"))
            .catch((err) => {
                setStatus("error");
                setMessage(
                    errorMessage(err, "This verification link is invalid or has expired."),
                );
            });
    }, [token]);

    if (status === "verifying") {
        return (
            <div className="auth-header">
                <div className="spinner" />
                <h1 className="auth-title">Verifying your email…</h1>
            </div>
        );
    }

    if (status === "success") {
        return (
            <>
                <div className="auth-header">
                    <h1 className="auth-title">Email verified</h1>
                    <p className="auth-subtitle">
                        Thanks — your email address is confirmed.
                    </p>
                </div>

                <p className="auth-footer">
                    <Link className="auth-link" to="/dashboard">Go to dashboard</Link>
                </p>
            </>
        );
    }

    return (
        <>
            <div className="auth-header">
                <h1 className="auth-title">Verification failed</h1>
                <p className="auth-subtitle">{message}</p>
            </div>

            <p className="auth-footer">
                <Link className="auth-link" to="/login">Back to sign in</Link>
            </p>
        </>
    );
}
