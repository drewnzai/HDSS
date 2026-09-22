import { configureStore } from "@reduxjs/toolkit";
import { authApi } from "./AuthApi";
import { adminUserApi } from "./AdminUserApi";
import authReducer from "./AuthSlice";
import { locationApi } from "./LocationApi";
import { formApi } from "./FormApi";

export const store = configureStore({
    reducer: {
        [authApi.reducerPath]: authApi.reducer,
        [adminUserApi.reducerPath]: adminUserApi.reducer,
        [locationApi.reducerPath]: locationApi.reducer,
        [formApi.reducerPath]: formApi.reducer,
        auth: authReducer
    },
    middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware().concat(authApi.middleware, adminUserApi.middleware, locationApi.middleware, formApi.middleware)
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;