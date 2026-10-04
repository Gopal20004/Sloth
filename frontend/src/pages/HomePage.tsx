import { useEffect, useState } from "react";
import { ArrowRight, MessageCircle, Search, Sparkles, Users, Video } from "lucide-react";
import { Link } from "react-router-dom";
import { api, errorText } from "../api";
import { EmptyState, ErrorNotice, GameTile, Loading } from "../components";
import type { Game, Page } from "../types";
import ControllerScene from "../ControllerScene";

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
        <div className="eyebrow"><span className="eyebrow-dot" /> YOUR PLAYER TWO IS OUT THERE</div>
        <h1>Good games.<br /><em>Great company.</em></h1>
        <p>Find your game. Meet your crew. Share the moments that make you say, “one more round.”</p>
        <div className="hero-cta"><a className="button primary hero-button" href="#explore">Find your community <ArrowRight size={18} /></a><Link className="button ghost" to="/servers"><Users size={17} /> Find your crew</Link></div>
        <div className="hero-footnote"><Sparkles size={14} /> Discussions, live chat & your best plays. All in one place.</div>
      </div>
      <ControllerScene />
    </section>

    <section className="explore-section container" id="explore">
      <div className="section-heading"><div><span className="section-kicker">PICK YOUR WORLD</span><h2>Where do you play?</h2><p>Every game has a community. Find yours.</p></div>
        <span className="section-count">{games.length} communities · endless conversations</span></div>
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
