import { Routes, Route } from "react-router-dom";
import Login from "./pages/Login";
import Home from "./pages/Home";
import Dashboard from "./pages/Dashboard";
import UserManagement from "./pages/admin/UserManagement";
import UserDetail from "./pages/admin/UserDetail";
import UserDelete from "./pages/admin/UserDelete";
import ProtectedRoute from "./store/ProtectedRoute";
import Layout from "./components/Layout";
import CreateUser from "./pages/admin/CreateUser";
import CreateLocation from "./pages/admin/CreateLocation";
import LocationImport from "./pages/admin/LocationImport";
import LocationManagement from "./pages/admin/LocationManagement";

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/" element={<Home />} />
          <Route path="/dashboard" element={<Dashboard />} />

          <Route element={<ProtectedRoute requiredRole="ADMIN" />}>
            <Route path="/admin/users" element={<UserManagement />} />
            <Route path="/admin/users/create" element={<CreateUser />} />
            <Route path="/admin/users/:username" element={<UserDetail />} />
            <Route path="/admin/users/:username/delete" element={<UserDelete />} />

            <Route path="/admin/locations" element={<LocationManagement />} />
            <Route path="/admin/locations/create" element={<CreateLocation />} />
            <Route path="/admin/locations/import" element={<LocationImport />} />
          </Route>
        </Route>
      </Route>
    </Routes>
  );
}

export default App;