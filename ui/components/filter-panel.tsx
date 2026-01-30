/**
 * App: Picture Model
 * Package: ui/components
 * File: filter-panel.tsx
 * Version: 0.1.0
 * Turns: 4
 * Author: Claude
 * Date: 2026-01-29
 * Exports: FilterPanel
 * Description: Advanced filter panel for search and filtering
 */
'use client';

import { useState } from 'react';
import { ChevronDown, ChevronUp, X } from 'lucide-react';
import type { RemoteFileDrive, Tag } from '@/types';

interface FilterPanelProps {
  drives?: RemoteFileDrive[];
  tags?: Tag[];
  selectedDriveIds?: string[];
  selectedTagIds?: string[];
  dateFrom?: string;
  dateTo?: string;
  fileTypes?: string[];
  selectedFileTypes?: string[];
  onDriveChange?: (driveIds: string[]) => void;
  onTagChange?: (tagIds: string[]) => void;
  onDateChange?: (from: string, to: string) => void;
  onFileTypeChange?: (fileTypes: string[]) => void;
  onClearFilters?: () => void;
}

export function FilterPanel({
  drives = [],
  tags = [],
  selectedDriveIds = [],
  selectedTagIds = [],
  dateFrom,
  dateTo,
  fileTypes = ['JPEG', 'PNG', 'RAW', 'GIF', 'WEBP'],
  selectedFileTypes = [],
  onDriveChange,
  onTagChange,
  onDateChange,
  onFileTypeChange,
  onClearFilters,
}: FilterPanelProps) {
  const [expandedSections, setExpandedSections] = useState({
    drives: true,
    date: true,
    fileType: true,
    tags: true,
  });

  const toggleSection = (section: keyof typeof expandedSections) => {
    setExpandedSections((prev) => ({ ...prev, [section]: !prev[section] }));
  };

  const handleDriveToggle = (driveId: string) => {
    const updated = selectedDriveIds.includes(driveId)
      ? selectedDriveIds.filter((id) => id !== driveId)
      : [...selectedDriveIds, driveId];
    onDriveChange?.(updated);
  };

  const handleTagToggle = (tagId: string) => {
    const updated = selectedTagIds.includes(tagId)
      ? selectedTagIds.filter((id) => id !== tagId)
      : [...selectedTagIds, tagId];
    onTagChange?.(updated);
  };

  const handleFileTypeToggle = (fileType: string) => {
    const updated = selectedFileTypes.includes(fileType)
      ? selectedFileTypes.filter((ft) => ft !== fileType)
      : [...selectedFileTypes, fileType];
    onFileTypeChange?.(updated);
  };

  const hasActiveFilters =
    selectedDriveIds.length > 0 ||
    selectedTagIds.length > 0 ||
    selectedFileTypes.length > 0 ||
    dateFrom ||
    dateTo;

  return (
    <div className="space-y-4 rounded-xl border border-[var(--border)] bg-white/70 p-4">
      <div className="flex items-center justify-between">
        <h3 className="font-semibold text-[var(--text)]">Filters</h3>
        {hasActiveFilters && onClearFilters && (
          <button
            onClick={onClearFilters}
            className="flex items-center gap-1 text-sm font-semibold text-[var(--accent)] hover:underline"
          >
            <X className="h-3.5 w-3.5" />
            <span>Clear All</span>
          </button>
        )}
      </div>

      {/* Drives Filter */}
      {drives.length > 0 && (
        <div className="space-y-2">
          <button
            onClick={() => toggleSection('drives')}
            className="flex w-full items-center justify-between text-sm font-semibold text-[var(--text)]"
          >
            <span>Drives</span>
            {expandedSections.drives ? (
              <ChevronUp className="h-4 w-4" />
            ) : (
              <ChevronDown className="h-4 w-4" />
            )}
          </button>
          {expandedSections.drives && (
            <div className="space-y-2">
              {drives.map((drive) => (
                <label key={drive.id} className="flex items-center gap-2 text-sm">
                  <input
                    type="checkbox"
                    checked={selectedDriveIds.includes(drive.id)}
                    onChange={() => handleDriveToggle(drive.id)}
                    className="rounded border-[var(--border)] text-[var(--accent)] focus:ring-[var(--accent)]"
                  />
                  <span className="text-[var(--text)]">{drive.name}</span>
                </label>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Date Range Filter */}
      <div className="space-y-2">
        <button
          onClick={() => toggleSection('date')}
          className="flex w-full items-center justify-between text-sm font-semibold text-[var(--text)]"
        >
          <span>Date Range</span>
          {expandedSections.date ? (
            <ChevronUp className="h-4 w-4" />
          ) : (
            <ChevronDown className="h-4 w-4" />
          )}
        </button>
        {expandedSections.date && (
          <div className="space-y-2">
            <div>
              <label className="text-xs text-[var(--muted)]">From</label>
              <input
                type="date"
                value={dateFrom || ''}
                onChange={(e) => onDateChange?.(e.target.value, dateTo || '')}
                className="w-full rounded-lg border border-[var(--border)] bg-white px-3 py-2 text-sm focus:border-[var(--accent)] focus:outline-none"
              />
            </div>
            <div>
              <label className="text-xs text-[var(--muted)]">To</label>
              <input
                type="date"
                value={dateTo || ''}
                onChange={(e) => onDateChange?.(dateFrom || '', e.target.value)}
                className="w-full rounded-lg border border-[var(--border)] bg-white px-3 py-2 text-sm focus:border-[var(--accent)] focus:outline-none"
              />
            </div>
          </div>
        )}
      </div>

      {/* File Type Filter */}
      <div className="space-y-2">
        <button
          onClick={() => toggleSection('fileType')}
          className="flex w-full items-center justify-between text-sm font-semibold text-[var(--text)]"
        >
          <span>File Type</span>
          {expandedSections.fileType ? (
            <ChevronUp className="h-4 w-4" />
          ) : (
            <ChevronDown className="h-4 w-4" />
          )}
        </button>
        {expandedSections.fileType && (
          <div className="space-y-2">
            {fileTypes.map((fileType) => (
              <label key={fileType} className="flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  checked={selectedFileTypes.includes(fileType)}
                  onChange={() => handleFileTypeToggle(fileType)}
                  className="rounded border-[var(--border)] text-[var(--accent)] focus:ring-[var(--accent)]"
                />
                <span className="text-[var(--text)]">{fileType}</span>
              </label>
            ))}
          </div>
        )}
      </div>

      {/* Tags Filter */}
      {tags.length > 0 && (
        <div className="space-y-2">
          <button
            onClick={() => toggleSection('tags')}
            className="flex w-full items-center justify-between text-sm font-semibold text-[var(--text)]"
          >
            <span>Tags</span>
            {expandedSections.tags ? (
              <ChevronUp className="h-4 w-4" />
            ) : (
              <ChevronDown className="h-4 w-4" />
            )}
          </button>
          {expandedSections.tags && (
            <div className="max-h-48 space-y-2 overflow-y-auto">
              {tags.map((tag) => (
                <label key={tag.id} className="flex items-center gap-2 text-sm">
                  <input
                    type="checkbox"
                    checked={selectedTagIds.includes(tag.id)}
                    onChange={() => handleTagToggle(tag.id)}
                    className="rounded border-[var(--border)] text-[var(--accent)] focus:ring-[var(--accent)]"
                  />
                  <span className="text-[var(--text)]">
                    {tag.name} ({tag.usageCount})
                  </span>
                </label>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
