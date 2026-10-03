import { useEffect, useState } from "react";
import { ArrowRight, Gamepad2, MessageCircle, Search, Sparkles, Users, Video } from "lucide-react";
import { Link } from "react-router-dom";
import { api, errorText } from "../api";
import { EmptyState, ErrorNotice, GameTile, Loading } from "../components";
import type { Game, Page } from "../types";

export default function HomePage() {
  const [games, setGames] = useState<Game[]>([]);
  const [results, setResults] = useState<Game[] | null>(null);
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [searching, setSearching] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    api<Page<Game>>("/api/games/popular?size=20")
      .then((page) => setGames(page.content))
      .catch((reason) => setError(errorText(reason)))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (!query.trim()) { setResults(null); setSearching(false); return; }
    const controller = new AbortController();
    const timeout = setTimeout(() => {
      setSearching(true);
      api<Page<Game>>(`/api/games/search?q=${encodeURIComponent(query.trim())}&size=20`, { signal: controller.signal })
        .then((page) => setResults(page.content))
        .catch((reason) => { if (!controller.signal.aborted) setError(errorText(reason)); })
        .finally(() => { if (!controller.signal.aborted) setSearching(false); });
    }, 250);
    return () => { clearTimeout(timeout); controller.abort(); };
  }, [query]);

  const shown = results ?? games;
  return <>
    <section className="hero container">
      <div className="hero-copy">
        <div className="eyebrow"><Sparkles size={15} /> YOUR NEXT COMMUNITY STARTS HERE</div>
        <h1>Every game<br />has a <em>home.</em></h1>
        <p>Find your people. Swap stories, catch the conversation, and share the moments worth replaying.</p>
        <a className="button primary hero-button" href="#explore">Explore games <ArrowRight size={18} /></a>
      </div>
      <div className="hero-visual" aria-hidden="true">
        <div className="hero-halo" /><div className="hero-orbit orbit-one" /><div className="hero-orbit orbit-two" />
        <div className="hero-center"><Gamepad2 size={76} strokeWidth={1.5} /></div>
        <div className="floating-pill pill-top"><MessageCircle size={17} /> Live conversations</div>
        <div className="floating-pill pill-bottom"><Video size={17} /> Moments that matter</div>
      </div>
    </section>

    <section className="explore-section container" id="explore">
      <div className="section-heading"><div><span className="section-kicker">DISCOVER</span><h2>Find your game</h2><p>Step into a community that already speaks your language.</p></div>
        <span className="section-count">{games.length} games to explore</span></div>
      <label className="search-box"><Search size={21} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search games..." aria-label="Search games" />
        {searching && <span className="searching-label">Searching</span>}</label>
      {error && <ErrorNotice message={error} onRetry={() => window.location.reload()} />}
      {loading ? <Loading label="Finding games..." /> : shown.length > 0 ?
        <div className="game-grid">{shown.map((game, index) => <GameTile key={game.id} game={game} index={index} />)}</div> :
        <EmptyState icon={<Search size={28} />} title="No games found" text="Try a different search term." />}
    </section>

    <section className="feature-strip container">
      <div><MessageCircle size={22} /><h3>Talk it out</h3><p>Join discussions and live chat for every game.</p></div>
      <div><Users size={22} /><h3>Find your crew</h3><p>Make a private server and invite your people.</p></div>
      <div><Video size={22} /><h3>Show the highlight</h3><p>Share clips with your game and your profile.</p></div>
      <Link to="/servers" className="feature-link">Explore servers <ArrowRight size={17} /></Link>
    </section>
  </>;
}
