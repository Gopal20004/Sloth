import { useEffect, useRef, useState, type FormEvent } from "react";
import { ArrowLeft, ArrowRight, MessageCircle, MessagesSquare, Plus, Send, Trash2, Video as VideoIcon } from "lucide-react";
import { Link, useParams } from "react-router-dom";
import { api, apiUrl, errorText, formatDate } from "../api";
import { useAuth } from "../auth";
import { EmptyState, ErrorNotice, Loading, VideoCard, VideoUpload } from "../components";
import type { ChatMessage, Game, Page, Post, Reply, Video } from "../types";
import { GameIdentity } from "../GameIdentity";

type Tab = "discussions" | "chat" | "videos";

export default function GamePage() {
  const { slug = "" } = useParams();
  const [game, setGame] = useState<Game | null>(null);
  const [tab, setTab] = useState<Tab>("discussions");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    setGame(null); setLoading(true); setError(""); setTab("discussions");
    api<Game>(`/api/games/${encodeURIComponent(slug)}`)
      .then((result) => { if (active) setGame(result); })
      .catch((reason) => { if (active) setError(errorText(reason)); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [slug]);

  if (loading) return <div className="container page"><Loading label="Loading community..." /></div>;
  if (!game) return <div className="container page"><ErrorNotice message={error || "Game not found"} /><Link to="/" className="text-link">Back to games</Link></div>;

  return <div className="container page game-page">
    <Link to="/" className="back-link"><ArrowLeft size={16} /> All games</Link>
    <div className="game-hero"><div className="game-hero-mark"><GameIdentity slug={game.slug} name={game.name} /></div><div className="game-hero-content">
      <span className="section-kicker">GAME COMMUNITY</span><h1>{game.name}</h1><p>{game.description}</p>
      <div className="game-hero-tags"><span><MessagesSquare size={15} /> Discussions</span><span><MessageCircle size={15} /> Live chat</span><span><VideoIcon size={15} /> Clips</span></div>
    </div></div>
    <div className="tabs" role="tablist" aria-label="Community sections">
      <button role="tab" aria-selected={tab === "discussions"} className={tab === "discussions" ? "active" : ""} onClick={() => setTab("discussions")}><MessagesSquare size={18} /> Discussions</button>
      <button role="tab" aria-selected={tab === "chat"} className={tab === "chat" ? "active" : ""} onClick={() => setTab("chat")}><MessageCircle size={18} /> Live chat</button>
      <button role="tab" aria-selected={tab === "videos"} className={tab === "videos" ? "active" : ""} onClick={() => setTab("videos")}><VideoIcon size={18} /> Clips</button>
    </div>
    {tab === "discussions" && <DiscussionTab slug={slug} />}
    {tab === "chat" && <ChatTab slug={slug} />}
    {tab === "videos" && <VideoTab slug={slug} />}
  </div>;
}

function DiscussionTab({ slug }: { slug: string }) {
  const { token, user } = useAuth();
  const [posts, setPosts] = useState<Post[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [selected, setSelected] = useState<number | null>(null);
  const [replies, setReplies] = useState<Reply[]>([]);
  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [replyBody, setReplyBody] = useState("");
  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  async function loadPosts(nextPage = 0) {
    try {
      const result = await api<Page<Post>>(`/api/games/${encodeURIComponent(slug)}/posts?page=${nextPage}&size=20`);
      setPosts((current) => nextPage ? [...current, ...result.content] : result.content);
      setPage(nextPage); setHasMore(nextPage + 1 < result.totalPages); setError("");
    } catch (reason) { setError(errorText(reason)); }
    finally { setLoading(false); }
  }

  useEffect(() => { void loadPosts(); }, [slug]);

  async function openPost(id: number) {
    if (selected === id) { setSelected(null); return; }
    setSelected(id); setReplies([]);
    try {
      const result = await api<Page<Reply>>(`/api/posts/${id}/replies?size=50`);
      setReplies(result.content);
    } catch (reason) { setError(errorText(reason)); }
  }

  async function createPost(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setBusy(true); setError("");
    try {
      const post = await api<Post>(`/api/games/${encodeURIComponent(slug)}/posts`, { method: "POST", body: JSON.stringify({ title, body }) }, token);
      setPosts((current) => [post, ...current]); setTitle(""); setBody(""); setSelected(post.id); setReplies([]);
    } catch (reason) { setError(errorText(reason)); }
    finally { setBusy(false); }
  }

  async function createReply(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); if (selected == null) return;
    setBusy(true); setError("");
    try {
      const reply = await api<Reply>(`/api/posts/${selected}/replies`, { method: "POST", body: JSON.stringify({ body: replyBody }) }, token);
      setReplies((current) => [...current, reply]); setReplyBody("");
    } catch (reason) { setError(errorText(reason)); }
    finally { setBusy(false); }
  }

  async function deletePost(id: number) {
    if (!window.confirm("Delete this discussion and its replies?")) return;
    try {
      await api<void>(`/api/posts/${id}`, { method: "DELETE" }, token);
      setPosts((current) => current.filter((post) => post.id !== id));
      if (selected === id) setSelected(null);
    } catch (reason) { setError(errorText(reason)); }
  }

  async function deleteReply(id: number) {
    try {
      await api<void>(`/api/replies/${id}`, { method: "DELETE" }, token);
      setReplies((current) => current.filter((reply) => reply.id !== id));
    } catch (reason) { setError(errorText(reason)); }
  }

  return <section className="tab-content"><div className="section-heading"><div><span className="section-kicker">THE CONVERSATION</span><h2>Discussions</h2><p>Ask a question, share a story, or help another player.</p></div></div>
    {token ? <form className="panel post-composer" onSubmit={createPost}><div className="composer-heading"><Plus size={19} /><h3>Start a discussion</h3></div>
      <label className="field"><span>Title</span><input value={title} onChange={(event) => setTitle(event.target.value)} required maxLength={160} placeholder="What's on your mind?" /></label>
      <label className="field"><span>Your post</span><textarea value={body} onChange={(event) => setBody(event.target.value)} required maxLength={5000} rows={3} placeholder="Tell the community more..." /></label>
      <button className="button primary" disabled={busy}>{busy ? "Posting..." : "Post discussion"} <ArrowRight size={17} /></button></form> :
      <div className="callout"><MessagesSquare size={20} /><span><Link to="/login">Log in</Link> to start a discussion.</span></div>}
    {error && <ErrorNotice message={error} />}
    {loading ? <Loading label="Loading discussions..." /> : posts.length ? <div className="post-list">{posts.map((post) => <article className="post-card panel" key={post.id}>
      <div className="post-meta"><span className="avatar-small">{post.authorName.slice(0, 1).toUpperCase()}</span><Link to={`/users/${post.authorId}`}>{post.authorName}</Link><span>·</span><time>{formatDate(post.createdAt)}</time>
        {user?.id === post.authorId && <button className="icon-button danger push-right" title="Delete discussion" onClick={() => deletePost(post.id)}><Trash2 size={16} /></button>}</div>
      <button className="post-open" onClick={() => openPost(post.id)} aria-expanded={selected === post.id}><h3>{post.title}</h3><p>{post.body}</p><span>{selected === post.id ? "Hide replies" : "View replies"} <ArrowRight size={15} /></span></button>
      {selected === post.id && <div className="reply-section"><h4>Replies</h4>{replies.length ? replies.map((reply) => <div className="reply" key={reply.id}><span className="avatar-small">{reply.authorName.slice(0, 1).toUpperCase()}</span><div><div className="post-meta"><Link to={`/users/${reply.authorId}`}>{reply.authorName}</Link><time>{formatDate(reply.createdAt)}</time></div><p>{reply.body}</p></div>
        {user?.id === reply.authorId && <button className="icon-button danger push-right" title="Delete reply" onClick={() => deleteReply(reply.id)}><Trash2 size={15} /></button>}</div>) : <p className="muted">No replies yet. Be the first.</p>}
        {token ? <form className="reply-form" onSubmit={createReply}><input value={replyBody} onChange={(event) => setReplyBody(event.target.value)} maxLength={2000} required placeholder="Write a reply..." /><button className="button secondary" disabled={busy}><Send size={16} /> Reply</button></form> : <p className="muted"><Link to="/login">Log in</Link> to reply.</p>}
      </div>}
    </article>)}</div> : <EmptyState icon={<MessagesSquare size={28} />} title="Start the conversation" text="No discussions yet. Your post could be the first." />}
    {hasMore && <button className="button ghost centered" onClick={() => loadPosts(page + 1)}>Load more discussions</button>}
  </section>;
}

function appendMessage(current: ChatMessage[], incoming: ChatMessage): ChatMessage[] {
  if (current.some((message) => message.id === incoming.id)) return current;
  return [...current, incoming].sort((a, b) => a.id - b.id).slice(-100);
}

function ChatTab({ slug }: { slug: string }) {
  const { token } = useAuth();
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [body, setBody] = useState("");
  const [connected, setConnected] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const bottom = useRef<HTMLDivElement>(null);

  useEffect(() => {
    let active = true;
    api<Page<ChatMessage>>(`/api/games/${encodeURIComponent(slug)}/chat/messages?size=100`)
      .then((page) => { if (active) setMessages(page.content.reverse()); })
      .catch((reason) => { if (active) setError(errorText(reason)); });
    const stream = new EventSource(apiUrl(`/api/games/${encodeURIComponent(slug)}/chat/stream`));
    stream.onopen = () => { setConnected(true); };
    stream.onerror = () => { setConnected(false); };
    stream.addEventListener("message", (event) => {
      const incoming = JSON.parse((event as MessageEvent).data) as ChatMessage;
      setMessages((current) => appendMessage(current, incoming));
    });
    return () => { active = false; stream.close(); };
  }, [slug]);

  useEffect(() => { bottom.current?.scrollIntoView({ behavior: "smooth", block: "end" }); }, [messages.length]);

  async function send(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setBusy(true); setError("");
    try {
      const message = await api<ChatMessage>(`/api/games/${encodeURIComponent(slug)}/chat/messages`, { method: "POST", body: JSON.stringify({ body }) }, token);
      setMessages((current) => appendMessage(current, message)); setBody("");
    } catch (reason) { setError(errorText(reason)); }
    finally { setBusy(false); }
  }

  return <section className="tab-content"><div className="section-heading"><div><span className="section-kicker">RIGHT NOW</span><h2>Live chat</h2><p>Jump into the conversation with other players.</p></div><span className={`live-indicator ${connected ? "online" : ""}`}><span />{connected ? "Live" : "Connecting"}</span></div>
    <div className="chat-panel panel"><div className="chat-messages">{messages.length ? messages.map((message) => <div className="chat-message" key={message.id}><span className="avatar-small">{message.senderName.slice(0, 1).toUpperCase()}</span><div><div className="post-meta"><Link to={`/users/${message.senderId}`}>{message.senderName}</Link><time>{formatDate(message.sentAt)}</time></div><p>{message.body}</p></div></div>) : <EmptyState icon={<MessageCircle size={28} />} title="It's quiet in here" text="Say hello and get the chat going." />}<div ref={bottom} /></div>
      {error && <ErrorNotice message={error} />}
      {token ? <form className="chat-compose" onSubmit={send}><input value={body} onChange={(event) => setBody(event.target.value)} maxLength={1000} required placeholder="Message the community..." aria-label="Chat message" /><button className="button primary" disabled={busy}><Send size={17} /><span>Send</span></button></form> : <div className="chat-signin"><Link to="/login">Log in</Link> to join the conversation.</div>}
    </div>
  </section>;
}

function VideoTab({ slug }: { slug: string }) {
  const [videos, setVideos] = useState<Video[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  async function load() {
    try {
      const result = await api<Page<Video>>(`/api/games/${encodeURIComponent(slug)}/videos?size=30`);
      setVideos(result.content); setError("");
    } catch (reason) { setError(errorText(reason)); }
    finally { setLoading(false); }
  }
  useEffect(() => { void load(); }, [slug]);

  return <section className="tab-content"><div className="section-heading"><div><span className="section-kicker">THE HIGHLIGHTS</span><h2>Community clips</h2><p>The moments this community wanted to share.</p></div></div>
    <VideoUpload gameSlug={slug} onUploaded={load} />
    {error && <ErrorNotice message={error} />}
    {loading ? <Loading label="Loading clips..." /> : videos.length ? <div className="video-grid">{videos.map((video) => <VideoCard key={video.id} video={video} onDeleted={load} />)}</div> :
      <EmptyState icon={<VideoIcon size={28} />} title="No clips yet" text="Share the first highlight for this game." />}
  </section>;
}
