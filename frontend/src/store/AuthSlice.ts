import { createSlice } from "@reduxjs/toolkit";
import type { PayloadAction } from "@reduxjs/toolkit";
import type { LoginResponse } from "../models/LoginResponse";

interface AuthState {
    username: string | null;
    authenticationToken: string | null;
    refreshToken: string | null;
    expiresAt: string | null;
}

const initialState: AuthState = {
    username: localStorage.getItem("username"),
    authenticationToken: localStorage.getItem("authenticationToken"),
    refreshToken: localStorage.getItem("refreshToken"),
    expiresAt: localStorage.getItem("expiresAt")
};

const AuthSlice = createSlice({
    name: "auth",
    initialState,
    reducers: {
        setCredentials: (
            state,
            action: PayloadAction<LoginResponse & { username: string }>
        ) => {
            const { authenticationToken, refreshToken, expiresAt, username } = action.payload;
            state.authenticationToken = authenticationToken;
            state.refreshToken = refreshToken;
            state.expiresAt = expiresAt;
            state.username = username;

            localStorage.setItem("authenticationToken", authenticationToken);
            localStorage.setItem("refreshToken", refreshToken);
            localStorage.setItem("expiresAt", expiresAt);
            localStorage.setItem("username", username);
        },
        logout: (state) => {
            state.authenticationToken = null;
            state.refreshToken = null;
            state.expiresAt = null;
            state.username = null;

            localStorage.removeItem("authenticationToken");
            localStorage.removeItem("refreshToken");
            localStorage.removeItem("expiresAt");
            localStorage.removeItem("username");
        }
    }
});

export const { setCredentials, logout } = AuthSlice.actions;
export default AuthSlice.reducer;

// selectors
export const selectIsAuthenticated = (state: { auth: AuthState }) =>
    Boolean(state.auth.authenticationToken);
export const selectUsername = (state: { auth: AuthState }) => state.auth.username;
export const selectTokenExpiry = (state: { auth: AuthState }) => state.auth.expiresAt;