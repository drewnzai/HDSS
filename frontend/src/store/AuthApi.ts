import { createApi, fetchBaseQuery, type BaseQueryFn, type FetchArgs, type FetchBaseQueryError } from "@reduxjs/toolkit/query/react";
import type { LoginResponse } from "../models/LoginResponse";
import type { LoginRequest } from "../models/LoginRequest";
import type { RefreshTokenRequest } from "../models/RefreshTokenRequest";
import type { RootState } from "./store";
import { logout, setCredentials } from "./AuthSlice";

const BASE_URL = "http://localhost:8080/api/";

const rawBaseQuery = fetchBaseQuery({
    baseUrl: BASE_URL,
    prepareHeaders: (headers, { getState }) => {
        const accessToken = (getState() as RootState).auth.authenticationToken;

        if (accessToken) {
            headers.set("Authorization", `Bearer ${accessToken}`);
        }

        return headers;
    },
});

const baseQueryWithReauth: BaseQueryFn<
string | FetchArgs,
    unknown,
    FetchBaseQueryError
    > = async (args, api, extraOptions) => {
        let result = await rawBaseQuery(args, api, extraOptions);

        if (result.error && result.error.status === 401) {
            const { refreshToken, username } = (api.getState() as RootState).auth;

            if (refreshToken && username) {
                const refreshResult = await rawBaseQuery(
                    {
                        url: "auth/refresh",
                        method: "POST",
                        body: { token: refreshToken, username } as RefreshTokenRequest
                    },
                    api,
                    extraOptions
                );

                if (refreshResult.data) {
                    const data = refreshResult.data as LoginResponse;
                    api.dispatch(setCredentials({ ...data, username }));
                    // retry the original request with the new token
                    result = await rawBaseQuery(args, api, extraOptions);
                } else {
                    api.dispatch(logout());
                }
            } else {
                api.dispatch(logout());
            }
        }

        return result;
    };

export const authApi = createApi({
    reducerPath: "authApi",
    baseQuery: baseQueryWithReauth,
    endpoints: (builder) => ({
        login: builder.mutation<LoginResponse, LoginRequest>({
            query: (credentials) => ({
                url: "auth/login",
                method: "POST",
                body: credentials
            }),
            async onQueryStarted(arg, { dispatch, queryFulfilled }) {
                try {
                    const { data } = await queryFulfilled;
                    // LoginResponse has no username field, so we carry it over from the request
                    dispatch(setCredentials({ ...data, username: arg.username }));
                } catch {
                    // login failed — nothing to store
                }
            }
        }),
        refresh: builder.mutation<LoginResponse, RefreshTokenRequest>({
            query: (body) => ({
                url: "auth/refresh",
                method: "POST",
                body
            }),
            async onQueryStarted(arg, { dispatch, queryFulfilled }) {
                try {
                    const { data } = await queryFulfilled;
                    dispatch(setCredentials({ ...data, username: arg.username }));
                } catch {
                    dispatch(logout());
                }
            }
        })
    })
});

export const { useLoginMutation, useRefreshMutation } = authApi;