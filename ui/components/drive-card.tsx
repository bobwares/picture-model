/**
 * App: Picture Model
 * Package: ui/components
 * File: drive-card.tsx
 * Version: 0.1.7
 * Turns: 1,5,8,10,17,23
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-02-02T02:13:59Z
 * Exports: DriveCard
 * Description: Card displaying drive status, metadata, and actions.
 */
'use client';

import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { crawlerApi, driveApi } from '@/lib/api-client';
import type { RemoteFileDrive } from '@/types';
import { HardDrive, Wifi, WifiOff, AlertCircle, FolderTree, Play } from 'lucide-react';
import { format, formatDistanceStrict, formatDistanceToNow } from 'date-fns';
import { EditDriveModal } from '@/components/edit-drive-modal';
import { CrawlHistoryModal } from '@/components/crawl-history-modal';
import Link from 'next/link';
import { useRouter } from 'next/navigation';

interface DriveCardProps {
  drive: RemoteFileDrive;
}

export function DriveCard({ drive }: DriveCardProps) {
  const queryClient = useQueryClient();
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [isHistoryOpen, setIsHistoryOpen] = useState(false);
  const router = useRouter();

  const connectMutation = useMutation({
    mutationFn: () => driveApi.connect(drive.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['drives'] });
      router.push(`/tree/${drive.id}`);
    },
    onError: (err: any) => {
      window.alert(err.response?.data?.message || 'Failed to connect drive');
    },
  });

  const disconnectMutation = useMutation({
    mutationFn: () => driveApi.disconnect(drive.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['drives'] });
    },
    onError: (err: any) => {
      window.alert(err.response?.data?.message || 'Failed to disconnect drive');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => driveApi.delete(drive.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['drives'] });
    },
    onError: (err: any) => {
      window.alert(err.response?.data?.message || 'Failed to delete drive');
    },
  });

  const startCrawlMutation = useMutation({
    mutationFn: () => crawlerApi.startCrawl({ driveId: drive.id }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['drives'] });
      queryClient.invalidateQueries({ queryKey: ['drive-latest-job', drive.id] });
      queryClient.invalidateQueries({ queryKey: ['drive-crawl-history', drive.id] });
    },
    onError: (err: any) => {
      window.alert(err.response?.data?.message || 'Failed to start crawl');
    },
  });

  const handleDelete = () => {
    const confirmed = window.confirm(`Delete drive "${drive.name}"? This cannot be undone.`);
    if (!confirmed) {
      return;
    }
    deleteMutation.mutate();
  };

  const getStatusColor = () => {
    switch (drive.status) {
      case 'CONNECTED':
        return 'text-emerald-700 bg-emerald-50 border-emerald-200';
      case 'CONNECTING':
        return 'text-amber-700 bg-amber-50 border-amber-200';
      case 'ERROR':
        return 'text-rose-700 bg-rose-50 border-rose-200';
      default:
        return 'text-slate-600 bg-slate-50 border-slate-200';
    }
  };

  const getStatusIcon = () => {
    switch (drive.status) {
      case 'CONNECTED':
        return <Wifi className="h-4 w-4" />;
      case 'CONNECTING':
        return <Wifi className="h-4 w-4 animate-pulse" />;
      case 'ERROR':
        return <AlertCircle className="h-4 w-4" />;
      default:
        return <WifiOff className="h-4 w-4" />;
    }
  };

  const getDriveIcon = () => {
    const iconClass = 'h-5 w-5 text-[var(--muted)]';
    return <HardDrive className={iconClass} />;
  };

  const { data: latestJob } = useQuery({
    queryKey: ['drive-latest-job', drive.id],
    queryFn: async () => {
      const response = await crawlerApi.listJobsByDrive(drive.id, 0, 1);
      return response.data.content[0] ?? null;
    },
    refetchInterval: (query) => {
      const data = query.state.data as (typeof latestJob) | undefined;
      if (!data) {
        return false;
      }
      return data.status === 'IN_PROGRESS' || data.status === 'PENDING' ? 4000 : false;
    },
  });

  const latestProgress = Math.max(0, Math.min(100, latestJob?.progressPercentage ?? 0));
  const latestStartDate = latestJob?.startTime ? new Date(latestJob.startTime) : null;
  const latestEndDate = latestJob?.endTime ? new Date(latestJob.endTime) : null;

  return (
    <div className="group relative overflow-hidden rounded-2xl border border-[var(--border)] bg-white/75 p-6 shadow-sm backdrop-blur transition hover:-translate-y-0.5 hover:shadow-md">
      <div className="absolute inset-x-0 top-0 h-1 bg-gradient-to-r from-transparent via-[var(--accent)]/60 to-transparent opacity-0 transition group-hover:opacity-100" />
      <div className="flex items-start justify-between">
        <div className="flex items-start space-x-4 flex-1">
          {/* Drive Icon */}
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[var(--surface-muted)]">
            {getDriveIcon()}
          </div>

          {/* Drive Info */}
          <div className="flex-1 min-w-0">
            <div className="flex items-center space-x-3">
              <h3 className="text-lg font-semibold text-[var(--text)] truncate">{drive.name}</h3>
              <span
                className={`inline-flex items-center space-x-1 rounded-full border px-2 py-1 text-xs font-semibold ${getStatusColor()}`}
              >
                {getStatusIcon()}
                <span>{drive.status}</span>
              </span>
            </div>

            <p className="mt-1 text-sm text-[var(--muted)] truncate">{drive.connectionUrl}</p>

            <div className="mt-3 flex flex-wrap items-center gap-x-6 gap-y-2 text-sm text-[var(--muted)]">
              <span>Images: {drive.imageCount.toLocaleString()}</span>
              {drive.lastCrawled && (
                <span>
                  Last Crawled: {formatDistanceToNow(new Date(drive.lastCrawled), { addSuffix: true })}
                </span>
              )}
            </div>

            <div className="mt-4 rounded-xl border border-[var(--border)] bg-white/70 p-4">
              <div className="flex flex-wrap items-center justify-between gap-2 text-xs text-[var(--muted)]">
                <span className="font-semibold text-[var(--text)]">Latest Crawl</span>
                <button
                  onClick={() => setIsHistoryOpen(true)}
                  className="text-xs font-semibold text-[var(--accent-ink)] transition hover:underline"
                >
                  View crawl history
                </button>
              </div>
              {latestJob ? (
                <div className="mt-2">
                  <p className="text-sm font-semibold text-[var(--text)]">
                    Status: {latestJob.status.replace('_', ' ')}
                  </p>
                  {latestStartDate && (
                    <p className="text-xs text-[var(--muted)]">
                      Started {format(latestStartDate, 'MMM d, yyyy h:mm a')} • Elapsed{' '}
                      {formatDistanceStrict(latestStartDate, latestEndDate ?? new Date())}
                    </p>
                  )}
                  <p className="text-xs text-[var(--muted)]">
                    {latestJob.filesProcessed} files processed
                  </p>
                  {latestJob.status === 'IN_PROGRESS' && (
                    <div className="mt-2">
                      <div className="h-2 w-full overflow-hidden rounded-full bg-[var(--surface-muted)]">
                        <div
                          className="h-full animate-pulse rounded-full bg-[var(--accent)]"
                          style={{ width: `${latestProgress}%` }}
                        />
                      </div>
                      <p className="mt-2 text-xs font-semibold text-[var(--accent-ink)]">
                        {latestProgress.toFixed(0)}% complete
                      </p>
                      {latestJob.currentPath && (
                        <p className="mt-2 truncate text-xs text-[var(--muted)]">
                          Processing {latestJob.currentPath}
                        </p>
                      )}
                    </div>
                  )}
                </div>
              ) : (
                <p className="mt-2 text-xs text-[var(--muted)]">No crawls yet.</p>
              )}
            </div>
          </div>
        </div>

        {/* Actions */}
        <div className="flex flex-wrap items-center gap-2 ml-4">
          {drive.status === 'CONNECTED' ? (
            <>
              <Link
                href={`/tree/${drive.id}`}
                className="flex items-center space-x-1 rounded-lg bg-[var(--accent-soft)] px-3 py-2 text-sm font-semibold text-[var(--accent-ink)] transition hover:brightness-95"
              >
                <FolderTree className="h-4 w-4" />
                <span>Browse Tree</span>
              </Link>
              <button
                onClick={() => startCrawlMutation.mutate()}
                disabled={startCrawlMutation.isPending}
                className="flex items-center space-x-1 rounded-lg bg-[var(--accent)] px-3 py-2 text-sm font-semibold text-white transition hover:bg-[var(--accent-strong)] disabled:opacity-50"
              >
                <Play className="h-4 w-4" />
                <span>{startCrawlMutation.isPending ? 'Starting...' : 'Start Crawl'}</span>
              </button>
              <button
                onClick={() => setIsEditOpen(true)}
                className="rounded-lg bg-[var(--surface-muted)] px-3 py-2 text-sm font-semibold text-[var(--text)] transition hover:bg-[var(--surface-strong)]"
              >
                Edit
              </button>
              <button
                onClick={() => disconnectMutation.mutate()}
                disabled={disconnectMutation.isPending}
                className="rounded-lg bg-[var(--surface-muted)] px-3 py-2 text-sm font-semibold text-[var(--muted)] transition hover:bg-[var(--surface-strong)] disabled:opacity-50"
              >
                {disconnectMutation.isPending ? 'Disconnecting...' : 'Disconnect'}
              </button>
            </>
          ) : (
            <>
              <button
                onClick={() => connectMutation.mutate()}
                disabled={connectMutation.isPending || drive.status === 'CONNECTING'}
                className="rounded-lg bg-[var(--accent)] px-3 py-2 text-sm font-semibold text-white transition hover:bg-[var(--accent-strong)] disabled:opacity-50"
              >
                {connectMutation.isPending || drive.status === 'CONNECTING' ? 'Connecting...' : 'Connect'}
              </button>
              <button
                onClick={() => setIsEditOpen(true)}
                className="rounded-lg bg-[var(--surface-muted)] px-3 py-2 text-sm font-semibold text-[var(--text)] transition hover:bg-[var(--surface-strong)]"
              >
                Edit
              </button>
              <button
                onClick={handleDelete}
                disabled={deleteMutation.isPending}
                className="rounded-lg bg-rose-50 px-3 py-2 text-sm font-semibold text-rose-700 transition hover:bg-rose-100 disabled:opacity-50"
              >
                {deleteMutation.isPending ? 'Deleting...' : 'Delete'}
              </button>
            </>
          )}
        </div>
      </div>
      <EditDriveModal
        isOpen={isEditOpen}
        onClose={() => setIsEditOpen(false)}
        drive={drive}
      />
      <CrawlHistoryModal
        isOpen={isHistoryOpen}
        onClose={() => setIsHistoryOpen(false)}
        driveId={drive.id}
        driveName={drive.name}
      />
    </div>
  );
}
