import { createApi } from "@reduxjs/toolkit/query/react";
import { baseQueryWithReauth } from "./AuthApi";
import type { FormDto } from "../models/FormDto";
import type { CreateFormRequest } from "../models/CreateFormRequest";
import type { PagedResponse } from "../models/PagedResponse";
import type { GetPageParams } from "./PageParams";

export const formApi = createApi({
    reducerPath: "formApi",
    baseQuery: baseQueryWithReauth,
    tagTypes: ["Form"],
    endpoints: (builder) => ({
        createForm: builder.mutation<FormDto, CreateFormRequest>({
            query: (body) => ({
                url: "forms",
                method: "POST",
                body,
            }), invalidatesTags: ["Form"]
        }),

        getAllForms: builder.query<PagedResponse<FormDto>, GetPageParams>({
            query: ({ page, size }) => `forms?page=${page}&size=${size}`,
            providesTags: (result) =>
                result
                    ? [
                        ...result.data.map((u) => ({ type: "Form" as const, id: u.id })),
                        { type: "Form" as const, id: "LIST" }
                    ]
                    : [{ type: "Form" as const, id: "LIST" }]
        }),

        getFormById: builder.query<FormDto, number>({
            query: (id) => `forms/${id}`,
            providesTags: (_result, _error, id) => [{ type: "Form", id: id }] 
        })
    }),
});

export const {
    useGetAllFormsQuery,
    useCreateFormMutation,
    useGetFormByIdQuery
} = formApi;