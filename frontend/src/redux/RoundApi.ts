import { createApi } from "@reduxjs/toolkit/query/react";
import { baseQueryWithReauth } from "./AuthApi";
import type {
  ValidationReport,
  RoundDto,
  CreateRoundRequest,
  RoundDetailsDto,
} from "../models/types/Round";

export const roundApi = createApi({
  reducerPath: "RoundApi",
  baseQuery: baseQueryWithReauth,

  tagTypes: ["Round"],

  endpoints: (builder) => ({
    // Run round validation without creating a round.
    validate: builder.mutation<ValidationReport, void>({
      query: () => ({
        url: "/rounds/validate",
        method: "POST",
      }),
    }),

    createRound: builder.mutation<RoundDto, CreateRoundRequest>({
      query: (body) => ({
        url: "/rounds",
        method: "POST",
        body,
      }),
      invalidatesTags: ["Round"],
    }),

    getRounds: builder.query<RoundDto[], void>({
      query: () => ({
        url: "/rounds",
        method: "GET",
      }),
      providesTags: (result) =>
        result
          ? [
              ...result.map(({ id }) => ({
                type: "Round" as const,
                id,
              })),
              { type: "Round" as const, id: "LIST" },
            ]
          : [{ type: "Round" as const, id: "LIST" }],
    }),

    getRound: builder.query<RoundDetailsDto, number>({
      query: (id) => ({
        url: `/rounds/${id}`,
        method: "GET",
      }),
      providesTags: (_result, _error, id) => [{ type: "Round", id }],
    }),
  }),
});

export const {
  useValidateMutation,
  useCreateRoundMutation,
  useGetRoundsQuery,
  useGetRoundQuery,
} = roundApi;
