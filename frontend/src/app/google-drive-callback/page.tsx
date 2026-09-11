import { useEffect, useRef } from "react";
import { Link, useSearchParams } from "react-router-dom";

import { useCompleteGoogleDriveAuthorization } from "../../hooks/useBackups";
import { errorMessage } from "../../lib/errorMessage";

/**
 * Where Google sends the browser after the consent screen.
 *
 * The route only forwards {@code code} and {@code state} to the backend,
 * which is what actually exchanges the code and stores the credential.
 * The code is single-use, so the exchange is fired exactly once even
 * though React may mount this twice in development.
 */
export default function GoogleDriveCallbackPage() {
    const [params] = useSearchParams();
    const complete = useCompleteGoogleDriveAuthorization();

    const code = params.get("code");
    const state = params.get("state");

    // Google's own refusal ("access_denied" when consent is declined)
    // arrives instead of a code.
    const denied = params.get("error");

    const exchanged = useRef(false);

    useEffect(() => {
        if (exchanged.current || denied || !code || !state) {
            return;
        }

        exchanged.current = true;
        complete.mutate({ code, state });
    }, [code, state, denied, complete]);

    return (
        <>
            <div className="page-header">
                <div>
                    <h1 className="page-header__title">Google Drive</h1>
                    <p className="page-header__subtitle">
                        Finishing the connection for backup storage.
                    </p>
                </div>
            </div>

            <div className="card">
                <div className="state">
                    {denied ? (
                        <>
                            <div className="state__icon">⚠</div>
                            <div className="state__title">Connection cancelled</div>
                            <div className="state__desc">
                                Google reported <code>{denied}</code>. Nothing was
                                changed, and no data has been uploaded.
                            </div>
                        </>
                    ) : !code || !state ? (
                        <>
                            <div className="state__icon">⚠</div>
                            <div className="state__title">Incomplete callback</div>
                            <div className="state__desc">
                                This page was opened without the parameters Google
                                supplies. Start again from the backup screen.
                            </div>
                        </>
                    ) : complete.isPending ? (
                        <>
                            <div className="spinner" />
                            <div className="state__desc">Connecting Google Drive…</div>
                        </>
                    ) : complete.isError ? (
                        <>
                            <div className="state__icon">⚠</div>
                            <div className="state__title">Couldn’t connect</div>
                            <div className="state__desc">
                                {errorMessage(complete.error)}
                            </div>
                        </>
                    ) : complete.isSuccess && complete.data.connected ? (
                        <>
                            <div className="state__icon">✓</div>
                            <div className="state__title">Google Drive connected</div>
                            <div className="state__desc">
                                Backups will be uploaded to{" "}
                                <code>{complete.data.folderName}/backups</code> as{" "}
                                {complete.data.accountEmail ?? "the connected account"}.
                            </div>
                        </>
                    ) : complete.isSuccess ? (
                        <>
                            <div className="state__icon">⚠</div>
                            <div className="state__title">Permission missing</div>
                            <div className="state__desc">
                                {complete.data.detail ??
                                    "The Drive permission was not granted."}
                            </div>
                        </>
                    ) : null}

                    <Link className="btn btn--primary" to="/backup">
                        Back to Backup
                    </Link>
                </div>
            </div>
        </>
    );
}
