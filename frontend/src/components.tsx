import { useState, type CSSProperties, type FormEvent, type ReactNode } from "react";
import { ArrowRight, CircleAlert, Film, LoaderCircle, UploadCloud, X } from "lucide-react";
import { Link } from "react-router-dom";
import { api, apiUrl, errorText, formatDate } from "./api";
import { useAuth } from "./auth";
import type { Game, Video } from "./types";

export function Loading({ label = "Loading..." }: { label?: string }) {
  return <div className="state-box"><LoaderCircle className="spin" size={24} /><span>{label}</span></div>;
}

export function ErrorNotice({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return <div className="notice error-notice" role="alert">
    <CircleAlert size={18} /> <span>{message}</span>
    {onRetry && <button className="text-button" onClick={onRetry}>Try again</button>}
  </div>;
}

export function EmptyState({ icon, title, text, action }: {
  icon?: ReactNode; title: string; text: string; action?: ReactNode;
}) {
  return <div className="empty-state">
    <div className="empty-icon">{icon ?? <Film size={26} />}</div>
    <h3>{title}</h3><p>{text}</p>{action}
  </div>;
}

const palette = [268, 207, 348, 28, 153, 234, 43, 310];
export function GameTile({ game, index = 0 }: { game: Game; index?: number }) {
  const hue = palette[index % palette.length];
  return <Link to={`/games/${game.slug}`} className="game-tile" style={{ "--game-hue": hue } as CSSProperties}>
    <div className="game-art">
      {game.coverUrl ? <img src={game.coverUrl} alt="" /> : <span>{game.name.slice(0, 2).toUpperCase()}</span>}
      <div className="game-art-orbit" />
    </div>
    <div className="game-tile-info"><div><h3>{game.name}</h3><p>{game.description}</p></div><ArrowRight size={18} /></div>
  </Link>;
}

export function VideoCard({ video, onDeleted }: { video: Video; onDeleted?: () => void }) {
  const { token, user } = useAuth();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  async function remove() {
    if (!window.confirm("Delete this clip?")) return;
    setBusy(true); setError("");
    try {
      await api<void>(`/api/videos/${video.id}`, { method: "DELETE" }, token);
      onDeleted?.();
    } catch (reason) { setError(errorText(reason)); }
    finally { setBusy(false); }
  }

  return <article className="video-card">
    <video controls preload="metadata" src={apiUrl(video.fileUrl)} aria-label={video.title} />
    <div className="video-card-body">
      <div className="video-card-title"><h3>{video.title}</h3>
        {user?.id === video.ownerId && <button className="icon-button danger" disabled={busy} title="Delete clip" onClick={remove}><X size={17} /></button>}
      </div>
      <p>by <Link to={`/users/${video.ownerId}`}>{video.ownerName}</Link> · {formatDate(video.createdAt)}</p>
      {error && <small className="error-text">{error}</small>}
    </div>
  </article>;
}

export function VideoUpload({ gameSlug, onUploaded }: { gameSlug?: string; onUploaded: () => void }) {
  const { token } = useAuth();
  const [title, setTitle] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formElement = event.currentTarget;
    if (!file) return;
    if (file.size > 100 * 1024 * 1024) { setError("Clips must be 100 MB or smaller."); return; }
    setBusy(true); setError("");
    try {
      const form = new FormData();
      form.set("title", title.trim());
      if (gameSlug) form.set("gameSlug", gameSlug);
      form.set("file", file);
      await api<Video>("/api/videos", { method: "POST", body: form }, token);
      setTitle(""); setFile(null); formElement.reset();
      onUploaded();
    } catch (reason) { setError(errorText(reason)); }
    finally { setBusy(false); }
  }

  if (!token) return <div className="callout"><UploadCloud size={20} /><span><Link to="/login">Sign in</Link> to share a clip.</span></div>;
  return <form className="upload-form panel" onSubmit={submit}>
    <div className="form-intro"><UploadCloud size={21} /><div><h3>Share a clip</h3><p>MP4 or WebM, up to 100 MB</p></div></div>
    <div className="form-row">
      <label className="field"><span>Clip title</span><input value={title} onChange={(event) => setTitle(event.target.value)} maxLength={160} required placeholder="An unforgettable moment" /></label>
      <label className="field file-field"><span>Video file</span><input type="file" accept="video/mp4,video/webm" onChange={(event) => setFile(event.target.files?.[0] ?? null)} required /></label>
    </div>
    {error && <ErrorNotice message={error} />}
    <button className="button primary" disabled={busy || !file}>{busy ? "Uploading..." : "Upload clip"}</button>
  </form>;
}
