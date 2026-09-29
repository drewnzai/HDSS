import { configureStore } from "@reduxjs/toolkit";
import { authApi } from "./AuthApi";
import authReducer from "./AuthSlice";
import { choiceApi } from "./ChoiceApi";
import { formApi } from "./FormApi";
import { locationApi } from "./LocationApi";
import { mappedFieldApi } from "./MappedFieldApi";
import { questionApi } from "./QuestionApi";
import { userApi } from "./UserApi";
import { downloadApkApi } from "./DownloadApkApi";

export const store = configureStore({
    reducer: {
        [authApi.reducerPath]: authApi.reducer,
        [userApi.reducerPath]: userApi.reducer,
        [locationApi.reducerPath]: locationApi.reducer,
        [formApi.reducerPath]: formApi.reducer,
        [questionApi.reducerPath]: questionApi.reducer,
        [choiceApi.reducerPath]: choiceApi.reducer,
        [mappedFieldApi.reducerPath]: mappedFieldApi.reducer,
        [downloadApkApi.reducerPath]: downloadApkApi.reducer,
        auth: authReducer
    },
    middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware().concat(authApi.middleware, userApi.middleware, locationApi.middleware, formApi.middleware, questionApi.middleware, choiceApi.middleware, mappedFieldApi.middleware, downloadApkApi.middleware)
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;