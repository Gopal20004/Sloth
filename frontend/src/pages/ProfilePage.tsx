import { useEffect, useState, type FormEvent } from "react";
import { CalendarDays, UserRound, Video as VideoIcon } from "lucide-react";
import { useParams } from "react-router-dom";
import { api, errorText, formatDate } from "../api";
import { useAuth } from "../auth";
import { EmptyState, ErrorNotice, Loading, VideoCard, VideoUpload } from "../components";
import type { Page, PublicUser, User, Video } from "../types";

export default function ProfilePage() {
  const { id } = useParams();
  const { user, token, updateUser } = useAuth();
  const profileId = id ? Number(id) : user?.id;
  const own = profileId === user?.id;
  const [profile, setProfile] = useState<PublicUser | null>(null);
  const [videos, setVideos] = useState<Video[]>([]);
  const [name, setName] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    if (!profileId || !Number.isInteger(profileId)) { setLoading(false); setError("Player not found"); return; }
    let active = true;
    setLoading(true); setError("");
    Promise.all([
      api<PublicUser>(`/api/users/public/${profileId}`),
      api<Page<Video>>(`/api/users/${profileId}/videos?size=30`)
    ]).then(([player, clips]) => {
      if (active) { setProfile(player); setName(player.displayName); setVideos(clips.content); }
    }).catch((reason) => { if (active) setError(errorText(reason)); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [profileId]);

  async function refreshVideos() {
    if (!profileId) return;
    try {
      const page = await api<Page<Video>>(`/api/users/${profileId}/videos?size=30`);
      setVideos(page.content);
    } catch (reason) { setError(errorText(reason)); }
  }

  async function saveProfile(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setSaving(true); setError(""); setSaved(false);
    try {
      const updated = await api<User>("/api/users/me", { method: "PATCH", body: JSON.stringify({ displayName: name }) }, token);
      updateUser(updated);
      setProfile({ id: updated.id, displayName: updated.displayName, createdAt: updated.createdAt });
      setSaved(true);
    } catch (reason) { setError(errorText(reason)); }
    finally { setSaving(false); }
  }

  if (loading) return <div className="container page"><Loading label="Loading profile..." /></div>;
  if (error && !profile) return <div className="container page"><ErrorNotice message={error} /></div>;
  if (!profile) return null;

  return <div className="container page profile-page">
    <div className="profile-banner"><div className="profile-avatar"><UserRound size={38} /></div></div>
    <div className="profile-head"><div><span className="section-kicker">{own ? "YOUR SPACE" : "PLAYER PROFILE"}</span><h1>{profile.displayName}</h1><p><CalendarDays size={16} /> Joined {formatDate(profile.createdAt)}</p></div></div>

    {own && <section className="panel profile-edit"><div><h2>Edit profile</h2><p>Choose the name other players see.</p></div>
      <form onSubmit={saveProfile}><label className="field"><span>Display name</span><input value={name} onChange={(event) => setName(event.target.value)} maxLength={50} required /></label>
        <button className="button secondary" disabled={saving}>{saving ? "Saving..." : "Save changes"}</button></form>
      {saved && <p className="success-text">Profile updated.</p>}
      {error && <ErrorNotice message={error} />}
    </section>}

    <section className="content-section"><div className="section-heading"><div><span className="section-kicker">HIGHLIGHTS</span><h2>{own ? "Your clips" : `${profile.displayName}'s clips`}</h2></div><VideoIcon size={24} /></div>
      {own && <VideoUpload onUploaded={refreshVideos} />}
      {videos.length ? <div className="video-grid">{videos.map((video) => <VideoCard key={video.id} video={video} onDeleted={refreshVideos} />)}</div> :
        <EmptyState icon={<VideoIcon size={28} />} title="No clips yet" text={own ? "Upload your first highlight to start your collection." : "This player hasn't shared a clip yet."} />}
    </section>
  </div>;
}
