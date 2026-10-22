import { createApi } from "@reduxjs/toolkit/query/react";
import { baseQueryWithReauth } from "./AuthApi";

export const roundApi = createApi({
    reducerPath: "RoundApi",
    baseQuery: baseQueryWithReauth,
    endpoints: (builder) => ({
        validate: builder.mutation<Object, void>({
            query: () => ({
                url: "/rounds/validate",
                method: "POST"
            })
        })
    })
});


export const {
    useValidateMutation
} = roundApi;