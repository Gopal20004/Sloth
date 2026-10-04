import { Box } from "lucide-react";

const identities: Record<string, { logo?: string; genre: string; hue: number }> = {
  minecraft: { genre: "BUILD · EXPLORE", hue: 140 },
  fortnite: { logo: "fortnite", genre: "BATTLE ROYALE", hue: 265 },
  roblox: { logo: "roblox", genre: "CREATE · DISCOVER", hue: 211 },
  valorant: { logo: "valorant", genre: "TACTICAL SHOOTER", hue: 351 },
  "league-of-legends": { logo: "leagueoflegends", genre: "TEAM STRATEGY", hue: 41 },
  "counter-strike-2": { logo: "counterstrike", genre: "TACTICAL SHOOTER", hue: 27 },
  "elden-ring": { genre: "ACTION RPG", hue: 47 },
  "grand-theft-auto-v": { genre: "OPEN WORLD", hue: 153 }
};

export function gameIdentity(slug: string) {
  return identities[slug] ?? { genre: "GAME COMMUNITY", hue: 265 };
}

// Local SVGs identify supported brands; full game titles remain the fallback.
export function GameIdentity({ slug, name }: { slug: string; name: string }) {
  const { logo } = gameIdentity(slug);
  if (logo) return <img className="game-logo" src={`/game-logos/${logo}.svg`} alt="" />;
  return <div className={`game-wordmark wordmark-${slug}`} aria-hidden="true">
    {slug === "minecraft" && <Box size={40} strokeWidth={1.5} />}
    {slug === "elden-ring" && <span className="ring-sigil" />}
    <span>{name}</span>
  </div>;
}
