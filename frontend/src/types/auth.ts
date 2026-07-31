export type UserRole = "OWNER" | "ADMIN" | "VIEWER";

export interface User {
    id: string;
    email: string;
    name: string;
    role: UserRole;
    emailVerified: boolean;
    createdAt: string;
}

export interface AuthResponse {
    accessToken: string;
    tokenType: string;
    expiresIn: number;
    user: User;
}
