import { useCallback, useEffect, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { useQueryClient } from "@tanstack/react-query";

import { setSessionExpiredHandler } from "../api/api";
import { setAccessToken } from "./tokenStore";
import { AuthContext } from "./AuthContext";
import type { AuthResponse, User } from "../types/auth";
import * as authService from "../services/auth.service";

export default function AuthProvider({ children }: { children: ReactNode }) {
    const queryClient = useQueryClient();

    const [user, setUser] = useState<User | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    const applySession = useCallback((auth: AuthResponse) => {
        setAccessToken(auth.accessToken);
        setUser(auth.user);
    }, []);

    const clearSession = useCallback(() => {
        setAccessToken(null);
        setUser(null);
        queryClient.clear();
    }, [queryClient]);

    // A reload loses the in-memory access token, so try to rebuild the
    // session from the httpOnly refresh cookie before rendering routes.
    useEffect(() => {
        let cancelled = false;

        authService
            .refresh()
            .then((auth) => {
                if (!cancelled) {
                    applySession(auth);
                }
            })
            .catch(() => {
                // No usable cookie — stay logged out.
            })
            .finally(() => {
                if (!cancelled) {
                    setIsLoading(false);
                }
            });

        return () => {
            cancelled = true;
        };
    }, [applySession]);

    // Lets the axios interceptor drop the session when a refresh fails.
    useEffect(() => {
        setSessionExpiredHandler(clearSession);

        return () => setSessionExpiredHandler(null);
    }, [clearSession]);

    const login = useCallback(
        async (email: string, password: string) => {
            applySession(await authService.login({ email, password }));
        },
        [applySession],
    );

    const register = useCallback(
        async (email: string, password: string, name: string) => {
            applySession(await authService.register({ email, password, name }));
        },
        [applySession],
    );

    const loginWithGoogle = useCallback(
        async (idToken: string) => {
            applySession(await authService.googleSignIn(idToken));
        },
        [applySession],
    );

    const logout = useCallback(async () => {
        try {
            await authService.logout();
        } finally {
            // Always clear locally, even if the server call failed.
            clearSession();
        }
    }, [clearSession]);

    const refreshUser = useCallback(async () => {
        setUser(await authService.getCurrentUser());
    }, []);

    const value = useMemo(
        () => ({
            user,
            isLoading,
            isAuthenticated: user !== null,
            login,
            register,
            loginWithGoogle,
            logout,
            refreshUser,
        }),
        [user, isLoading, login, register, loginWithGoogle, logout, refreshUser],
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
