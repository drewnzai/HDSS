import { useNavigate } from "react-router-dom";
import { logout } from "./AuthSlice";
import { authApi } from "./AuthApi";
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