import { Navigate, Outlet } from "react-router-dom";
import { useAppSelector } from "./hooks";
import { selectIsAuthenticated } from "./AuthSlice";

function ProtectedRoute() {
    const isAuthenticated = useAppSelector(selectIsAuthenticated);
    return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
}

export default ProtectedRoute;