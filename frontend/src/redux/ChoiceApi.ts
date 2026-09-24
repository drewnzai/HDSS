import { createApi } from "@reduxjs/toolkit/query/react";
import { baseQueryWithReauth } from "./AuthApi";
import type { ChoiceDto } from "../models/ChoiceDto";
import type { CreateChoiceRequest } from "../models/CreateChoiceRequest";
import type { UpdateChoiceRequest } from "../models/UpdateChoiceRequest";


interface CreateChoiceArgs {
    body: CreateChoiceRequest;
}

interface UpdateChoiceArgs {
    choiceId: number;
    body: UpdateChoiceRequest;
}

export const choiceApi = createApi({
    reducerPath: "choiceApi",
    baseQuery: baseQueryWithReauth,
    tagTypes: ["Choice"],
    endpoints: (builder) => ({
        getChoicesByListName: builder.query<ChoiceDto[], string>({
            query: (listName) => `choices/list/${encodeURIComponent(listName)}`,
            providesTags: (result, _error, listName) =>
                result
                    ? [
                        ...result.map((c) => ({ type: "Choice" as const, id: c.id })),
                        { type: "Choice" as const, id: `LIST-${listName}` },
                    ]
                    : [{ type: "Choice" as const, id: `LIST-${listName}` }],
        }),

        createChoice: builder.mutation<ChoiceDto, CreateChoiceArgs>({
            query: ({ body }) => ({
                url: "choices",
                method: "POST",
                body,
            }),
            invalidatesTags: (result) =>
                result
                    ? [{ type: "Choice", id: `LIST-${result.listName}` }]
                    : [],
        }),

        updateChoice: builder.mutation<ChoiceDto, UpdateChoiceArgs>({
            query: ({ choiceId, body }) => ({
                url: `choices/${choiceId}`,
                method: "PUT",
                body,
            }),
            invalidatesTags: (result, _error, { choiceId }) => [
                { type: "Choice", id: choiceId },
                ...(result ? [{ type: "Choice" as const, id: `LIST-${result.listName}` }] : []),
            ],
        }),

        deleteChoice: builder.mutation<string, { choiceId: number; listName: string }>({
            query: ({ choiceId }) => ({
                url: `choices/${choiceId}`,
                method: "DELETE",
                responseHandler: "text",
            }),
            invalidatesTags: (_result, _error, { choiceId, listName }) => [
                { type: "Choice", id: choiceId },
                { type: "Choice", id: `LIST-${listName}` },
            ],
        }),
    }),
});

export const {
    useGetChoicesByListNameQuery,
    useCreateChoiceMutation,
    useUpdateChoiceMutation,
    useDeleteChoiceMutation,
} = choiceApi;
