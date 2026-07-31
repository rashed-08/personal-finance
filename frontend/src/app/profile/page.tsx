import { useAuth } from "../../hooks/useAuth";
import ChangePasswordForm from "../../components/auth/ChangePasswordForm";
import ResendVerificationPanel from "../../components/auth/ResendVerificationPanel";

export default function ProfilePage() {
    const { user } = useAuth();

    if (!user) {
        return null;
    }

    return (
        <>
            <div className="page-header">
                <div>
                    <h1 className="page-header__title">Profile</h1>
                    <p className="page-header__subtitle">
                        Your account details and password.
                    </p>
                </div>
            </div>

            <div className="card profile-card">
                <div className="profile-row">
                    <span className="profile-row__label">Name</span>
                    <span className="profile-row__value">{user.name}</span>
                </div>

                <div className="profile-row">
                    <span className="profile-row__label">Email</span>
                    <span className="profile-row__value">
                        {user.email}
                        {user.emailVerified ? (
                            <span className="pill pill--active">
                                <span className="pill__dot" />
                                Verified
                            </span>
                        ) : (
                            <span className="pill pill--inactive">
                                <span className="pill__dot" />
                                Unverified
                            </span>
                        )}
                    </span>
                </div>

                <div className="profile-row">
                    <span className="profile-row__label">Role</span>
                    <span className="profile-row__value">{user.role}</span>
                </div>

                <div className="profile-row">
                    <span className="profile-row__label">Member since</span>
                    <span className="profile-row__value">
                        {new Date(user.createdAt).toLocaleDateString()}
                    </span>
                </div>
            </div>

            {!user.emailVerified && <ResendVerificationPanel email={user.email} />}

            <div className="dashboard-section">
                <h2 className="dashboard-section__title">Change Password</h2>

                <div className="card">
                    <ChangePasswordForm />
                </div>
            </div>
        </>
    );
}
