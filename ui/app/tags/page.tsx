/**
 * App: Picture Model
 * Package: ui/app/tags
 * File: page.tsx
 * Version: 0.1.0
 * Turns: 4
 * Author: Claude
 * Date: 2026-01-29
 * Exports: TagsPage
 * Description: Tags view with tag filtering and image grid across all drives
 */
'use client';

import { useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useQuery } from '@tanstack/react-query';
import { Header } from '@/components/header';
import { ImageGrid } from '@/components/image-grid';
import { tagApi, imageApi, driveApi } from '@/lib/api-client';
import type { Image } from '@/types';
import { ChevronLeft, Plus, Settings as SettingsIcon, ArrowUpDown } from 'lucide-react';

type SortOption = 'recent' | 'name' | 'date';
type TagSortOption = 'name' | 'usage' | 'recent';

export default function TagsPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const driveIdParam = searchParams.get('driveId');

  const [selectedTagIds, setSelectedTagIds] = useState<Set<string>>(new Set());
  const [selectedDriveIds, setSelectedDriveIds] = useState<Set<string>>(
    driveIdParam ? new Set([driveIdParam]) : new Set()
  );
  const [selectedImageIds, setSelectedImageIds] = useState<Set<string>>(new Set());
  const [sortBy, setSortBy] = useState<SortOption>('recent');
  const [tagSortBy, setTagSortBy] = useState<TagSortOption>('name');
  const [page, setPage] = useState(0);

  // Fetch all tags
  const { data: tags = [] } = useQuery({
    queryKey: ['tags'],
    queryFn: async () => {
      const response = await tagApi.getAll();
      return response.data;
    },
  });

  // Fetch all drives
  const { data: drives = [] } = useQuery({
    queryKey: ['drives'],
    queryFn: async () => {
      const response = await driveApi.getAll();
      return response.data;
    },
  });

  // Fetch images filtered by tags and drives
  const { data: imagesResponse, isLoading: imagesLoading } = useQuery({
    queryKey: ['images', 'tags', Array.from(selectedTagIds), Array.from(selectedDriveIds), sortBy, page],
    queryFn: async () => {
      const response = await imageApi.search({
        tagIds: Array.from(selectedTagIds),
        driveId: selectedDriveIds.size === 1 ? Array.from(selectedDriveIds)[0] : undefined,
        sort: sortBy,
        page,
        size: 24,
      });
      return response.data;
    },
    enabled: selectedTagIds.size > 0,
  });

  const images = imagesResponse?.content || [];
  const totalImages = imagesResponse?.totalElements || 0;

  // Sort tags
  const sortedTags = [...tags].sort((a, b) => {
    switch (tagSortBy) {
      case 'usage':
        return b.usageCount - a.usageCount;
      case 'recent':
        return new Date(b.createdDate).getTime() - new Date(a.createdDate).getTime();
      case 'name':
      default:
        return a.name.localeCompare(b.name);
    }
  });

  const handleTagToggle = (tagId: string) => {
    setSelectedTagIds((prev) => {
      const next = new Set(prev);
      if (next.has(tagId)) {
        next.delete(tagId);
      } else {
        next.add(tagId);
      }
      return next;
    });
    setPage(0);
  };

  const handleDriveToggle = (driveId: string) => {
    setSelectedDriveIds((prev) => {
      const next = new Set(prev);
      if (next.has(driveId)) {
        next.delete(driveId);
      } else {
        next.add(driveId);
      }
      return next;
    });
    setPage(0);
  };

  const handleImageSelect = (imageId: string) => {
    setSelectedImageIds((prev) => {
      const next = new Set(prev);
      if (next.has(imageId)) {
        next.delete(imageId);
      } else {
        next.add(imageId);
      }
      return next;
    });
  };

  const handleImageClick = (imageId: string) => {
    const image = images.find((img) => img.id === imageId);
    if (image) {
      router.push(`/image/${image.driveId}/${imageId}`);
    }
  };

  const handleSelectAll = () => {
    if (selectedImageIds.size === images.length) {
      setSelectedImageIds(new Set());
    } else {
      setSelectedImageIds(new Set(images.map((img) => img.id)));
    }
  };

  return (
    <div className="min-h-screen">
      <Header />

      <main className="mx-auto max-w-[1800px] px-4 py-6 sm:px-6 lg:px-8">
        {/* Breadcrumb */}
        <div className="mb-6 flex items-center gap-3">
          <button
            onClick={() => router.push('/')}
            className="flex items-center gap-2 text-sm font-semibold text-[var(--muted)] hover:text-[var(--text)]"
          >
            <ChevronLeft className="h-4 w-4" />
            <span>Dashboard</span>
          </button>
          <span className="text-[var(--muted)]">/</span>
          <span className="text-sm font-semibold text-[var(--text)]">Tags</span>
        </div>

        <div className="grid grid-cols-1 gap-6 lg:grid-cols-[320px_1fr]">
          {/* Left Sidebar - Tags and Filters */}
          <div className="space-y-6">
            {/* Tags List */}
            <div className="rounded-xl border border-[var(--border)] bg-white/70 p-4">
              <div className="mb-4 flex items-center justify-between">
                <h2 className="text-sm font-semibold uppercase tracking-wide text-[var(--muted)]">
                  All Tags ({tags.length})
                </h2>
                <button
                  className="rounded-lg p-1.5 text-[var(--accent)] hover:bg-[var(--accent-soft)]"
                  title="Manage Tags"
                >
                  <SettingsIcon className="h-4 w-4" />
                </button>
              </div>

              {/* Tag Sort */}
              <div className="mb-3">
                <select
                  value={tagSortBy}
                  onChange={(e) => setTagSortBy(e.target.value as TagSortOption)}
                  className="w-full rounded-lg border border-[var(--border)] bg-white px-3 py-1.5 text-sm font-medium text-[var(--text)] focus:border-[var(--accent)] focus:outline-none"
                >
                  <option value="name">Name</option>
                  <option value="usage">Most Used</option>
                  <option value="recent">Recent</option>
                </select>
              </div>

              {/* Tags */}
              <div className="max-h-[400px] space-y-2 overflow-y-auto">
                {sortedTags.map((tag) => (
                  <label
                    key={tag.id}
                    className={`flex cursor-pointer items-center gap-2 rounded-lg px-3 py-2 text-sm transition ${
                      selectedTagIds.has(tag.id)
                        ? 'bg-[var(--accent-soft)] text-[var(--accent-ink)]'
                        : 'hover:bg-[var(--surface-muted)]'
                    }`}
                  >
                    <input
                      type="checkbox"
                      checked={selectedTagIds.has(tag.id)}
                      onChange={() => handleTagToggle(tag.id)}
                      className="rounded border-[var(--border)] text-[var(--accent)] focus:ring-[var(--accent)]"
                    />
                    <span className="flex-1 font-medium">{tag.name}</span>
                    <span className="text-xs opacity-70">({tag.usageCount})</span>
                  </label>
                ))}
              </div>

              <button className="mt-4 flex w-full items-center justify-center gap-2 rounded-lg border border-dashed border-[var(--border)] py-2 text-sm font-semibold text-[var(--accent)] hover:bg-[var(--accent-soft)]/30">
                <Plus className="h-4 w-4" />
                <span>Create Tag</span>
              </button>
            </div>

            {/* Drive Filter */}
            {drives.length > 0 && (
              <div className="rounded-xl border border-[var(--border)] bg-white/70 p-4">
                <h3 className="mb-3 text-sm font-semibold uppercase tracking-wide text-[var(--muted)]">
                  Filter by Drive
                </h3>
                <div className="space-y-2">
                  <label className="flex items-center gap-2 text-sm">
                    <input
                      type="checkbox"
                      checked={selectedDriveIds.size === 0}
                      onChange={() => setSelectedDriveIds(new Set())}
                      className="rounded border-[var(--border)] text-[var(--accent)] focus:ring-[var(--accent)]"
                    />
                    <span className="font-medium">All Drives</span>
                  </label>
                  {drives.map((drive) => (
                    <label key={drive.id} className="flex items-center gap-2 text-sm">
                      <input
                        type="checkbox"
                        checked={selectedDriveIds.has(drive.id)}
                        onChange={() => handleDriveToggle(drive.id)}
                        className="rounded border-[var(--border)] text-[var(--accent)] focus:ring-[var(--accent)]"
                      />
                      <span>{drive.name}</span>
                    </label>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Right Main Area - Image Grid */}
          <div className="space-y-4">
            {/* Selected Tags Display */}
            {selectedTagIds.size > 0 && (
              <div className="rounded-xl border border-[var(--border)] bg-white/70 p-4">
                <div className="flex flex-wrap items-center gap-3">
                  <span className="text-sm font-semibold text-[var(--muted)]">
                    Selected Tags:
                  </span>
                  {Array.from(selectedTagIds).map((tagId) => {
                    const tag = tags.find((t) => t.id === tagId);
                    return tag ? (
                      <span
                        key={tagId}
                        className="rounded-full border border-[var(--accent)] bg-[var(--accent-soft)] px-3 py-1 text-sm font-medium text-[var(--accent-ink)]"
                      >
                        {tag.name} ({totalImages})
                      </span>
                    ) : null;
                  })}
                </div>
              </div>
            )}

            {/* Controls */}
            <div className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-[var(--border)] bg-white/70 p-4">
              <div className="flex items-center gap-3">
                {selectedTagIds.size > 0 ? (
                  <span className="text-sm text-[var(--muted)]">
                    Showing {images.length} of {totalImages} images
                  </span>
                ) : (
                  <span className="text-sm text-[var(--muted)]">Select tags to view images</span>
                )}
              </div>

              {selectedTagIds.size > 0 && (
                <div className="flex items-center gap-3">
                  {/* Sort Dropdown */}
                  <div className="flex items-center gap-2">
                    <ArrowUpDown className="h-4 w-4 text-[var(--muted)]" />
                    <select
                      value={sortBy}
                      onChange={(e) => setSortBy(e.target.value as SortOption)}
                      className="rounded-lg border border-[var(--border)] bg-white px-3 py-1.5 text-sm font-medium text-[var(--text)] focus:border-[var(--accent)] focus:outline-none"
                    >
                      <option value="recent">Recent</option>
                      <option value="name">Name</option>
                      <option value="date">Date</option>
                    </select>
                  </div>

                  {/* Batch Actions */}
                  {selectedImageIds.size > 0 && (
                    <>
                      <button
                        onClick={handleSelectAll}
                        className="rounded-lg bg-[var(--surface-muted)] px-3 py-1.5 text-sm font-semibold text-[var(--text)] transition hover:bg-[var(--surface-strong)]"
                      >
                        {selectedImageIds.size === images.length ? 'Deselect All' : 'Select All'}
                      </button>
                      <button className="rounded-lg bg-[var(--accent)] px-3 py-1.5 text-sm font-semibold text-white transition hover:bg-[var(--accent-strong)]">
                        Batch Tag ({selectedImageIds.size})
                      </button>
                    </>
                  )}
                </div>
              )}
            </div>

            {/* Image Grid */}
            <ImageGrid
              images={images}
              loading={imagesLoading}
              selectedImageIds={selectedImageIds}
              onImageSelect={handleImageSelect}
              onImageClick={handleImageClick}
              size="medium"
              emptyMessage={
                selectedTagIds.size === 0
                  ? 'Select one or more tags to view images'
                  : 'No images found with the selected tags'
              }
            />
          </div>
        </div>
      </main>
    </div>
  );
}
