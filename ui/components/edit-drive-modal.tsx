/**
 * App: Picture Model
 * Package: ui/components
 * File: edit-drive-modal.tsx
 * Version: 0.1.5
 * Turns: 5,25
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-02-02T06:01:05Z
 * Exports: EditDriveModal
 * Description: Modal form for editing an existing remote drive.
 * EditDriveModal - updates a drive via API and refreshes the drives list.
 */
'use client';

import { useEffect, useState } from 'react';
import { createPortal } from 'react-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { driveApi } from '@/lib/api-client';
import { X } from 'lucide-react';
import type { CreateDriveRequest, DriveType, RemoteFileDrive } from '@/types';

const driveFormSchema = z
  .object({
    name: z.string().min(1, 'Drive name is required').max(200, 'Name too long'),
    type: z.enum(['LOCAL', 'SMB', 'SFTP', 'FTP']),
    connectionUrl: z.string().max(500, 'URL too long').optional(),
    rootPath: z.string().max(500, 'Path too long').default('/'),
    autoConnect: z.boolean().default(false),
    autoCrawl: z.boolean().default(false),
    credentials: z.string().optional(),
  })
  .superRefine((data, ctx) => {
    if (data.type !== 'LOCAL' && (!data.connectionUrl || data.connectionUrl.trim().length === 0)) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['connectionUrl'],
        message: 'Connection URL is required',
      });
    }
  });

type DriveForm = z.infer<typeof driveFormSchema>;

interface EditDriveModalProps {
  isOpen: boolean;
  onClose: () => void;
  drive: RemoteFileDrive;
}

export function EditDriveModal({ isOpen, onClose, drive }: EditDriveModalProps) {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [isMounted, setIsMounted] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    watch,
  } = useForm<DriveForm>({
    resolver: zodResolver(driveFormSchema),
    defaultValues: {
      name: drive.name,
      type: drive.type,
      connectionUrl: drive.connectionUrl,
      rootPath: drive.rootPath,
      autoConnect: drive.autoConnect,
      autoCrawl: drive.autoCrawl,
      credentials: '',
    },
  });

  useEffect(() => {
    setIsMounted(true);
  }, []);

  useEffect(() => {
    if (isOpen) {
      reset({
        name: drive.name,
        type: drive.type,
        connectionUrl: drive.connectionUrl,
        rootPath: drive.rootPath,
        autoConnect: drive.autoConnect,
        autoCrawl: drive.autoCrawl,
        credentials: '',
      });
      setError(null);
    }
  }, [drive, isOpen, reset]);

  const driveType = watch('type');

  const updateMutation = useMutation({
    mutationFn: async (payload: Partial<CreateDriveRequest>) => {
      const response = await driveApi.update(drive.id, payload);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['drives'] });
      onClose();
      setError(null);
    },
    onError: (err: any) => {
      setError(err.response?.data?.message || 'Failed to update drive');
    },
  });

  const onSubmit = (data: DriveForm) => {
    setError(null);
    let credentials: CreateDriveRequest['credentials'];
    if (data.credentials) {
      try {
        JSON.parse(data.credentials);
        credentials = data.credentials;
      } catch (err) {
        setError('Credentials must be valid JSON');
        return;
      }
    }

    const payload: Partial<CreateDriveRequest> = {
      name: data.name,
      rootPath: data.rootPath,
      autoConnect: data.autoConnect,
      autoCrawl: data.autoCrawl,
    };
    if (data.type !== 'LOCAL') {
      payload.connectionUrl = data.connectionUrl;
    }
    if (credentials) {
      payload.credentials = credentials;
    }

    updateMutation.mutate(payload);
  };

  if (!isOpen || !isMounted) return null;

  return createPortal(
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div
        className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm transition-opacity"
        onClick={onClose}
      />

      <div className="flex min-h-full items-center justify-center p-4">
        <div className="relative w-full max-w-4xl rounded-2xl border border-[var(--border)] bg-white/90 p-6 shadow-xl backdrop-blur">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h2 className="text-2xl font-semibold text-[var(--text)]">Edit Drive</h2>
              <p className="text-sm text-[var(--muted)]">
                Update the connection settings for this remote drive.
              </p>
            </div>
            <button
              onClick={onClose}
              className="rounded-full p-2 text-[var(--muted)] transition hover:bg-[var(--surface-muted)] hover:text-[var(--text)]"
            >
              <X className="h-6 w-6" />
            </button>
          </div>

          {error && (
            <div className="mb-4 rounded-lg border border-rose-200 bg-rose-50 p-3">
              <p className="text-sm text-rose-700">{error}</p>
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
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

            <div>
              <label htmlFor="type" className="block text-sm font-semibold text-[var(--text)] mb-1">
                Drive Type *
              </label>
              <select
                {...register('type')}
                id="type"
                disabled
                className="w-full rounded-lg border border-[var(--border)] bg-white px-3 py-2 text-sm text-[var(--text)] focus:outline-none focus:ring-2 focus:ring-[var(--accent)] disabled:opacity-60"
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
              <p className="mt-1 text-xs text-[var(--muted)]">Drive type cannot be changed after creation.</p>
            </div>

            {driveType !== 'LOCAL' && (
              <div>
                <label htmlFor="connectionUrl" className="block text-sm font-semibold text-[var(--text)] mb-1">
                  Connection URL *
                </label>
                <input
                  {...register('connectionUrl')}
                  type="text"
                  id="connectionUrl"
                  placeholder={
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
            )}

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
                disabled={updateMutation.isPending}
                className="rounded-lg bg-[var(--accent)] px-4 py-2 text-sm font-semibold text-white transition hover:bg-[var(--accent-strong)] disabled:opacity-60"
              >
                {updateMutation.isPending ? 'Saving...' : 'Save Changes'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  , document.body);
}
