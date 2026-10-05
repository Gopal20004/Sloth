import { MessageCircle, Pause, Play, Sparkles, Users } from "lucide-react";
import { useEffect, useRef, useState, type PointerEvent } from "react";
import { useInView, useMotionValue, useReducedMotion, useSpring, useTransform } from "motion/react";
import * as m from "motion/react-m";

// Spring-smoothed transforms tilt the original SVG in perspective without a WebGL renderer.
export default function ControllerScene() {
  const scene = useRef<HTMLDivElement>(null);
  const visible = useInView(scene, { amount: 0.15 });
  const reduce = useReducedMotion();
  const [paused, setPaused] = useState(false);
  const [desktop, setDesktop] = useState(false);
  const [pageVisible, setPageVisible] = useState(!document.hidden);
  // Small/touch screens get the complete composition without a continuous animation loop.
  useEffect(() => {
    const media = window.matchMedia("(min-width: 761px) and (hover: hover) and (pointer: fine)");
    const updateDevice = () => setDesktop(media.matches);
    const updateVisibility = () => setPageVisible(!document.hidden);
    updateDevice();
    media.addEventListener("change", updateDevice);
    document.addEventListener("visibilitychange", updateVisibility);
    return () => {
      media.removeEventListener("change", updateDevice);
      document.removeEventListener("visibilitychange", updateVisibility);
    };
  }, []);
  const active = desktop && visible && pageVisible && !reduce && !paused;
  const pointerX = useMotionValue(0);
  const pointerY = useMotionValue(0);
  const rotateX = useSpring(pointerX, { stiffness: 100, damping: 22 });
  const rotateY = useSpring(pointerY, { stiffness: 100, damping: 22 });

  // Opposing foreground/background movement gives depth with just two transformed layers.
  const backdropX = useTransform(rotateY, (value) => -value * 1.5);
  const backdropY = useTransform(rotateX, (value) => value * 1.5);

  function resetTilt() { pointerX.set(0); pointerY.set(0); }
  function tilt(event: PointerEvent<HTMLDivElement>) {
    if (!active || event.pointerType !== "mouse") return;
    const bounds = event.currentTarget.getBoundingClientRect();
    pointerX.set(-((event.clientY - bounds.top) / bounds.height - 0.5) * 12);
    pointerY.set(((event.clientX - bounds.left) / bounds.width - 0.5) * 16);
  }
  useEffect(() => {
    if (!active) { pointerX.set(0); pointerY.set(0); }
  }, [active, pointerX, pointerY]);

  return <div ref={scene} className="scene-stage" data-motion={active ? "active" : "still"}>
    <div className="controller-scene" aria-hidden="true" onPointerMove={tilt} onPointerLeave={resetTilt}>
    <span className="scene-watermark">PLAY</span>
    <div className="scene-grid" /><div className="scene-glow" />
    <m.div className="scene-depth" style={{ x: active ? backdropX : 0, y: active ? backdropY : 0 }}>
      <div className="scene-portal"><span /><span /><span /></div>
      <div className="scene-stars" />
      <span className="scene-shard shard-one" /><span className="scene-shard shard-two" />
      <span className="scene-coordinate">SLOTH / PLAYER SPACE</span>
    </m.div>
    <m.div className="controller-float" style={{ rotateX: active ? rotateX : 0, rotateY: active ? rotateY : 0 }}
      animate={{ y: active ? [-6, 9, -6] : 0 }}
      transition={{ y: { duration: active ? 7 : 0, repeat: active ? Infinity : 0, ease: "easeInOut" } }}>
      <svg className="controller-object" viewBox="0 0 500 360" fill="none">
        <defs>
          <linearGradient id="shell" x1="140" y1="75" x2="360" y2="305" gradientUnits="userSpaceOnUse"><stop stopColor="#ede5ff" /><stop offset=".35" stopColor="#a89abd" /><stop offset=".7" stopColor="#6e627f" /><stop offset="1" stopColor="#322b48" /></linearGradient>
          <linearGradient id="shell-edge" x1="250" y1="100" x2="250" y2="320" gradientUnits="userSpaceOnUse"><stop stopColor="#726081" /><stop offset="1" stopColor="#151321" /></linearGradient>
          <linearGradient id="touch" x1="212" y1="112" x2="280" y2="178" gradientUnits="userSpaceOnUse"><stop stopColor="#49435d" /><stop offset="1" stopColor="#1b192b" /></linearGradient>
          <radialGradient id="stick"><stop stopColor="#696379" /><stop offset=".7" stopColor="#292535" /><stop offset="1" stopColor="#100e1d" /></radialGradient>
        </defs>
        <ellipse cx="255" cy="315" rx="155" ry="19" fill="#07060c" opacity=".6" />
        <path d="M141 105C99 108 87 155 77 205L65 270C61 301 91 324 114 298L160 250H340L386 298C409 324 439 301 435 270L423 205C413 155 401 108 359 105Z" fill="url(#shell-edge)" />
        <path d="M142 89C100 91 88 138 77 189L65 252C61 282 91 308 115 280L161 232H339L385 280C409 308 439 282 435 252L423 189C412 138 400 91 358 89Z" fill="url(#shell)" stroke="#f4eaff" strokeOpacity=".38" strokeWidth="2" />
        <path d="M111 105Q137 73 184 91M316 91Q363 73 389 105" stroke="#292334" strokeWidth="14" strokeLinecap="round" />
        <path d="M201 108H299L290 165H210Z" fill="url(#touch)" stroke="#ede2ff" strokeOpacity=".3" />
        <path d="M203 105H298" stroke="#cdff8a" strokeWidth="4" strokeLinecap="round" />
        <circle cx="179" cy="212" r="33" fill="#a79cb7" /><circle cx="179" cy="211" r="27" fill="url(#stick)" /><circle cx="179" cy="209" r="18" stroke="#a293b9" strokeOpacity=".35" strokeWidth="2" />
        <circle cx="319" cy="212" r="33" fill="#a79cb7" /><circle cx="319" cy="211" r="27" fill="url(#stick)" /><circle cx="319" cy="209" r="18" stroke="#a293b9" strokeOpacity=".35" strokeWidth="2" />
        <path d="M135 120H152V138H170V155H152V173H135V155H117V138H135Z" fill="#252132" stroke="#c4b5da" strokeOpacity=".45" strokeWidth="2" />
        <g fill="#2a2438" stroke="#dacaf0" strokeOpacity=".5"><circle cx="357" cy="122" r="12" /><circle cx="381" cy="146" r="12" /><circle cx="357" cy="170" r="12" /><circle cx="333" cy="146" r="12" /></g>
        <path d="M353 125L357 117L361 125Z" stroke="#caff8c" strokeWidth="1.5" /><circle cx="381" cy="146" r="4" stroke="#fc9dad" strokeWidth="1.5" /><path d="M353 166L361 174M361 166L353 174" stroke="#b7b3ff" strokeWidth="1.5" /><path d="M329 142H337V150H329Z" stroke="#c9a7ff" strokeWidth="1.5" />
        <circle cx="250" cy="196" r="10" fill="#554b65" /><path d="M246 196H254M250 192V200" stroke="#e7daf9" strokeWidth="2" />
        <path d="M89 239L111 190M410 239L388 190" stroke="#eae1ff" strokeOpacity=".17" strokeWidth="3" strokeLinecap="round" />
      </svg>
    </m.div>
    <div className="scene-note note-party"><span className="scene-note-icon"><Users size={18} /></span><div><strong>Your next party</strong><small>Starts with a hello.</small></div><span className="note-dot" /></div>
    <div className="scene-note note-chat"><MessageCircle size={17} /><span>Same game. New friends.</span></div>
    <span className="scene-spark"><Sparkles size={24} /></span>
    <span className="scene-caption">GOOD GAMES ARE BETTER TOGETHER</span>
    </div>
    {desktop && !reduce && <button type="button" className="scene-motion-toggle" aria-pressed={paused}
      aria-label="Pause scene motion" onClick={() => setPaused((value) => !value)}>
      {paused ? <Play size={12} /> : <Pause size={12} />}{paused ? "Motion paused" : "Pause motion"}
    </button>}
  </div>;
}
