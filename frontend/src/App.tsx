import { Routes, Route } from 'react-router-dom'
import './App.css'
import Home from './pages/Home'
import Login from './pages/Login'
import ProtectedRoute from './store/ProtectedRoute'
import Layout from './components/Layout'
import UserManagement from './pages/admin/UserManagement'

function App() {

  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/" element={<Home />} />

          <Route element={<ProtectedRoute requiredRole="ADMIN" />}>
            <Route path="/admin/users" element={<UserManagement />} />
          </Route>
        </Route>
      </Route>
    </Routes>

  )
}

export default App
