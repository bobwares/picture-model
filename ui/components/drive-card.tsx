/**
 * App: Picture Model
 * Package: ui/components
 * File: drive-card.tsx
 * Version: 0.1.0
 * Turns: 1
 * Author: Codex
 * Date: 2026-01-29T22:11:12Z
 * Exports: DriveCard
 * Description: Card displaying drive status, metadata, and actions.
 */
'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { driveApi } from '@/lib/api-client';
import type { RemoteFileDrive } from '@/types';
import { HardDrive, Wifi, WifiOff, AlertCircle, FolderTree, Tag } from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';

interface DriveCardProps {
  drive: RemoteFileDrive;
}

export function DriveCard({ drive }: DriveCardProps) {
  const queryClient = useQueryClient();

  const connectMutation = useMutation({
    mutationFn: () => driveApi.connect(drive.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['drives'] });
    },
  });

  const disconnectMutation = useMutation({
    mutationFn: () => driveApi.disconnect(drive.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['drives'] });
    },
  });

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
          </div>
        </div>

        {/* Actions */}
        <div className="flex flex-wrap items-center gap-2 ml-4">
          {drive.status === 'CONNECTED' ? (
            <>
              <button className="flex items-center space-x-1 rounded-lg bg-[var(--accent-soft)] px-3 py-2 text-sm font-semibold text-[var(--accent-ink)] transition hover:brightness-95">
                <FolderTree className="h-4 w-4" />
                <span>Browse Tree</span>
              </button>
              <button className="flex items-center space-x-1 rounded-lg bg-[var(--surface-muted)] px-3 py-2 text-sm font-semibold text-[var(--text)] transition hover:bg-[var(--surface-strong)]">
                <Tag className="h-4 w-4" />
                <span>View by Tags</span>
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
              <button className="rounded-lg bg-[var(--surface-muted)] px-3 py-2 text-sm font-semibold text-[var(--text)] transition hover:bg-[var(--surface-strong)]">
                Edit
              </button>
              <button className="rounded-lg bg-rose-50 px-3 py-2 text-sm font-semibold text-rose-700 transition hover:bg-rose-100">
                Delete
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
