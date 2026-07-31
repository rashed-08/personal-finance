import api from "../api/api";
import type { AuthResponse, User } from "../types/auth";

export interface LoginPayload {
    email: string;
    password: string;
}

export interface RegisterPayload {
    email: string;
    password: string;
    name: string;
}

export async function login(payload: LoginPayload): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>("/auth/login", payload);
    return response.data;
}

export async function register(payload: RegisterPayload): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>("/auth/register", payload);
    return response.data;
}

export async function googleSignIn(idToken: string): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>("/auth/google", { idToken });
    return response.data;
}

export async function refresh(): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>("/auth/refresh", {});
    return response.data;
}

export async function logout(): Promise<void> {
    await api.post("/auth/logout", {});
}

export async function getCurrentUser(): Promise<User> {
    const response = await api.get<User>("/users/me");
    return response.data;
}

export async function verifyEmail(token: string): Promise<void> {
    await api.post("/auth/verify-email", { token });
}

export async function resendVerification(email: string): Promise<void> {
    await api.post("/auth/resend-verification", { email });
}

export async function forgotPassword(email: string): Promise<void> {
    await api.post("/auth/forgot-password", { email });
}

export async function resetPassword(
    token: string,
    newPassword: string,
): Promise<void> {
    await api.post("/auth/reset-password", { token, newPassword });
}

export async function changePassword(
    currentPassword: string,
    newPassword: string,
): Promise<void> {
    await api.put("/users/me/password", { currentPassword, newPassword });
}
