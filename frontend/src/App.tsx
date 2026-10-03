import { useEffect, useState, type ReactNode } from "react";
import { ArrowRight, Gamepad2, Home, LogOut, Menu, UserRound, Users, X } from "lucide-react";
import { Link, Navigate, Route, Routes, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "./auth";
import { Loading } from "./components";
import AuthPage from "./pages/AuthPage";
import GamePage from "./pages/GamePage";
import HomePage from "./pages/HomePage";
import ProfilePage from "./pages/ProfilePage";
import ServersPage from "./pages/ServersPage";

function ScrollToTop() {
  const { pathname } = useLocation();
  useEffect(() => { window.scrollTo(0, 0); }, [pathname]);
  return null;
}

function RequireAuth({ children }: { children: ReactNode }) {
  const { token, ready } = useAuth();
  const location = useLocation();
  if (!ready) return <div className="container page"><Loading /></div>;
  if (!token) return <Navigate to={`/login?next=${encodeURIComponent(location.pathname)}`} replace />;
  return children;
}

function Header() {
  const { user, signOut } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [open, setOpen] = useState(false);
  useEffect(() => { setOpen(false); }, [location.pathname]);

  async function logout() {
    try { await signOut(); } catch { /* local session has already been cleared */ }
    navigate("/");
  }

  return <header className="site-header"><div className="container header-inner">
    <Link to="/" className="brand" aria-label="Sloth home"><span className="brand-mark"><Gamepad2 size={22} strokeWidth={2.3} /></span><span>sloth<span className="brand-dot">.</span></span></Link>
    <nav className={`main-nav ${open ? "open" : ""}`} aria-label="Main navigation">
      <Link className={location.pathname === "/" ? "active" : ""} to="/"><Home size={17} /> Explore</Link>
      <Link className={location.pathname.startsWith("/servers") ? "active" : ""} to="/servers"><Users size={17} /> Servers</Link>
      {user && <Link className={location.pathname === "/profile" ? "active" : ""} to="/profile"><UserRound size={17} /> Profile</Link>}
    </nav>
    <div className="header-actions">{user ? <><Link className="header-user" to="/profile"><span className="avatar-small">{user.displayName.slice(0, 1).toUpperCase()}</span><span>{user.displayName}</span></Link><button className="icon-button" title="Log out" aria-label="Log out" onClick={logout}><LogOut size={18} /></button></> : <><Link className="text-link header-login" to="/login">Login</Link><Link className="button primary header-join" to="/register">Join Sloth <ArrowRight size={16} /></Link></>}
      <button className="icon-button menu-toggle" aria-label={open ? "Close menu" : "Open menu"} aria-expanded={open} onClick={() => setOpen((value) => !value)}>{open ? <X size={22} /> : <Menu size={22} />}</button>
    </div>
  </div></header>;
}

function Footer() {
  return <footer className="site-footer"><div className="container footer-inner"><Link to="/" className="brand"><span className="brand-mark"><Gamepad2 size={18} /></span><span>sloth<span className="brand-dot">.</span></span></Link><p>Every game has a home. Every player has a voice.</p><span>Made for the players.</span></div></footer>;
}

export default function App() {
  return <div className="app-shell"><ScrollToTop /><Header /><main className="site-main"><Routes>
    <Route path="/" element={<HomePage />} />
    <Route path="/games/:slug" element={<GamePage />} />
    <Route path="/servers" element={<RequireAuth><ServersPage /></RequireAuth>} />
    <Route path="/profile" element={<RequireAuth><ProfilePage /></RequireAuth>} />
    <Route path="/users/:id" element={<ProfilePage />} />
    <Route path="/login" element={<AuthPage mode="login" />} />
    <Route path="/register" element={<AuthPage mode="register" />} />
    <Route path="*" element={<div className="container page not-found"><h1>Lost in the lobby?</h1><p>That page doesn't exist.</p><Link className="button primary" to="/">Back to games <ArrowRight size={17} /></Link></div>} />
  </Routes></main><Footer /></div>;
}
