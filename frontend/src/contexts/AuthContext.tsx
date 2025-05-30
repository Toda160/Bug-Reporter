// src/contexts/AuthContext.tsx
import { createContext, useContext, useState, useEffect } from 'react'
import type { ReactNode } from 'react'
import { authService } from '../services/api'

interface User {
  id: string
  email: string
  username: string
  role: string      // ← include role
  avatar?: string
}

interface AuthContextType {
  user: User | null
  loading: boolean
  error: string | null
  login: (username: string, password: string) => Promise<void>
  register: (userData: any) => Promise<void>
  logout: () => void
  clearError: () => void
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    // hydrate from localStorage
    const stored = localStorage.getItem('user')
    if (stored) {
      setUser(JSON.parse(stored))
    }
    setLoading(false)
  }, [])

  const login = async (username: string, password: string) => {
    setError(null)
     const data = await authService.login(username, password)
     // data === { id, username, email, role, token? }
      const u: User = {
        id:       data.id,
        email:    data.email,
        username: data.username,
        role:     data.role,
        avatar:   data.avatar
    }
    setUser(u)
    localStorage.setItem('user', JSON.stringify(u))
  }

  const register = async (userData: any) => {
    setError(null)
    // if you want to auto‐login on register, you can follow the same pattern:
    const data = await authService.register(userData)
   // if register returns the same shape (id, username, email, role):
    const u: User = {
      id:       data.id,
      email:    data.email,
      username: data.username,
      role:     data.role
    }
    setUser(u)
    localStorage.setItem('user', JSON.stringify(u))
  }

  const logout = () => {
    authService.logout()
    setUser(null)
    localStorage.removeItem('user')
  }

  const clearError = () => setError(null)

  return (
    <AuthContext.Provider
      value={{ user, loading, error, login, register, logout, clearError }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be inside AuthProvider')
  return ctx
}
