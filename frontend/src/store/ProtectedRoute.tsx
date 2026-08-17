import { Navigate, Outlet } from "react-router-dom";
import { useAppSelector } from "./hooks";
import { selectIsAuthenticated, selectRole } from "./AuthSlice";

interface ProtectedRouteProps {
    requiredRole?: "ADMIN" | "USER";
}

function ProtectedRoute({ requiredRole }: ProtectedRouteProps) {
    const isAuthenticated = useAppSelector(selectIsAuthenticated);
    const role = useAppSelector(selectRole);

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (requiredRole && role !== requiredRole) {
        return (
            <Navigate
                to="/"
                replace
                state={{ flash: "You don't have permission to access that page.", flashType: "danger" }}
            />
        );
    }

    return <Outlet />;
}

export default ProtectedRoute;