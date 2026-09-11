import {
    useBeginGoogleDriveAuthorization,
    useDisconnectGoogleDrive,
} from "../../hooks/useBackups";
import { errorMessage } from "../../lib/errorMessage";
import type { GoogleDriveConnection } from "../../types/backup";

interface Props {
    connection: GoogleDriveConnection;
}

export default function GoogleDriveCard({ connection }: Props) {
    const authorize = useBeginGoogleDriveAuthorization();
    const disconnect = useDisconnectGoogleDrive();

    function handleConnect() {
        authorize.mutate(undefined, {
            onSuccess: (authorizationUrl) => {
                // A full navigation, not a popup: Google's consent screen
                // refuses to render inside an iframe, and the callback
                // route brings the user straight back.
                window.location.assign(authorizationUrl);
            },
        });
    }

    return (
        <div className="card" style={{ padding: 20 }}>
            <div className="list-row">
                <div className="list-row__main">
                    <div className="list-row__title">Google Drive</div>

                    <div className="list-row__subtitle">
                        {connection.connected
                            ? `Connected as ${connection.accountEmail ?? "a Google account"} · backups go to ${connection.folderName}/backups`
                            : (connection.detail ??
                              "Not connected.")}
                    </div>
                </div>

                {connection.configured && (
                    <div className="row-actions">
                        {connection.connected ? (
                            <>
                                <button
                                    type="button"
                                    className="btn btn--ghost btn--sm"
                                    disabled={authorize.isPending}
                                    onClick={handleConnect}
                                >
                                    Reconnect
                                </button>

                                <button
                                    type="button"
                                    className="btn btn--danger btn--sm"
                                    disabled={disconnect.isPending}
                                    onClick={() => disconnect.mutate()}
                                >
                                    {disconnect.isPending ? "Disconnecting…" : "Disconnect"}
                                </button>
                            </>
                        ) : (
                            <button
                                type="button"
                                className="btn btn--primary btn--sm"
                                disabled={authorize.isPending}
                                onClick={handleConnect}
                            >
                                {authorize.isPending ? "Redirecting…" : "Connect Google Drive"}
                            </button>
                        )}
                    </div>
                )}
            </div>

            {connection.connected && (
                <p className="field__hint" style={{ marginTop: 12 }}>
                    Uploaded archives contain your complete financial history. The app can
                    only see files it created in your Drive, and you can revoke access at
                    any time from this page or from your Google account.
                </p>
            )}

            {(authorize.isError || disconnect.isError) && (
                <div className="form-error" role="alert" style={{ marginTop: 12 }}>
                    <span>⚠</span>
                    <span>{errorMessage(authorize.error ?? disconnect.error)}</span>
                </div>
            )}
        </div>
    );
}
