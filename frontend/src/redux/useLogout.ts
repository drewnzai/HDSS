import { useNavigate } from "react-router-dom";
import { authApi } from "./AuthApi";
import { logout } from "./AuthSlice";
import { useAppDispatch } from "./hooks";

export function useLogout() {
    const dispatch = useAppDispatch();
    const navigate = useNavigate();

    return () => {
        dispatch(logout());
        // clear cached queries so no stale/previous-user data lingers
        dispatch(authApi.util.resetApiState());
        navigate("/login", { replace: true });
    };
}