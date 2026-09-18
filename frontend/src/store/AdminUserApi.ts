import { createApi } from "@reduxjs/toolkit/query/react";
import { baseQueryWithReauth } from "./AuthApi";
import type { PagedResponse } from "../models/PagedResponse";
import type { UserSummary } from "../models/UserSummary";
import type { CreateUserRequest } from "../models/CreateUserRequest";

interface GetUsersParams {
    page: number;
    size: number;
}

export const adminUserApi = createApi({
    reducerPath: "adminUserApi",
    baseQuery: baseQueryWithReauth,
    tagTypes: ["User"],
    endpoints: (builder) => ({
        getUsers: builder.query<PagedResponse<UserSummary>, GetUsersParams>({
            query: ({ page, size }) => `users?page=${page}&size=${size}`,
            providesTags: (result) =>
                result
                    ? [
                        ...result.data.map((u) => ({ type: "User" as const, id: u.username })),
                        { type: "User" as const, id: "LIST" }
                    ]
                    : [{ type: "User" as const, id: "LIST" }]
        }),

        getUserByUsername: builder.query<UserSummary, string>({
            query: (username) => `users/${username}`,
            providesTags: (_result, _error, username) => [{ type: "User", id: username }]
        }),

        createUser: builder.mutation<string, CreateUserRequest>({
            query: (body) => ({
                url: "users/add",
                method: "POST",
                body,
                responseHandler: "text" // same plain-string response pattern as delete
            }),
            invalidatesTags: [{ type: "User", id: "LIST" }]
        }),

        deleteUser: builder.mutation<string, UserSummary>({
            query: (user) => ({
                url: "users/delete",
                method: "DELETE",
                body: user,
                responseHandler: "text"
            }),
            invalidatesTags: (_result, _error, user) => [
                { type: "User", id: user.username },
                { type: "User", id: "LIST" }
            ]
        })
    })
});

export const {
    useGetUsersQuery,
    useGetUserByUsernameQuery,
    useDeleteUserMutation,
    useCreateUserMutation
} = adminUserApi;