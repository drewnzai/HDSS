export interface LoginResponse{
    authenticationToken: string;
    refreshToken: string;
    expiresAt: string;
    firstName: string;
    role: "ADMIN" | "USER";
}