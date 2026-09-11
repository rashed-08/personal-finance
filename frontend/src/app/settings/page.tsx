import { useSettings } from "../../hooks/useSettings";
import SettingsForm from "../../components/settings/SettingsForm";

export default function SettingsPage() {
    const { data = [], isLoading, error } = useSettings();

    return (
        <>
            <div className="page-header">
                <div>
                    <h1 className="page-header__title">Settings</h1>
                    <p className="page-header__subtitle">
                        Application configuration. Changes affect future behaviour only —
                        historical reports and recorded transactions are never rewritten.
                    </p>
                </div>
            </div>

            {isLoading ? (
                <div className="card">
                    <div className="state">
                        <div className="spinner" />
                        <div className="state__desc">Loading settings…</div>
                    </div>
                </div>
            ) : error ? (
                <div className="card">
                    <div className="state">
                        <div className="state__icon">⚠</div>
                        <div className="state__title">Couldn’t load settings</div>
                        <div className="state__desc">
                            Check that the backend is running on
                            <code> localhost:8080</code> and try again.
                        </div>
                    </div>
                </div>
            ) : data.length === 0 ? (
                <div className="card">
                    <div className="state">
                        <div className="state__icon">⚙</div>
                        <div className="state__title">No settings found</div>
                        <div className="state__desc">
                            Settings are seeded by database migration. Run the Flyway
                            migrations and reload.
                        </div>
                    </div>
                </div>
            ) : (
                <SettingsForm settings={data} />
            )}
        </>
    );
}
