/**
 * App: Picture Model
 * Package: ui/app/tree
 * File: page.tsx
 * Version: 0.1.0
 * Turns: 1
 * Author: Codex
 * Date: 2026-01-29T22:11:12Z
 * Exports: TreePage
 * Description: Directory tree view placeholder with styled empty state.
 */
'use client';

import { Header } from '@/components/header';
import { FolderTree } from 'lucide-react';

export default function TreePage() {
  return (
    <div className="min-h-screen">
      <Header />
      <main className="mx-auto max-w-5xl px-4 py-10 sm:px-6 lg:px-8">
        <div className="rounded-3xl border border-[var(--border)] bg-white/70 px-6 py-12 text-center shadow-sm backdrop-blur animate-rise sm:px-12">
          <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-[var(--surface-muted)] text-[var(--accent-ink)]">
            <FolderTree className="h-7 w-7" />
          </div>
          <h2 className="mt-6 text-2xl font-semibold text-[var(--text)]">Directory Tree</h2>
          <p className="mt-2 text-sm text-[var(--muted)] sm:text-base">
            Browse your images by folder structure with a live tree sidebar.
          </p>
          <p className="mt-4 text-xs uppercase tracking-[0.2em] text-[var(--muted)]">
            Tree navigation coming soon
          </p>
        </div>
      </main>
    </div>
  );
}
