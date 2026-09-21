import { createApi } from "@reduxjs/toolkit/query/react";
import { baseQueryWithReauth } from "./AuthApi";
import type { FormDto } from "../models/FormDto";
import type { CreateFormRequest } from "../models/CreateFormRequest";

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

        getAllForms: builder.query<FormDto[], void>({ 
            query: () => ({ 
                url: "forms", 
                method: "GET", 
            }), providesTags: ["Form"], 
        }),
    })
});

export const {
    useGetAllFormsQuery,
    useCreateFormMutation
} = formApi;