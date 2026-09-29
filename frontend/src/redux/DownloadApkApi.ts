import { createApi } from "@reduxjs/toolkit/query/react";
import { baseQueryWithReauth } from "./AuthApi";

export const downloadApkApi = createApi({
    reducerPath: "downloadApkApi",
        baseQuery: baseQueryWithReauth,
        endpoints: (builder) => ({
            downloadApk: builder.mutation<Blob, void>({
                query: () => ({
                    url: "/app/download",
                    method: "GET",
                    responseHandler: (response) => response.blob(),
                }),
            }),
        })
});

export const {
    useDownloadApkMutation
} = downloadApkApi;