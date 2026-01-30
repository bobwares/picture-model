/**
 * App: Picture Model
 * Package: ui/app/tree/[driveId]
 * File: page.tsx
 * Version: 0.1.0
 * Turns: 4
 * Author: Claude
 * Date: 2026-01-29
 * Exports: TreeViewPage
 * Description: Directory tree view with sidebar navigation and image grid
 */
'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useQuery } from '@tanstack/react-query';
import { Header } from '@/components/header';
import { DirectoryTree } from '@/components/directory-tree';
import { ImageGrid } from '@/components/image-grid';
import { driveApi } from '@/lib/api-client';
import type { Image } from '@/types';
import { ChevronLeft, ArrowUpDown } from 'lucide-react';

type SortOption = 'date' | 'name' | 'size';

export default function TreeViewPage() {
  const params = useParams();
  const router = useRouter();
  const driveId = params.driveId as string;

  const [selectedPath, setSelectedPath] = useState<string>('');
  const [selectedImageIds, setSelectedImageIds] = useState<Set<string>>(new Set());
  const [sortBy, setSortBy] = useState<SortOption>('date');
  const [page, setPage] = useState(0);

  // Fetch drive details
  const { data: drive } = useQuery({
    queryKey: ['drive', driveId],
    queryFn: async () => {
      const response = await driveApi.getById(driveId);
      return response.data;
    },
  });

  // Fetch directory tree
  const { data: treeData, isLoading: treeLoading } = useQuery({
    queryKey: ['tree', driveId],
    queryFn: async () => {
      const response = await driveApi.getTree(driveId);
      return response.data;
    },
    enabled: !!driveId,
  });

  // Fetch images for selected path
  const { data: imagesResponse, isLoading: imagesLoading } = useQuery({
    queryKey: ['images', driveId, selectedPath, sortBy, page],
    queryFn: async () => {
      const response = await driveApi.getImages(driveId, {
        path: selectedPath,
        sort: sortBy,
        page,
        size: 24,
      });
      return response.data;
    },
    enabled: !!driveId && selectedPath !== '',
  });

  const images = imagesResponse?.content || [];
  const hasMore = imagesResponse ? !imagesResponse.last : false;

  // Set initial path when tree loads
  useEffect(() => {
    if (treeData && !selectedPath && treeData.path) {
      setSelectedPath(treeData.path);
    }
  }, [treeData, selectedPath]);

  const handlePathSelect = (path: string) => {
    setSelectedPath(path);
    setPage(0);
    setSelectedImageIds(new Set());
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
    router.push(`/image/${driveId}/${imageId}`);
  };

  const handleLoadMore = () => {
    setPage((prev) => prev + 1);
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
          <span className="text-sm font-semibold text-[var(--text)]">
            {drive?.name || 'Loading...'}
          </span>
          {selectedPath && (
            <>
              <span className="text-[var(--muted)]">/</span>
              <span className="text-sm text-[var(--muted)]">{selectedPath}</span>
            </>
          )}
        </div>

        <div className="grid grid-cols-1 gap-6 lg:grid-cols-[320px_1fr]">
          {/* Left Sidebar - Directory Tree */}
          <div className="lg:sticky lg:top-20 lg:h-[calc(100vh-120px)]">
            <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-[var(--muted)]">
              Directory Tree
            </h2>
            {treeData && (
              <DirectoryTree
                rootNode={treeData}
                selectedPath={selectedPath}
                onPathSelect={handlePathSelect}
                loading={treeLoading}
              />
            )}
          </div>

          {/* Right Main Area - Image Grid */}
          <div className="space-y-4">
            {/* Controls */}
            <div className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-[var(--border)] bg-white/70 p-4">
              <div className="flex items-center gap-3">
                <span className="text-sm text-[var(--muted)]">
                  {selectedPath ? `Path: ${selectedPath}` : 'Select a folder'}
                </span>
                {images.length > 0 && (
                  <span className="rounded-full bg-[var(--accent-soft)] px-2.5 py-0.5 text-xs font-semibold text-[var(--accent-ink)]">
                    {images.length} images
                  </span>
                )}
              </div>

              <div className="flex items-center gap-3">
                {/* Sort Dropdown */}
                <div className="flex items-center gap-2">
                  <ArrowUpDown className="h-4 w-4 text-[var(--muted)]" />
                  <select
                    value={sortBy}
                    onChange={(e) => setSortBy(e.target.value as SortOption)}
                    className="rounded-lg border border-[var(--border)] bg-white px-3 py-1.5 text-sm font-medium text-[var(--text)] focus:border-[var(--accent)] focus:outline-none"
                  >
                    <option value="date">Date</option>
                    <option value="name">Name</option>
                    <option value="size">Size</option>
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
            </div>

            {/* Image Grid */}
            <ImageGrid
              images={images}
              loading={imagesLoading}
              selectedImageIds={selectedImageIds}
              onImageSelect={handleImageSelect}
              onImageClick={handleImageClick}
              onLoadMore={handleLoadMore}
              hasMore={hasMore}
              size="medium"
              emptyMessage={
                selectedPath
                  ? 'No images in this directory'
                  : 'Select a folder to view images'
              }
            />
          </div>
        </div>
      </main>
    </div>
  );
}
