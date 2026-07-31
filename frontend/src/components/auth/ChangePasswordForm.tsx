import { useState } from "react";
import type { FormEvent } from "react";

import { changePassword } from "../../services/auth.service";
import { errorMessage } from "../../lib/errorMessage";
import { useAuth } from "../../hooks/useAuth";

export default function ChangePasswordForm() {
    const { logout } = useAuth();

    const [currentPassword, setCurrentPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [isPending, setIsPending] = useState(false);
    const [isDone, setIsDone] = useState(false);

    async function submit(e: FormEvent) {
        e.preventDefault();

        if (!currentPassword || !newPassword) {
            setError("Both the current and new password are required.");
            return;
        }

        if (newPassword.length < 8) {
            setError("New password must be at least 8 characters.");
            return;
        }

        if (newPassword !== confirmPassword) {
            setError("Passwords do not match.");
            return;
        }

        setError(null);
        setIsPending(true);

        try {
            await changePassword(currentPassword, newPassword);

            setIsDone(true);
            setCurrentPassword("");
            setNewPassword("");
            setConfirmPassword("");

            // The server revoked every refresh token, so this session is
            // already dead — sign out cleanly instead of failing later.
            await logout();
        } catch (err) {
            setError(errorMessage(err));
        } finally {
            setIsPending(false);
        }
    }

    if (isDone) {
        return (
            <div className="state">
                <div className="state__icon">✅</div>
                <div className="state__desc">
                    Password updated. All sessions were signed out — please sign in
                    again with your new password.
                </div>
            </div>
        );
    }

    return (
        <form className="form" onSubmit={submit} noValidate>
            {error && (
                <div className="form-error" role="alert">
                    <span>⚠</span>
                    <span>{error}</span>
                </div>
            )}

            <div className="form-warning">
                <span>ℹ</span>
                <span>Changing your password signs you out of all devices.</span>
            </div>

            <div className="field">
                <label className="field__label" htmlFor="current-password">
                    Current Password<span className="field__req">*</span>
                </label>
                <input
                    id="current-password"
                    className="input"
                    type="password"
                    autoComplete="current-password"
                    value={currentPassword}
                    onChange={(e) => setCurrentPassword(e.target.value)}
                />
            </div>

            <div className="field">
                <label className="field__label" htmlFor="new-password">
                    New Password<span className="field__req">*</span>
                </label>
                <input
                    id="new-password"
                    className="input"
                    type="password"
                    autoComplete="new-password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    placeholder="At least 8 characters"
                />
            </div>

            <div className="field">
                <label className="field__label" htmlFor="confirm-new-password">
                    Confirm New Password<span className="field__req">*</span>
                </label>
                <input
                    id="confirm-new-password"
                    className="input"
                    type="password"
                    autoComplete="new-password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                />
            </div>

            <div className="form-actions">
                <button
                    type="submit"
                    className="btn btn--primary"
                    disabled={isPending}
                >
                    {isPending ? "Updating…" : "Update Password"}
                </button>
            </div>
        </form>
    );
}
