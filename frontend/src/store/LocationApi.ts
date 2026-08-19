import { createApi } from "@reduxjs/toolkit/query/react";
import { baseQueryWithReauth } from "./AuthApi";
import type { CreateLocationRequest } from "../models/CreateLocationRequest";
import type { LocationDto } from "../models/Location";
import type { LocationImportResult } from "../models/LocationImportResult";

export const locationApi = createApi({
    reducerPath: "locationApi",
    baseQuery: baseQueryWithReauth,
    tagTypes: ["Location"],
    endpoints: (builder) => ({
        getChildren: builder.query<LocationDto[], number | null>({
            query: (parentId) =>
                parentId === null ? "locations" : `locations?id=${parentId}`,
            providesTags: (result, _error, parentId) =>
                result
                    ? [
                        ...result.map((loc) => ({ type: "Location" as const, id: loc.id })),
                        { type: "Location" as const, id: `CHILDREN_${parentId ?? "root"}` }
                    ]
                    : [{ type: "Location" as const, id: `CHILDREN_${parentId ?? "root"}` }]
        }),

        getDescendants: builder.query<LocationDto[], number>({
            query: (id) => `locations/${id}/descendants`
        }),

        getAncestors: builder.query<LocationDto[], number>({
            query: (id) => `locations/${id}/ancestors`
        }),

        createLocation: builder.mutation<string, CreateLocationRequest>({
            query: (body) => ({
                url: "locations",
                method: "POST",
                body,
                responseHandler: "text"
            }),
            invalidatesTags: (_result, _error, arg) => [
                { type: "Location", id: `CHILDREN_${arg.parentId ?? "root"}` }
            ]
        }),

        importLocations: builder.mutation<LocationImportResult, FormData>({
            query: (formData) => ({
                url: "locations/import",
                method: "POST",
                body: formData
            }),
            // Wipes every cached children-list, since an import can touch any branch of the tree
            invalidatesTags: [{ type: "Location", id: "CHILDREN_root" }]
        })
    })
});

export const {
    useGetChildrenQuery,
    useGetDescendantsQuery,
    useGetAncestorsQuery,
    useCreateLocationMutation,
    useImportLocationsMutation
} = locationApi;