/**
 * App: Picture Model
 * Package: ui/app
 * File: page.tsx
 * Version: 0.1.2
 * Turns: 1,5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T08:34:12Z
 * Exports: DashboardPage
 * Description: Dashboard landing page for drives, stats, and recent activity.
 */
'use client';

import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { driveApi, systemApi } from '@/lib/api-client';
import { DriveCard } from '@/components/drive-card';
import { StatsCard } from '@/components/stats-card';
import { RecentActivity } from '@/components/recent-activity';
import { Header } from '@/components/header';
import { AddDriveModal } from '@/components/add-drive-modal';
import { HardDrive, Image as ImageIcon, Tag as TagIcon, Activity } from 'lucide-react';

export default function DashboardPage() {
  const [isAddDriveModalOpen, setIsAddDriveModalOpen] = useState(false);
  const { data: drives, isLoading: drivesLoading } = useQuery({
    queryKey: ['drives'],
    queryFn: async () => {
      const response = await driveApi.getAll();
      return response.data;
    },
  });

  const { data: systemStatus } = useQuery({
    queryKey: ['system-status'],
    queryFn: async () => {
      const response = await systemApi.getStatus();
      return response.data;
    },
  });

  return (
    <div className="min-h-screen">
      <Header />

      <main className="mx-auto flex max-w-7xl flex-col gap-10 px-4 py-10 sm:px-6 lg:px-8">
        <section className="relative overflow-hidden rounded-3xl border border-[var(--border)] bg-white/70 p-8 shadow-sm backdrop-blur animate-rise">
          <div className="absolute -right-16 -top-16 h-36 w-36 rounded-full bg-[var(--accent-soft)] opacity-70" />
          <div className="relative flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.3em] text-[var(--muted)]">
                Dashboard
              </p>
              <h1 className="mt-3 text-3xl font-semibold text-[var(--text)] sm:text-4xl">
                Remote drive control center
              </h1>
              <p className="mt-3 max-w-2xl text-sm text-[var(--muted)] sm:text-base">
                Monitor connections, trigger crawls, and stay on top of your image library across every drive.
              </p>
            </div>
            <button
              onClick={() => setIsAddDriveModalOpen(true)}
              className="rounded-lg bg-[var(--accent)] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[var(--accent-strong)]"
            >
              + Add New Drive
            </button>
          </div>
        </section>

        <section className="grid grid-cols-4 gap-4">
          <StatsCard
            title="Total Images"
            value={systemStatus?.totalImages?.toLocaleString() || '0'}
            icon={<ImageIcon className="h-5 w-5" />}
          />
          <StatsCard
            title="Total Drives"
            value={systemStatus?.totalDrives || 0}
            icon={<HardDrive className="h-5 w-5" />}
          />
          <StatsCard
            title="Total Tags"
            value={systemStatus?.totalTags || 0}
            icon={<TagIcon className="h-5 w-5" />}
          />
          <StatsCard
            title="Active Crawls"
            value={systemStatus?.activeCrawls || 0}
            icon={<Activity className="h-5 w-5" />}
          />
        </section>

        <section className="flex flex-col gap-4">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-xl font-semibold text-[var(--text)]">Remote File Drives</h2>
              <p className="text-sm text-[var(--muted)]">
                Connect, browse, and manage your active storage locations.
              </p>
            </div>
          </div>

          {drivesLoading ? (
            <div className="rounded-2xl border border-[var(--border)] bg-white/70 p-12 text-center shadow-sm backdrop-blur">
              <div className="mx-auto h-10 w-10 animate-spin rounded-full border-2 border-[var(--border)] border-t-[var(--accent)]" />
              <p className="mt-3 text-sm text-[var(--muted)]">Loading drives...</p>
            </div>
          ) : drives && drives.length > 0 ? (
            <div className="space-y-4">
              {drives.map((drive) => (
                <DriveCard key={drive.id} drive={drive} />
              ))}
            </div>
          ) : (
            <div className="rounded-2xl border border-dashed border-[var(--border)] bg-white/70 p-12 text-center shadow-sm backdrop-blur">
              <HardDrive className="mx-auto h-12 w-12 text-[var(--muted)]" />
              <h3 className="mt-3 text-base font-semibold text-[var(--text)]">No drives configured</h3>
              <p className="mt-1 text-sm text-[var(--muted)]">
                Add a remote drive to start indexing your images.
              </p>
              <div className="mt-6">
                <button
                  onClick={() => setIsAddDriveModalOpen(true)}
                  className="rounded-lg bg-[var(--accent)] px-4 py-2 text-sm font-semibold text-white transition hover:bg-[var(--accent-strong)]"
                >
                  + Add Your First Drive
                </button>
              </div>
            </div>
          )}
        </section>

        <RecentActivity />
      </main>

      <AddDriveModal
        isOpen={isAddDriveModalOpen}
        onClose={() => setIsAddDriveModalOpen(false)}
      />
    </div>
  );
}
