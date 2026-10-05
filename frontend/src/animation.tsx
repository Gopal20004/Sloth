import type { ReactNode } from "react";
import { LazyMotion, MotionConfig, domAnimation, useReducedMotion } from "motion/react";
import * as m from "motion/react-m";

// Load only animation/gesture features; drag and layout projection are unnecessary here.
export function MotionProvider({ children }: { children: ReactNode }) {
  return <MotionConfig reducedMotion="user"><LazyMotion features={domAnimation} strict>{children}</LazyMotion></MotionConfig>;
}

export function Reveal({ children, className, delay = 0 }: { children: ReactNode; className?: string; delay?: number }) {
  const reduce = useReducedMotion();
  return <m.div className={className}
    initial={reduce ? false : { opacity: 0, y: 18 }}
    whileInView={{ opacity: 1, y: 0 }}
    viewport={{ once: true, amount: 0.12 }}
    transition={{ duration: reduce ? 0 : 0.45, delay: reduce ? 0 : delay, ease: [0.22, 1, 0.36, 1] }}>
    {children}
  </m.div>;
}

export function PageTransition({ children }: { children: ReactNode }) {
  const reduce = useReducedMotion();
  // Enter immediately on navigation. Avoid retaining old forms or chat subscriptions for exit effects.
  return <m.div className="route-transition"
    initial={reduce ? false : { opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }}
    transition={{ duration: reduce ? 0 : 0.24, ease: "easeOut" }}>{children}</m.div>;
}
