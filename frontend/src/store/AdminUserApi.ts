import { createApi } from "@reduxjs/toolkit/query/react";
import { baseQueryWithReauth } from "./AuthApi";
import type { PagedResponse } from "../models/PagedResponse";
import type { UserSummary } from "../models/UserSummary";

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
            query: ({ page, size }) => `user?page=${page}&size=${size}`,
            providesTags: (result) =>
                result
                    ? [
                        ...result.data.map((u) => ({ type: "User" as const, id: u.username })),
                        { type: "User" as const, id: "LIST" }
                    ]
                    : [{ type: "User" as const, id: "LIST" }]
        }),

        // Used by the delete confirmation page, not the table itself.
        deleteUser: builder.mutation<void, string>({
            query: (username) => ({
                url: `user/${username}`,
                method: "DELETE"
            }),
            invalidatesTags: [{ type: "User", id: "LIST" }]
        })
    })
});

export const { useGetUsersQuery, useDeleteUserMutation } = adminUserApi;