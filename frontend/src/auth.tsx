import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import { api } from "./api";
import type { User } from "./types";

type AuthContextValue = {
  token: string | null;
  user: User | null;
  ready: boolean;
  signIn: (email: string, password: string) => Promise<void>;
  register: (displayName: string, email: string, password: string) => Promise<void>;
  signOut: () => Promise<void>;
  updateUser: (user: User) => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);
const storageKey = "sloth-session-token";

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => sessionStorage.getItem(storageKey));
  const [user, setUser] = useState<User | null>(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    if (!token) {
      setUser(null);
      setReady(true);
      return;
    }
    let cancelled = false;
    api<User>("/api/users/me", {}, token)
      .then((profile) => { if (!cancelled) setUser(profile); })
      .catch(() => {
        if (!cancelled) {
          sessionStorage.removeItem(storageKey);
          setToken(null);
          setUser(null);
        }
      })
      .finally(() => { if (!cancelled) setReady(true); });
    return () => { cancelled = true; };
  }, [token]);

  async function signIn(email: string, password: string) {
    const result = await api<{ token: string }>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password })
    });
    const profile = await api<User>("/api/users/me", {}, result.token);
    sessionStorage.setItem(storageKey, result.token);
    setToken(result.token);
    setUser(profile);
    setReady(true);
  }

  async function register(displayName: string, email: string, password: string) {
    await api<User>("/api/users/register", {
      method: "POST",
      body: JSON.stringify({ displayName, email, password })
    });
    await signIn(email, password);
  }

  async function signOut() {
    try {
      if (token) await api<void>("/api/auth/logout", { method: "DELETE" }, token);
    } finally {
      sessionStorage.removeItem(storageKey);
      setToken(null);
      setUser(null);
    }
  }

  return <AuthContext.Provider value={{ token, user, ready, signIn, register, signOut, updateUser: setUser }}>
    {children}
  </AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error("AuthProvider is missing");
  return context;
}
