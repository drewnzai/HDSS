import { Routes, Route } from 'react-router-dom'
import './App.css'
import Home from './pages/Home'
import Login from './pages/Login'
import ProtectedRoute from './store/ProtectedRoute'

function App() {

  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      <Route element={<ProtectedRoute />}>
        <Route path="/" element={<Home />} />
      </Route>
      
      <Route element={<ProtectedRoute requiredRole='ADMIN'/>}>
      </Route>
    </Routes>

  )
}

export default App
