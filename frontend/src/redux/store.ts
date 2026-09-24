import { configureStore } from "@reduxjs/toolkit";
import { authApi } from "./AuthApi";
import { userApi } from "./UserApi";
import authReducer from "./AuthSlice";
import { locationApi } from "./LocationApi";
import { formApi } from "./FormApi";
import { questionApi } from "./QuestionApi";
import { choiceApi } from "./ChoiceApi";
import { mappedFieldApi } from "./MappedFieldApi";

export const store = configureStore({
    reducer: {
        [authApi.reducerPath]: authApi.reducer,
        [userApi.reducerPath]: userApi.reducer,
        [locationApi.reducerPath]: locationApi.reducer,
        [formApi.reducerPath]: formApi.reducer,
        [questionApi.reducerPath]: questionApi.reducer,
        [choiceApi.reducerPath]: choiceApi.reducer,
        [mappedFieldApi.reducerPath]: mappedFieldApi.reducer,
        auth: authReducer
    },
    middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware().concat(authApi.middleware, userApi.middleware, locationApi.middleware, formApi.middleware, questionApi.middleware, choiceApi.middleware, mappedFieldApi.middleware)
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;