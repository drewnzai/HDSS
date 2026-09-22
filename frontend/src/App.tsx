import { Routes, Route } from "react-router-dom"
import Login from "./pages/login/Login"
import ProtectedRoute from "./routes/ProtectedRoute"
import Home from "./pages/home/Home"
import UserManagement from "./pages/admin/user/management/UserManagement"
import Layout from "./components/layout/Layout"
import UserDetail from "./pages/admin/user/UserDetail"
import CreateUser from "./pages/admin/user/CreateUser"
import LocationManagement from "./pages/admin/location/management/LocationManagement"
import LocationImport from "./pages/admin/location/import/LocationImport"
import CreateForm from "./pages/admin/form/create/CreateForm"
import FormManagement from "./pages/admin/form/management/FormManagement"

function App() {

  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      <Route element={<ProtectedRoute />}>
        <Route path="/" element={<Home />} />
      </Route>

      <Route element={<ProtectedRoute requiredRole="ADMIN" />}>
        <Route element={<Layout />}>
          <Route path="/admin/users" element={<UserManagement />} />
          <Route path="/admin/users/create" element={<CreateUser />} />
          <Route path="/admin/users/:username" element={<UserDetail />} />
          <Route path="/admin/locations" element={<LocationManagement />} />
          <Route path="/admin/locations/import" element={<LocationImport />} />
          <Route path="/admin/forms/create" element={<CreateForm />} />
          <Route path="/admin/forms" element={<FormManagement />} />
        </Route>
      </Route>
    </Routes>
  )
}

export default App;
