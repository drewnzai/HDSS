import { createApi } from "@reduxjs/toolkit/query/react";
import { baseQueryWithReauth } from "./AuthApi";
import type { MappableFieldDto } from "../models/MappableFieldDto";
import type { MappedEntity } from "../models/types/MappedEntity";

export const mappedFieldApi = createApi({
    reducerPath: "mappedFieldApi",
    baseQuery: baseQueryWithReauth,
    tagTypes: ["MappedField"],
    endpoints: (builder) => ({
        // NONE has no mappable fields — callers should skip this query
        // when mappedEntity === "NONE" rather than calling it.
        getMappableFields: builder.query<MappableFieldDto[], MappedEntity>({
            query: (entity) => `mapped-fields/${entity}`,
            providesTags: (_result, _error, entity) => [
                { type: "MappedField", id: entity },
            ],
        }),
    }),
});

export const { useGetMappableFieldsQuery } = mappedFieldApi;
