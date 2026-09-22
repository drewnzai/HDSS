import { createApi } from "@reduxjs/toolkit/query/react";
import { baseQueryWithReauth } from "./AuthApi";
import type { QuestionDto } from "../models/QuestionDto";
import type { CreateQuestionRequest } from "../models/CreateQuestionRequest";
import type { UpdateQuestionRequest } from "../models/UpdateQuestionRequest";

interface CreateQuestionArgs {
    formId: number;
    body: CreateQuestionRequest;
}

interface UpdateQuestionArgs {
    formId: number;
    questionId: number;
    body: UpdateQuestionRequest;
}

export const questionApi = createApi({
    reducerPath: "questionApi",
    baseQuery: baseQueryWithReauth,
    tagTypes: ["Question"],
    endpoints: (builder) => ({
        getQuestionsByForm: builder.query<QuestionDto[], number>({
            query: (formId) => `forms/${formId}/questions`,
            providesTags: (result) =>
                result
                    ? [
                        ...result.map((q) => ({ type: "Question" as const, id: q.id })),
                        { type: "Question" as const, id: "LIST" },
                    ]
                    : [{ type: "Question" as const, id: "LIST" }],
        }),

        createQuestion: builder.mutation<QuestionDto, CreateQuestionArgs>({
            query: ({ formId, body }) => ({
                url: `forms/${formId}/questions`,
                method: "POST",
                body,
            }),
            invalidatesTags: [{ type: "Question", id: "LIST" }],
        }),

        updateQuestion: builder.mutation<QuestionDto, UpdateQuestionArgs>({
            query: ({ formId, questionId, body }) => ({
                url: `forms/${formId}/questions/${questionId}`,
                method: "PUT",
                body,
            }),
            invalidatesTags: (_result, _error, { questionId }) => [
                { type: "Question", id: questionId },
                { type: "Question", id: "LIST" },
            ],
        }),

        deleteQuestion: builder.mutation<string, { formId: number; questionId: number }>({
            query: ({ formId, questionId }) => ({
                url: `forms/${formId}/questions/${questionId}`,
                method: "DELETE",
                responseHandler: "text",
            }),
            // Bug fix: the mutation arg is {formId, questionId}, not a
            // scalar id — using it directly as `id` produced a tag that
            // never matched what getQuestionsByForm provides, so the
            // list never refetched after a delete.
            invalidatesTags: (_result, _error, { questionId }) => [
                { type: "Question", id: questionId },
                { type: "Question", id: "LIST" },
            ],
        }),
    }),
});

export const {
    useGetQuestionsByFormQuery,
    useCreateQuestionMutation,
    useUpdateQuestionMutation,
    useDeleteQuestionMutation,
} = questionApi;