import axios from "axios";
import type { InternalAxiosRequestConfig } from "axios";

import { getAccessToken, setAccessToken } from "../auth/tokenStore";

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL ?? "http://localhost:8080/api",
    headers: {
        "Content-Type": "application/json",
    },
    timeout: 10000,
    // Send the httpOnly refresh cookie on /auth/** calls.
    withCredentials: true,
});

/**
 * Endpoints that establish authentication. A 401 from these is a real
 * failure (bad credentials / dead session), never something a refresh
 * could fix, so the retry logic must skip them.
 */
const AUTH_PATHS = [
    "/auth/login",
    "/auth/register",
    "/auth/google",
    "/auth/refresh",
    "/auth/logout",
];

/** Called when the session cannot be recovered; wired up by AuthProvider. */
let onSessionExpired: (() => void) | null = null;

export function setSessionExpiredHandler(handler: (() => void) | null): void {
    onSessionExpired = handler;
}

api.interceptors.request.use((config) => {
    const token = getAccessToken();

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

/** Shared across concurrent 401s so only one refresh call goes out. */
let refreshPromise: Promise<string> | null = null;

async function refreshAccessToken(): Promise<string> {
    if (!refreshPromise) {
        refreshPromise = axios
            .post<{ accessToken: string }>(
                "/auth/refresh",
                {},
                { baseURL: api.defaults.baseURL, withCredentials: true },
            )
            .then((response) => {
                setAccessToken(response.data.accessToken);
                return response.data.accessToken;
            })
            .finally(() => {
                refreshPromise = null;
            });
    }

    return refreshPromise;
}

type RetriableConfig = InternalAxiosRequestConfig & { _retried?: boolean };

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const config = error.config as RetriableConfig | undefined;
        const status = error.response?.status;
        const url = config?.url ?? "";

        const isRetriable =
            status === 401 &&
            config &&
            !config._retried &&
            !AUTH_PATHS.some((path) => url.startsWith(path));

        if (!isRetriable) {
            return Promise.reject(error);
        }

        config._retried = true;

        try {
            const token = await refreshAccessToken();

            config.headers.Authorization = `Bearer ${token}`;

            return api.request(config);
        } catch (refreshError) {
            setAccessToken(null);
            onSessionExpired?.();

            return Promise.reject(refreshError);
        }
    },
);

export default api;
