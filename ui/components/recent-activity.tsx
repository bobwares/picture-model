/**
 * App: Picture Model
 * Package: ui/components
 * File: recent-activity.tsx
 * Version: 0.1.0
 * Turns: 1
 * Author: Codex
 * Date: 2026-01-29T22:11:12Z
 * Exports: RecentActivity
 * Description: Recent crawl activity list with status indicators.
 */
'use client';

import { useQuery } from '@tanstack/react-query';
import { crawlerApi } from '@/lib/api-client';
import { Activity, CheckCircle, XCircle, Clock } from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';

export function RecentActivity() {
  const { data: jobs } = useQuery({
    queryKey: ['recent-jobs'],
    queryFn: async () => {
      const response = await crawlerApi.listJobs(0, 5);
      return response.data.content;
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
      <h2 className="text-xl font-semibold text-[var(--text)] mb-4">Recent Activity</h2>

      {jobs && jobs.length > 0 ? (
        <div className="space-y-4">
          {jobs.map((job) => (
            <div key={job.id} className="flex items-center space-x-4 border-b border-[var(--border)] py-3 last:border-b-0">
              <div className="flex-shrink-0">{getStatusIcon(job.status)}</div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-semibold text-[var(--text)]">
                  Crawl {job.status.toLowerCase().replace('_', ' ')} on {job.driveName}
                </p>
                <p className="text-sm text-[var(--muted)]">
                  {job.filesProcessed} files processed • {formatDistanceToNow(new Date(job.startTime), { addSuffix: true })}
                </p>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <p className="text-sm text-[var(--muted)] text-center py-8">No recent activity</p>
      )}
    </div>
  );
}
