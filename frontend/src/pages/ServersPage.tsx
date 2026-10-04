import { useEffect, useState, type FormEvent } from "react";
import { ArrowRight, Copy, Crown, Plus, Shield, Trash2, UserMinus, Users } from "lucide-react";
import { Link } from "react-router-dom";
import { api, errorText, formatDate } from "../api";
import { useAuth } from "../auth";
import { EmptyState, ErrorNotice, Loading } from "../components";
import type { Invite, Page, Server, ServerMember } from "../types";

export default function ServersPage() {
  const { user, token } = useAuth();
  const [servers, setServers] = useState<Server[]>([]);
  const [selected, setSelected] = useState<Server | null>(null);
  const [members, setMembers] = useState<ServerMember[]>([]);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [joinCode, setJoinCode] = useState("");
  const [invite, setInvite] = useState<Invite | null>(null);
  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [copied, setCopied] = useState(false);

  async function loadServers(preferredId?: number) {
    try {
      const page = await api<Page<Server>>("/api/servers/mine?size=50", {}, token);
      setServers(page.content);
      setSelected((current) => page.content.find((server) => server.id === (preferredId ?? current?.id)) ?? page.content[0] ?? null);
      setError("");
    } catch (reason) { setError(errorText(reason)); }
    finally { setLoading(false); }
  }

  useEffect(() => { void loadServers(); }, [token]);
  useEffect(() => {
    if (!selected) { setMembers([]); return; }
    let active = true;
    api<Page<ServerMember>>(`/api/servers/${selected.id}/members?size=50`, {}, token)
      .then((page) => { if (active) setMembers(page.content); })
      .catch((reason) => { if (active) setError(errorText(reason)); });
    setInvite(null); setCopied(false);
    return () => { active = false; };
  }, [selected?.id, token]);

  async function create(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setBusy(true); setError("");
    try {
      const server = await api<Server>("/api/servers", { method: "POST", body: JSON.stringify({ name, description }) }, token);
      setName(""); setDescription(""); await loadServers(server.id);
    } catch (reason) { setError(errorText(reason)); }
    finally { setBusy(false); }
  }

  async function join(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setBusy(true); setError("");
    try {
      const server = await api<Server>("/api/servers/join", { method: "POST", body: JSON.stringify({ code: joinCode }) }, token);
      setJoinCode(""); await loadServers(server.id);
    } catch (reason) { setError(errorText(reason)); }
    finally { setBusy(false); }
  }

  async function createInvite() {
    if (!selected) return;
    setBusy(true); setError("");
    try { setInvite(await api<Invite>(`/api/servers/${selected.id}/invites`, { method: "POST" }, token)); }
    catch (reason) { setError(errorText(reason)); }
    finally { setBusy(false); }
  }

  async function revokeInvite() {
    if (!selected || !invite) return;
    try {
      await api<void>(`/api/servers/${selected.id}/invites/${invite.id}`, { method: "DELETE" }, token);
      setInvite(null);
    } catch (reason) { setError(errorText(reason)); }
  }

  async function leaveOrDelete() {
    if (!selected) return;
    const owner = selected.ownerId === user?.id;
    if (!window.confirm(owner ? "Delete this server and remove all members?" : "Leave this server?")) return;
    try {
      await api<void>(owner ? `/api/servers/${selected.id}` : `/api/servers/${selected.id}/members/me`, { method: "DELETE" }, token);
      setSelected(null); await loadServers();
    } catch (reason) { setError(errorText(reason)); }
  }

  async function removeMember(memberId: number) {
    if (!selected || !window.confirm("Remove this member?")) return;
    try {
      await api<void>(`/api/servers/${selected.id}/members/${memberId}`, { method: "DELETE" }, token);
      setMembers((current) => current.filter((member) => member.userId !== memberId));
    } catch (reason) { setError(errorText(reason)); }
  }

  async function copyInvite() {
    if (!invite) return;
    try { await navigator.clipboard.writeText(invite.code); setCopied(true); }
    catch { setError("Couldn't copy the code. Select it and copy it manually."); }
  }

  if (!token || !user) return <div className="container page"><EmptyState icon={<Users size={28} />} title="Find your crew" text="Log in to create or join a server." action={<Link to="/login" className="button primary">Log in <ArrowRight size={17} /></Link>} /></div>;

  return <div className="container page servers-page">
    <div className="page-intro"><span className="section-kicker">YOUR CREW</span><h1>Servers</h1><p>Create a space for your people, or join one with an invite.</p></div>
    {error && <ErrorNotice message={error} />}
    <div className="server-layout">
      <aside className="server-sidebar"><div className="panel server-list"><h2>Your servers</h2>{loading ? <Loading /> : servers.length ? servers.map((server) =>
        <button key={server.id} className={`server-list-item ${selected?.id === server.id ? "active" : ""}`} onClick={() => setSelected(server)}><span className="server-icon">{server.name.slice(0, 1).toUpperCase()}</span><span>{server.name}</span>{server.ownerId === user.id && <Crown size={15} />}</button>) : <p className="muted">No servers yet.</p>}</div>
        <form className="panel stack-form" onSubmit={create}><div className="form-intro"><Plus size={21} /><div><h3>Create a server</h3><p>Start a private group.</p></div></div>
          <label className="field"><span>Name</span><input value={name} onChange={(event) => setName(event.target.value)} maxLength={100} required placeholder="Your crew's name" /></label>
          <label className="field"><span>Description</span><textarea value={description} onChange={(event) => setDescription(event.target.value)} maxLength={1000} rows={2} placeholder="What's this group about?" /></label>
          <button className="button primary full" disabled={busy}>Create server</button></form>
        <form className="panel stack-form" onSubmit={join}><div className="form-intro"><Users size={21} /><div><h3>Join a server</h3><p>Have an invite code?</p></div></div>
          <label className="field"><span>Invite code</span><input value={joinCode} onChange={(event) => setJoinCode(event.target.value)} required placeholder="Paste your code" /></label>
          <button className="button secondary full" disabled={busy}>Join server</button></form>
      </aside>

      <section className="server-detail">{selected ? <>
        <div className="server-detail-head panel"><div className="server-detail-symbol">{selected.name.slice(0, 1).toUpperCase()}</div><div><span className="section-kicker">{selected.ownerId === user.id ? "YOU OWN THIS SERVER" : "MEMBER"}</span><h2>{selected.name}</h2><p>{selected.description || "A place for your crew."}</p></div></div>
        <div className="panel members-panel"><div className="panel-heading"><div><h3>Members</h3><p>{members.length} people in this server</p></div><Users size={21} /></div>
          {members.map((member) => <div className="member-row" key={member.userId}><span className="avatar-small">{member.displayName.slice(0, 1).toUpperCase()}</span><div><Link to={`/users/${member.userId}`}>{member.displayName}</Link><small>Joined {formatDate(member.joinedAt)}</small></div>{member.userId === selected.ownerId && <span className="owner-badge"><Crown size={13} /> Owner</span>}
            {selected.ownerId === user.id && member.userId !== user.id && <button className="icon-button danger push-right" title="Remove member" onClick={() => removeMember(member.userId)}><UserMinus size={17} /></button>}</div>)}
        </div>
        {selected.ownerId === user.id && <div className="panel invite-panel"><div className="panel-heading"><div><h3>Invite players</h3><p>Invite codes are valid for seven days.</p></div><Shield size={21} /></div>
          {invite ? <div className="invite-code"><code>{invite.code}</code><button className="button secondary" onClick={copyInvite}><Copy size={16} /> {copied ? "Copied" : "Copy"}</button><button className="text-button danger" onClick={revokeInvite}>Revoke</button></div> : <button className="button secondary" disabled={busy} onClick={createInvite}><Plus size={17} /> Create invite code</button>}</div>}
        <button className="text-button danger leave-button" onClick={leaveOrDelete}><Trash2 size={16} /> {selected.ownerId === user.id ? "Delete server" : "Leave server"}</button>
      </> : <EmptyState icon={<Users size={28} />} title="A place for your people" text="Create a server or join one to see it here." />}</section>
    </div>
  </div>;
}
