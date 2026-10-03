import { useState, type FormEvent } from "react";
import { ArrowRight, Gamepad2, LockKeyhole } from "lucide-react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { errorText } from "../api";
import { useAuth } from "../auth";
import { ErrorNotice } from "../components";

export default function AuthPage({ mode }: { mode: "login" | "register" }) {
  const { signIn, register } = useAuth();
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const [displayName, setDisplayName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const isRegister = mode === "register";

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setBusy(true); setError("");
    try {
      if (isRegister) await register(displayName, email, password);
      else await signIn(email, password);
      const next = params.get("next");
      navigate(next?.startsWith("/") && !next.startsWith("//") ? next : "/");
    } catch (reason) { setError(errorText(reason)); }
    finally { setBusy(false); }
  }

  return <div className="auth-page container">
    <div className="auth-art"><div className="auth-art-orbit" /><Gamepad2 size={86} strokeWidth={1.2} /><h2>Find your place<br />in every game.</h2><p>Good games are better together.</p></div>
    <div className="auth-panel panel">
      <div className="auth-icon"><LockKeyhole size={24} /></div>
      <span className="section-kicker">{isRegister ? "JOIN THE COMMUNITY" : "WELCOME BACK"}</span>
      <h1>{isRegister ? "Create your account" : "Log in to Sloth"}</h1>
      <p>{isRegister ? "One account. A whole world of communities." : "Your games and your people are waiting."}</p>
      <form onSubmit={submit} className="stack-form">
        {isRegister && <label className="field"><span>Display name</span><input autoComplete="nickname" value={displayName} onChange={(event) => setDisplayName(event.target.value)} required maxLength={50} placeholder="How players will know you" /></label>}
        <label className="field"><span>Email</span><input type="email" autoComplete="email" value={email} onChange={(event) => setEmail(event.target.value)} required placeholder="you@example.com" /></label>
        <label className="field"><span>Password</span><input type="password" autoComplete={isRegister ? "new-password" : "current-password"} value={password} onChange={(event) => setPassword(event.target.value)} required minLength={isRegister ? 8 : undefined} placeholder="At least 8 characters" /></label>
        {error && <ErrorNotice message={error} />}
        <button className="button primary full" disabled={busy}>{busy ? "Please wait..." : isRegister ? "Create account" : "Log in"}<ArrowRight size={17} /></button>
      </form>
      <div className="auth-switch">{isRegister ? "Already have an account?" : "New to Sloth?"} <Link to={isRegister ? "/login" : "/register"}>{isRegister ? "Log in" : "Create an account"}</Link></div>
    </div>
  </div>;
}
