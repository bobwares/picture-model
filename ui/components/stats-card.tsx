/**
 * App: Picture Model
 * Package: ui/components
 * File: stats-card.tsx
 * Version: 0.1.0
 * Turns: 1
 * Author: Codex
 * Date: 2026-01-29T22:11:12Z
 * Exports: StatsCard
 * Description: Highlight card for dashboard statistics.
 */
interface StatsCardProps {
  title: string;
  value: string | number;
  icon: React.ReactNode;
}

export function StatsCard({ title, value, icon }: StatsCardProps) {
  return (
    <div className="group relative overflow-hidden rounded-2xl border border-[var(--border)] bg-white/75 p-6 shadow-sm backdrop-blur transition hover:-translate-y-0.5 hover:shadow-md">
      <div className="absolute inset-0 opacity-0 transition group-hover:opacity-100">
        <div className="absolute -right-10 -top-10 h-24 w-24 rounded-full bg-[var(--accent-soft)] opacity-60" />
      </div>
      <div className="relative flex items-center justify-between">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wide text-[var(--muted)]">{title}</p>
          <p className="mt-3 text-3xl font-semibold text-[var(--text)]">{value}</p>
        </div>
        <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-[var(--surface-muted)] text-[var(--accent-ink)]">
          {icon}
        </div>
      </div>
    </div>
  );
}
