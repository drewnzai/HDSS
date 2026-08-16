import { configureStore } from "@reduxjs/toolkit";
import { authApi } from "./AuthApi";
import { adminUserApi } from "./AdminUserApi";
import authReducer from "./AuthSlice";

export const store = configureStore({
    reducer: {
        [authApi.reducerPath]: authApi.reducer,
        [adminUserApi.reducerPath]: adminUserApi.reducer,
        auth: authReducer
    },
    middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware().concat(authApi.middleware, adminUserApi.middleware)
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;