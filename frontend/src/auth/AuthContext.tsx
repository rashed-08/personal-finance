import { createContext } from "react";

import type { User } from "../types/auth";

export interface AuthContextValue {
    user: User | null;
    /** True until the initial refresh-cookie bootstrap settles. */
    isLoading: boolean;
    isAuthenticated: boolean;
    login: (email: string, password: string) => Promise<void>;
    register: (email: string, password: string, name: string) => Promise<void>;
    loginWithGoogle: (idToken: string) => Promise<void>;
    logout: () => Promise<void>;
    refreshUser: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextValue | null>(null);
