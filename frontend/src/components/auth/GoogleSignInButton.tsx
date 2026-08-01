import { useEffect, useRef } from "react";

/**
 * Renders Google's official Sign-In button via Google Identity Services.
 *
 * GIS hands us an ID token which the caller posts to /api/auth/google;
 * the backend verifies it. Renders nothing when VITE_GOOGLE_CLIENT_ID
 * is unset, so the feature stays invisible until configured.
 */

interface CredentialResponse {
    credential?: string;
}

interface GoogleIdentityApi {
    accounts: {
        id: {
            initialize: (config: {
                client_id: string;
                callback: (response: CredentialResponse) => void;
            }) => void;
            renderButton: (
                parent: HTMLElement,
                options: Record<string, string | number>,
            ) => void;
        };
    };
}

declare global {
    interface Window {
        google?: GoogleIdentityApi;
    }
}

const GIS_SRC = "https://accounts.google.com/gsi/client";

interface Props {
    onCredential: (idToken: string) => void;
    disabled?: boolean;
}

export default function GoogleSignInButton({ onCredential, disabled }: Props) {
    const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;
    const containerRef = useRef<HTMLDivElement>(null);

    // Keep the latest callback without re-rendering the Google button.
    const callbackRef = useRef(onCredential);

    useEffect(() => {
        callbackRef.current = onCredential;
    }, [onCredential]);

    useEffect(() => {
        if (!clientId || !containerRef.current) {
            return;
        }

        const container = containerRef.current;

        function render() {
            if (!window.google) {
                return;
            }

            window.google.accounts.id.initialize({
                client_id: clientId as string,
                callback: (response) => {
                    if (response.credential) {
                        callbackRef.current(response.credential);
                    }
                },
            });

            window.google.accounts.id.renderButton(container, {
                type: "standard",
                theme: "outline",
                size: "large",
                text: "continue_with",
                width: 320,
            });
        }

        if (window.google) {
            render();
            return;
        }

        let script = document.querySelector<HTMLScriptElement>(
            `script[src="${GIS_SRC}"]`,
        );

        if (!script) {
            script = document.createElement("script");
            script.src = GIS_SRC;
            script.async = true;
            script.defer = true;
            document.head.appendChild(script);
        }

        script.addEventListener("load", render);

        return () => script?.removeEventListener("load", render);
    }, [clientId]);

    if (!clientId) {
        // Production hides the button entirely — end users cannot act on a
        // missing server-side setting. In development, say so on the page:
        // rendering nothing at all just looks like the feature is broken.
        if (!import.meta.env.DEV) {
            return null;
        }

        return (
            <>
                <div className="auth-divider">
                    <span>or</span>
                </div>

                <div className="google-button-placeholder">
                    Google Sign-In is hidden because <code>VITE_GOOGLE_CLIENT_ID</code> is
                    not set. Add it to the <code>.env</code> file in the project root and
                    restart the dev server. This notice only appears in development.
                </div>
            </>
        );
    }

    return (
        <>
            <div className="auth-divider">
                <span>or</span>
            </div>

            <div
                className={disabled ? "google-button google-button--busy" : "google-button"}
                ref={containerRef}
            />
        </>
    );
}
