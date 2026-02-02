/**
 * App: Picture Model
 * Package: ui/components
 * File: crawl-history-modal.tsx
 * Version: 0.1.7
 * Turns: 17,18,19,20,21,23,24,25
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-02-02T02:22:59Z
 * Exports: CrawlHistoryModal
 * Description: Modal for viewing crawl history by drive. Methods: parseErrors - normalize error list.
 */
'use client';

import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { crawlerApi } from '@/lib/api-client';
import { Activity, CheckCircle, Clock, XCircle, X } from 'lucide-react';
import { format, formatDistanceStrict } from 'date-fns';
import type { CrawlJob } from '@/types';
import { createPortal } from 'react-dom';

interface CrawlHistoryModalProps {
  isOpen: boolean;
  onClose: () => void;
  driveId: string;
  driveName: string;
}

export function CrawlHistoryModal({ isOpen, onClose, driveId, driveName }: CrawlHistoryModalProps) {
  const queryClient = useQueryClient();
  const [isConfirmingClear, setIsConfirmingClear] = useState(false);

  const { data: jobs, isLoading } = useQuery({
    queryKey: ['drive-crawl-history', driveId],
    queryFn: async () => {
      const response = await crawlerApi.listJobsByDrive(driveId, 0, 20);
      return response.data.content;
    },
    enabled: isOpen,
    refetchInterval: (query) => {
      const data = query.state.data as CrawlJob[] | undefined;
      if (!data) {
        return false;
      }
      return data.some((job) => job.status === 'IN_PROGRESS' || job.status === 'PENDING') ? 4000 : false;
    },
  });

  const clearMutation = useMutation({
    mutationFn: () => crawlerApi.clearDriveHistory(driveId),
    onSuccess: () => {
      queryClient.setQueryData(['drive-crawl-history', driveId], []);
      queryClient.invalidateQueries({ queryKey: ['drive-crawl-history', driveId] });
      queryClient.invalidateQueries({ queryKey: ['drive-latest-job', driveId] });
      setIsConfirmingClear(false);
    },
    onError: (err: any) => {
      window.alert(err.response?.data?.message || 'Failed to clear crawl history');
    },
  });

  const handleClear = () => {
    setIsConfirmingClear(true);
  };

  if (!isOpen) return null;

  return createPortal(
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm transition-opacity" onClick={onClose} />

      <div className="flex min-h-full items-center justify-center p-4">
        <div className="relative flex w-full max-w-3xl flex-col rounded-2xl border border-[var(--border)] bg-white/90 p-6 shadow-xl backdrop-blur max-h-[90vh]">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h2 className="text-2xl font-semibold text-[var(--text)]">Crawl History</h2>
              <p className="text-sm text-[var(--muted)]">Drive: {driveName}</p>
            </div>
            <button
              onClick={onClose}
              className="rounded-full p-2 text-[var(--muted)] transition hover:bg-[var(--surface-muted)] hover:text-[var(--text)]"
            >
              <X className="h-6 w-6" />
            </button>
          </div>

          {isConfirmingClear && (
            <div className="absolute inset-0 z-10 flex items-center justify-center rounded-2xl bg-white/80 p-4 backdrop-blur-sm">
              <div className="w-full max-w-md rounded-xl border border-amber-200 bg-amber-50 p-5 text-sm text-amber-800 shadow-lg">
                <p className="font-semibold">Clear crawl history?</p>
                <p className="mt-1 text-xs">
                  This will remove all crawl records for "{driveName}".
                </p>
                <div className="mt-4 flex flex-wrap gap-2">
                  <button
                    onClick={() => clearMutation.mutate()}
                    disabled={clearMutation.isPending}
                    className="rounded-lg bg-rose-600 px-3 py-2 text-xs font-semibold text-white transition hover:bg-rose-700 disabled:opacity-50"
                  >
                    {clearMutation.isPending ? 'Clearing...' : 'Confirm Clear'}
                  </button>
                  <button
                    onClick={() => setIsConfirmingClear(false)}
                    className="rounded-lg bg-white px-3 py-2 text-xs font-semibold text-[var(--text)] transition hover:bg-[var(--surface-muted)]"
                  >
                    Cancel
                  </button>
                </div>
              </div>
            </div>
          )}

          <div className="max-h-[520px] space-y-3 overflow-y-auto pr-1">
            {isLoading ? (
              <div className="rounded-xl border border-[var(--border)] bg-white/70 p-6 text-center text-sm text-[var(--muted)]">
                Loading crawl history...
              </div>
            ) : jobs && jobs.length > 0 ? (
              jobs.map((job) => {
                const startDate = new Date(job.startTime);
                const endDate = job.endTime ? new Date(job.endTime) : new Date();
                const progress = Math.max(0, Math.min(100, job.progressPercentage ?? 0));
                const errors = job.errors ? parseErrors(job.errors) : [];
                return (
                  <div
                    key={job.id}
                    className="min-h-[92px] rounded-xl border border-[var(--border)] bg-white/70 p-4"
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex items-center space-x-3">
                        <div>{getStatusIcon(job.status)}</div>
                        <div>
                          <p className="text-sm font-semibold text-[var(--text)]">
                            {job.status.replace('_', ' ')}
                          </p>
                          <p className="text-xs text-[var(--muted)]">
                            Started {format(startDate, 'MMM d, yyyy h:mm a')} • Elapsed{' '}
                            {formatDistanceStrict(startDate, endDate)}
                          </p>
                        </div>
                      </div>
                      <div className="text-right text-xs text-[var(--muted)]">
                        <p>{job.filesProcessed} files processed</p>
                        {job.isIncremental && <p>Incremental crawl</p>}
                      </div>
                    </div>

                    {job.status === 'IN_PROGRESS' && (
                      <div className="mt-3">
                        <div className="h-2 w-full overflow-hidden rounded-full bg-[var(--surface-muted)]">
                          <div
                            className="h-full animate-pulse rounded-full bg-[var(--accent)]"
                            style={{ width: `${progress}%` }}
                          />
                        </div>
                        <p className="mt-2 text-xs font-semibold text-[var(--accent-ink)]">
                          {progress.toFixed(0)}% complete
                        </p>
                      </div>
                    )}

                    {job.status === 'FAILED' && errors.length > 0 && (
                      <div className="mt-3 rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-xs text-rose-700">
                        {errors.map((error, index) => (
                          <p key={`${job.id}-error-${index}`} className="truncate">
                            {error}
                          </p>
                        ))}
                      </div>
                    )}
                  </div>
                );
              })
            ) : (
              <div className="rounded-xl border border-[var(--border)] bg-white/70 p-6 text-center text-sm text-[var(--muted)]">
                No crawl history yet.
              </div>
            )}
          </div>

          <div className="mt-6 flex flex-wrap items-center justify-end gap-3">
            <button
              onClick={handleClear}
              disabled={clearMutation.isPending}
              className="rounded-lg bg-rose-50 px-4 py-2 text-sm font-semibold text-rose-700 transition hover:bg-rose-100 disabled:opacity-50"
            >
              {clearMutation.isPending ? 'Clearing...' : 'Clear History'}
            </button>
            <button
              onClick={onClose}
              className="rounded-lg bg-[var(--surface-muted)] px-4 py-2 text-sm font-semibold text-[var(--text)] transition hover:bg-[var(--surface-strong)]"
            >
              Close
            </button>
          </div>
        </div>
      </div>
    </div>,
    document.body
  );
}

function getStatusIcon(status: string) {
  switch (status) {
    case 'COMPLETED':
      return <CheckCircle className="h-5 w-5 text-green-600" />;
    case 'FAILED':
    case 'CANCELLED':
      return <XCircle className="h-5 w-5 text-red-600" />;
    case 'IN_PROGRESS':
      return <Activity className="h-5 w-5 text-blue-600 animate-pulse" />;
    default:
      return <Clock className="h-5 w-5 text-[var(--muted)]" />;
  }
}

function parseErrors(errors: string): string[] {
  try {
    const parsed = JSON.parse(errors);
    if (Array.isArray(parsed)) {
      return parsed.filter((entry) => typeof entry === 'string') as string[];
    }
  } catch (error) {
    return [errors];
  }
  return [errors];
}
