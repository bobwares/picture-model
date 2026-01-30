/**
 * App: Picture Model
 * Package: ui/components
 * File: add-drive-modal.tsx
 * Version: 0.1.0
 * Turns: 1
 * Author: Codex
 * Date: 2026-01-29T22:11:12Z
 * Exports: AddDriveModal
 * Description: Modal form for creating a new remote drive.
 */
'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { driveApi } from '@/lib/api-client';
import { X } from 'lucide-react';
import type { DriveType } from '@/types';

const createDriveSchema = z.object({
  name: z.string().min(1, 'Drive name is required').max(200, 'Name too long'),
  type: z.enum(['LOCAL', 'SMB', 'SFTP', 'FTP']),
  connectionUrl: z.string().min(1, 'Connection URL is required').max(500, 'URL too long'),
  rootPath: z.string().max(500, 'Path too long').default('/'),
  autoConnect: z.boolean().default(false),
  autoCrawl: z.boolean().default(false),
  credentials: z.string().optional(),
});

type CreateDriveForm = z.infer<typeof createDriveSchema>;

interface AddDriveModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export function AddDriveModal({ isOpen, onClose }: AddDriveModalProps) {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    watch,
  } = useForm<CreateDriveForm>({
    resolver: zodResolver(createDriveSchema),
    defaultValues: {
      rootPath: '/',
      autoConnect: false,
      autoCrawl: false,
    },
  });

  const driveType = watch('type');

  const createMutation = useMutation({
    mutationFn: async (data: CreateDriveForm) => {
      const response = await driveApi.create(data);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['drives'] });
      reset();
      onClose();
      setError(null);
    },
    onError: (err: any) => {
      setError(err.response?.data?.message || 'Failed to create drive');
    },
  });

  const onSubmit = (data: CreateDriveForm) => {
    setError(null);
    createMutation.mutate(data);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm transition-opacity"
        onClick={onClose}
      />

      {/* Modal */}
      <div className="flex min-h-full items-center justify-center p-4">
        <div className="relative w-full max-w-2xl rounded-2xl border border-[var(--border)] bg-white/90 p-6 shadow-xl backdrop-blur">
          {/* Header */}
          <div className="flex items-center justify-between mb-6">
            <div>
              <h2 className="text-2xl font-semibold text-[var(--text)]">Add New Drive</h2>
              <p className="text-sm text-[var(--muted)]">Connect a local or network drive to start indexing.</p>
            </div>
            <button
              onClick={onClose}
              className="rounded-full p-2 text-[var(--muted)] transition hover:bg-[var(--surface-muted)] hover:text-[var(--text)]"
            >
              <X className="h-6 w-6" />
            </button>
          </div>

          {/* Error Message */}
          {error && (
            <div className="mb-4 rounded-lg border border-rose-200 bg-rose-50 p-3">
              <p className="text-sm text-rose-700">{error}</p>
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            {/* Drive Name */}
            <div>
              <label htmlFor="name" className="block text-sm font-semibold text-[var(--text)] mb-1">
                Drive Name *
              </label>
              <input
                {...register('name')}
                type="text"
                id="name"
                placeholder="My Pictures Drive"
                className="w-full rounded-lg border border-[var(--border)] bg-white px-3 py-2 text-sm text-[var(--text)] placeholder:text-[var(--muted)] focus:outline-none focus:ring-2 focus:ring-[var(--accent)]"
              />
              {errors.name && (
                <p className="mt-1 text-sm text-rose-700">{errors.name.message}</p>
              )}
            </div>

            {/* Drive Type */}
            <div>
              <label htmlFor="type" className="block text-sm font-semibold text-[var(--text)] mb-1">
                Drive Type *
              </label>
              <select
                {...register('type')}
                id="type"
                className="w-full rounded-lg border border-[var(--border)] bg-white px-3 py-2 text-sm text-[var(--text)] focus:outline-none focus:ring-2 focus:ring-[var(--accent)]"
              >
                <option value="">Select type...</option>
                <option value="LOCAL">Local Filesystem</option>
                <option value="SMB">SMB/CIFS Network Share</option>
                <option value="SFTP">SFTP (SSH)</option>
                <option value="FTP">FTP</option>
              </select>
              {errors.type && (
                <p className="mt-1 text-sm text-rose-700">{errors.type.message}</p>
              )}
            </div>

            {/* Connection URL */}
            <div>
              <label htmlFor="connectionUrl" className="block text-sm font-semibold text-[var(--text)] mb-1">
                Connection URL *
              </label>
              <input
                {...register('connectionUrl')}
                type="text"
                id="connectionUrl"
                placeholder={
                  driveType === 'LOCAL' ? '/path/to/directory' :
                  driveType === 'SMB' ? 'smb://server/share' :
                  driveType === 'SFTP' ? 'sftp://server.com' :
                  driveType === 'FTP' ? 'ftp://server.com' :
                  'Connection URL'
                }
                className="w-full rounded-lg border border-[var(--border)] bg-white px-3 py-2 text-sm text-[var(--text)] placeholder:text-[var(--muted)] focus:outline-none focus:ring-2 focus:ring-[var(--accent)]"
              />
              {errors.connectionUrl && (
                <p className="mt-1 text-sm text-rose-700">{errors.connectionUrl.message}</p>
              )}
            </div>

            {/* Root Path */}
            <div>
              <label htmlFor="rootPath" className="block text-sm font-semibold text-[var(--text)] mb-1">
                Root Path
              </label>
              <input
                {...register('rootPath')}
                type="text"
                id="rootPath"
                placeholder="/"
                className="w-full rounded-lg border border-[var(--border)] bg-white px-3 py-2 text-sm text-[var(--text)] placeholder:text-[var(--muted)] focus:outline-none focus:ring-2 focus:ring-[var(--accent)]"
              />
              {errors.rootPath && (
                <p className="mt-1 text-sm text-rose-700">{errors.rootPath.message}</p>
              )}
            </div>

            {/* Credentials (for non-LOCAL drives) */}
            {driveType && driveType !== 'LOCAL' && (
              <div>
                <label htmlFor="credentials" className="block text-sm font-semibold text-[var(--text)] mb-1">
                  Credentials (JSON)
                </label>
                <textarea
                  {...register('credentials')}
                  id="credentials"
                  rows={3}
                  placeholder='{"username": "user", "password": "pass"}'
                  className="w-full rounded-lg border border-[var(--border)] bg-white px-3 py-2 text-sm text-[var(--text)] placeholder:text-[var(--muted)] focus:outline-none focus:ring-2 focus:ring-[var(--accent)] font-mono"
                />
                {errors.credentials && (
                  <p className="mt-1 text-sm text-rose-700">{errors.credentials.message}</p>
                )}
                <p className="mt-1 text-xs text-[var(--muted)]">
                  Optional: JSON object with authentication credentials
                </p>
              </div>
            )}

            {/* Options */}
            <div className="space-y-2">
              <label className="flex items-center rounded-lg border border-transparent px-2 py-1 text-sm text-[var(--text)] transition hover:border-[var(--border)] hover:bg-[var(--surface-muted)]">
                <input
                  {...register('autoConnect')}
                  type="checkbox"
                  className="h-4 w-4 rounded border-[var(--border)] text-[var(--accent)] focus:ring-[var(--accent)]"
                />
                <span className="ml-2 text-sm text-[var(--text)]">
                  Auto-connect on startup
                </span>
              </label>

              <label className="flex items-center rounded-lg border border-transparent px-2 py-1 text-sm text-[var(--text)] transition hover:border-[var(--border)] hover:bg-[var(--surface-muted)]">
                <input
                  {...register('autoCrawl')}
                  type="checkbox"
                  className="h-4 w-4 rounded border-[var(--border)] text-[var(--accent)] focus:ring-[var(--accent)]"
                />
                <span className="ml-2 text-sm text-[var(--text)]">
                  Auto-crawl after connection
                </span>
              </label>
            </div>

            {/* Actions */}
            <div className="flex justify-end gap-3 pt-4 border-t">
              <button
                type="button"
                onClick={onClose}
                className="rounded-lg bg-[var(--surface-muted)] px-4 py-2 text-sm font-semibold text-[var(--text)] transition hover:bg-[var(--surface-strong)]"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={createMutation.isPending}
                className="rounded-lg bg-[var(--accent)] px-4 py-2 text-sm font-semibold text-white transition hover:bg-[var(--accent-strong)] disabled:opacity-60"
              >
                {createMutation.isPending ? 'Creating...' : 'Create Drive'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
