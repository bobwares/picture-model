/**
 * App: Picture Model
 * Package: ui/components
 * File: metadata-editor.tsx
 * Version: 0.1.0
 * Turns: 4
 * Author: Claude
 * Date: 2026-01-29
 * Exports: MetadataEditor
 * Description: Metadata display and editing component with EXIF data
 */
'use client';

import { useState, useEffect } from 'react';
import { TagPill } from './tag-pill';
import type { Image, ImageMetadata, Tag } from '@/types';
import {
  Calendar,
  Camera,
  FileText,
  Folder,
  HardDrive,
  MapPin,
  Maximize2,
  Save,
} from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';

interface MetadataEditorProps {
  image: Image;
  availableTags?: Tag[];
  onSave?: (metadata: Partial<Image>) => void;
  onAddTag?: (tagId: string) => void;
  onRemoveTag?: (tagId: string) => void;
}

export function MetadataEditor({
  image,
  availableTags = [],
  onSave,
  onAddTag,
  onRemoveTag,
}: MetadataEditorProps) {
  const [customDescription, setCustomDescription] = useState('');
  const [customLocation, setCustomLocation] = useState('');
  const [customNotes, setCustomNotes] = useState('');
  const [isDirty, setIsDirty] = useState(false);

  useEffect(() => {
    // Load custom metadata
    const descriptionMeta = image.metadata?.find((m) => m.key === 'description');
    const locationMeta = image.metadata?.find((m) => m.key === 'location');
    const notesMeta = image.metadata?.find((m) => m.key === 'notes');

    setCustomDescription(descriptionMeta?.value || '');
    setCustomLocation(locationMeta?.value || '');
    setCustomNotes(notesMeta?.value || '');
    setIsDirty(false);
  }, [image]);

  const handleSave = () => {
    const updatedMetadata: ImageMetadata[] = [
      ...(image.metadata || []),
      {
        id: `custom-description-${Date.now()}`,
        key: 'description',
        value: customDescription,
        source: 'USER_ENTERED',
        lastModified: new Date().toISOString(),
      },
      {
        id: `custom-location-${Date.now()}`,
        key: 'location',
        value: customLocation,
        source: 'USER_ENTERED',
        lastModified: new Date().toISOString(),
      },
      {
        id: `custom-notes-${Date.now()}`,
        key: 'notes',
        value: customNotes,
        source: 'USER_ENTERED',
        lastModified: new Date().toISOString(),
      },
    ];

    onSave?.({ metadata: updatedMetadata });
    setIsDirty(false);
  };

  const getMetadataValue = (key: string): string | undefined => {
    return image.metadata?.find((m) => m.key === key)?.value;
  };

  const formatBytes = (bytes: number): string => {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${(bytes / Math.pow(k, i)).toFixed(1)} ${sizes[i]}`;
  };

  return (
    <div className="flex h-full flex-col overflow-y-auto bg-white/70 p-6">
      <h3 className="mb-6 text-lg font-semibold text-[var(--text)]">Metadata</h3>

      <div className="space-y-6">
        {/* Drive Info */}
        <div className="space-y-2">
          <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wide text-[var(--muted)]">
            <HardDrive className="h-3.5 w-3.5" />
            <span>Drive</span>
          </div>
          <p className="text-sm text-[var(--text)]">{image.driveName}</p>
        </div>

        {/* Filename */}
        <div className="space-y-2">
          <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wide text-[var(--muted)]">
            <FileText className="h-3.5 w-3.5" />
            <span>Filename</span>
          </div>
          <p className="break-all text-sm text-[var(--text)]">{image.fileName}</p>
        </div>

        {/* File Size */}
        <div className="space-y-2">
          <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wide text-[var(--muted)]">
            <Maximize2 className="h-3.5 w-3.5" />
            <span>Size</span>
          </div>
          <p className="text-sm text-[var(--text)]">{formatBytes(image.fileSize)}</p>
        </div>

        {/* Dimensions */}
        {image.width && image.height && (
          <div className="space-y-2">
            <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wide text-[var(--muted)]">
              <Maximize2 className="h-3.5 w-3.5" />
              <span>Dimensions</span>
            </div>
            <p className="text-sm text-[var(--text)]">
              {image.width} × {image.height}
            </p>
          </div>
        )}

        {/* Captured Date */}
        {image.capturedAt && (
          <div className="space-y-2">
            <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wide text-[var(--muted)]">
              <Calendar className="h-3.5 w-3.5" />
              <span>Captured</span>
            </div>
            <p className="text-sm text-[var(--text)]">
              {new Date(image.capturedAt).toLocaleString()}
            </p>
            <p className="text-xs text-[var(--muted)]">
              {formatDistanceToNow(new Date(image.capturedAt), { addSuffix: true })}
            </p>
          </div>
        )}

        {/* Camera Info */}
        {getMetadataValue('camera') && (
          <div className="space-y-2">
            <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wide text-[var(--muted)]">
              <Camera className="h-3.5 w-3.5" />
              <span>Camera</span>
            </div>
            <p className="text-sm text-[var(--text)]">{getMetadataValue('camera')}</p>
          </div>
        )}

        {/* Tags */}
        <div className="space-y-2">
          <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wide text-[var(--muted)]">
            <span>Tags</span>
          </div>
          <div className="flex flex-wrap gap-2">
            {image.tags && image.tags.length > 0 ? (
              image.tags.map((tag) => (
                <TagPill
                  key={tag.id}
                  tag={tag}
                  removable={!!onRemoveTag}
                  onRemove={onRemoveTag}
                />
              ))
            ) : (
              <p className="text-sm text-[var(--muted)]">No tags</p>
            )}
          </div>
          {onAddTag && availableTags.length > 0 && (
            <button className="mt-2 text-sm font-semibold text-[var(--accent)] hover:underline">
              + Add Tag
            </button>
          )}
        </div>

        {/* Divider */}
        <div className="border-t border-[var(--border)]" />

        {/* Custom Metadata (Editable) */}
        <div className="space-y-4">
          <h4 className="text-xs font-semibold uppercase tracking-wide text-[var(--muted)]">
            Custom Data
          </h4>

          {/* Description */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-[var(--text)]">Description</label>
            <textarea
              value={customDescription}
              onChange={(e) => {
                setCustomDescription(e.target.value);
                setIsDirty(true);
              }}
              className="w-full rounded-lg border border-[var(--border)] bg-white px-3 py-2 text-sm text-[var(--text)] focus:border-[var(--accent)] focus:outline-none"
              rows={3}
              placeholder="Add a description..."
            />
          </div>

          {/* Location */}
          <div className="space-y-2">
            <label className="flex items-center gap-2 text-sm font-medium text-[var(--text)]">
              <MapPin className="h-3.5 w-3.5" />
              <span>Location</span>
            </label>
            <input
              type="text"
              value={customLocation}
              onChange={(e) => {
                setCustomLocation(e.target.value);
                setIsDirty(true);
              }}
              className="w-full rounded-lg border border-[var(--border)] bg-white px-3 py-2 text-sm text-[var(--text)] focus:border-[var(--accent)] focus:outline-none"
              placeholder="Where was this taken?"
            />
          </div>

          {/* Notes */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-[var(--text)]">Notes</label>
            <textarea
              value={customNotes}
              onChange={(e) => {
                setCustomNotes(e.target.value);
                setIsDirty(true);
              }}
              className="w-full rounded-lg border border-[var(--border)] bg-white px-3 py-2 text-sm text-[var(--text)] focus:border-[var(--accent)] focus:outline-none"
              rows={3}
              placeholder="Add notes..."
            />
          </div>

          {/* Save Button */}
          {onSave && (
            <button
              onClick={handleSave}
              disabled={!isDirty}
              className="flex w-full items-center justify-center gap-2 rounded-lg bg-[var(--accent)] px-4 py-2 text-sm font-semibold text-white transition hover:bg-[var(--accent-strong)] disabled:opacity-50"
            >
              <Save className="h-4 w-4" />
              <span>Save Changes</span>
            </button>
          )}
        </div>

        {/* Divider */}
        <div className="border-t border-[var(--border)]" />

        {/* File Path */}
        <div className="space-y-2">
          <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wide text-[var(--muted)]">
            <Folder className="h-3.5 w-3.5" />
            <span>Path</span>
          </div>
          <p className="break-all text-xs text-[var(--muted)]">{image.filePath}</p>
          <button
            onClick={() => navigator.clipboard.writeText(image.fullPath)}
            className="text-xs font-semibold text-[var(--accent)] hover:underline"
          >
            Copy Full Path
          </button>
        </div>
      </div>
    </div>
  );
}
