/**
 * App: Picture Model
 * Package: ui/components
 * File: recent-activity.tsx
 * Version: 0.1.6
 * Turns: 1,10,11,12,13,14,15
 * Author: Codex
 * Date: 2026-02-01T17:06:46Z
 * Exports: RecentActivity
 * Description: Recent crawl activity list with status indicators.
 */
'use client';

import { useQuery } from '@tanstack/react-query';
import { crawlerApi } from '@/lib/api-client';
import { Activity, CheckCircle, XCircle, Clock } from 'lucide-react';
import { format, formatDistanceStrict, formatDistanceToNow } from 'date-fns';

export function RecentActivity() {
  const { data: jobs } = useQuery({
    queryKey: ['recent-jobs'],
    queryFn: async () => {
      const response = await crawlerApi.listJobs(0, 5);
      return response.data.content;
    },
    refetchInterval: (query) => {
      const data = query.state.data as typeof jobs | undefined;
      if (!data) {
        return false;
      }
      return data.some((job) => job.status === 'IN_PROGRESS') ? 4000 : false;
    },
  });

  const getStatusIcon = (status: string) => {
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
  };

  return (
    <div className="rounded-2xl border border-[var(--border)] bg-white/75 p-6 shadow-sm backdrop-blur">
      <h2 className="text-xl font-semibold text-[var(--text)] mb-4">Crawl Summary</h2>

      {jobs && jobs.length > 0 ? (
        <div className="space-y-4">
          {jobs.map((job) => {
            const isActive = job.status === 'IN_PROGRESS';
            const timestamp = job.endTime ? job.endTime : job.startTime;
            const progress = Math.max(0, Math.min(100, job.progressPercentage ?? 0));
            const startDate = new Date(job.startTime);
            const endDate = job.endTime ? new Date(job.endTime) : new Date();
            const errors = job.errors ? parseErrors(job.errors) : [];
            return (
              <div key={job.id} className="border-b border-[var(--border)] py-3 last:border-b-0">
                <div className="flex items-center space-x-4">
                  <div className="flex-shrink-0">{getStatusIcon(job.status)}</div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-semibold text-[var(--text)]">
                      Crawl {job.status.toLowerCase().replace('_', ' ')}
                    </p>
                    <p className="text-xs font-semibold text-[var(--muted)]">
                      Drive: {job.driveName}
                    </p>
                    <p className="text-sm text-[var(--muted)]">
                      {job.filesProcessed} files processed • {formatDistanceToNow(new Date(timestamp), { addSuffix: true })}
                    </p>
                    <p className="text-xs text-[var(--muted)]">
                      Started {format(startDate, 'MMM d, yyyy h:mm a')} • Elapsed {formatDistanceStrict(startDate, endDate)}
                    </p>
                    {job.status === 'FAILED' && errors.length > 0 && (
                      <div className="mt-2 rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-xs text-rose-700">
                        {errors.map((error, index) => (
                          <p key={`${job.id}-error-${index}`} className="truncate">
                            {error}
                          </p>
                        ))}
                      </div>
                    )}
                    {isActive && (
                      <div className="mt-2">
                        <div className="h-2 w-full overflow-hidden rounded-full bg-[var(--surface-muted)]">
                          <div
                            className="h-full animate-pulse rounded-full bg-[var(--accent)]"
                            style={{ width: `${progress}%` }}
                          />
                        </div>
                        <p className="mt-2 text-xs font-semibold text-[var(--accent-ink)]">
                          {progress.toFixed(0)}% complete
                        </p>
                        {job.currentPath && (
                          <p className="mt-2 truncate text-xs text-[var(--muted)]">
                            Processing {job.currentPath}
                          </p>
                        )}
                      </div>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        <p className="text-sm text-[var(--muted)] text-center py-8">No recent activity</p>
      )}
    </div>
  );
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
