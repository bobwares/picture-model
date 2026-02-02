/**
 * App: Picture Model
 * Package: ui/app/tree/[driveId]
 * File: page.tsx
 * Version: 0.1.10
 * Turns: 4,9,10,11,12,13,6,7,8,14,15
 * Author: Claude
 * Date: 2026-02-02T18:51:30Z
 * Exports: TreeViewPage
 * Description: Directory tree view with sidebar navigation and image grid
 */
'use client';

import { useMemo, useState, useEffect, useRef } from 'react';
import type { CSSProperties } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useQuery } from '@tanstack/react-query';
import { Header } from '@/components/header';
import { DirectoryTree } from '@/components/directory-tree';
import { ImageGrid } from '@/components/image-grid';
import { driveApi } from '@/lib/api-client';
import type { Image } from '@/types';
import {
  ChevronLeft,
  ArrowUpDown,
  PanelLeftClose,
  PanelLeftOpen,
  ArrowDownAZ,
  ArrowUpAZ,
} from 'lucide-react';

type SortOption = 'date' | 'name' | 'size';
type SortDirection = 'asc' | 'desc';
type ThumbnailSize = 'small' | 'medium' | 'large';
const PAGE_SIZE_BY_THUMBNAIL: Record<ThumbnailSize, number> = {
  small: 120,
  medium: 72,
  large: 48,
};

const DEFAULT_SIDEBAR_WIDTH = 240;
const COLLAPSED_SIDEBAR_WIDTH = 72;
const MIN_SIDEBAR_WIDTH = 200;
const MAX_SIDEBAR_WIDTH = 480;

export default function TreeViewPage() {
  const params = useParams();
  const router = useRouter();
  const driveId = params.driveId as string;

  const [selectedPath, setSelectedPath] = useState<string>('');
  const [selectedImageIds, setSelectedImageIds] = useState<Set<string>>(new Set());
  const [sortBy, setSortBy] = useState<SortOption>('date');
  const [sortDirection, setSortDirection] = useState<SortDirection>('desc');
  const [thumbnailSize, setThumbnailSize] = useState<ThumbnailSize>('medium');
  const [page, setPage] = useState(0);
  const [accumulatedImages, setAccumulatedImages] = useState<Image[]>([]);
  const [isTreeCollapsed, setIsTreeCollapsed] = useState(false);
  const [sidebarWidth, setSidebarWidth] = useState(DEFAULT_SIDEBAR_WIDTH);
  const [isResizing, setIsResizing] = useState(false);
  const resizeState = useRef({ startX: 0, startWidth: DEFAULT_SIDEBAR_WIDTH });
  const pageSize = useMemo(() => PAGE_SIZE_BY_THUMBNAIL[thumbnailSize], [thumbnailSize]);

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
    queryKey: ['images', driveId, selectedPath, sortBy, sortDirection, page, pageSize],
    queryFn: async () => {
      const response = await driveApi.getImages(driveId, {
        path: selectedPath,
        sort: `${sortBy},${sortDirection}`,
        page,
        size: pageSize,
      });
      return response.data;
    },
    enabled: !!driveId && selectedPath !== '',
  });

  const images = imagesResponse?.content || [];
  const hasMore = imagesResponse ? !imagesResponse.last : false;
  const visibleImages = useMemo(() => accumulatedImages, [accumulatedImages]);

  // Set initial path when tree loads
  useEffect(() => {
    if (treeData && !selectedPath && treeData.path) {
      setSelectedPath(treeData.path);
    }
  }, [treeData, selectedPath]);

  useEffect(() => {
    if (isTreeCollapsed) {
      setIsResizing(false);
    }
  }, [isTreeCollapsed]);

  useEffect(() => {
    if (!isResizing) {
      return;
    }

    const handleMove = (event: PointerEvent) => {
      const delta = event.clientX - resizeState.current.startX;
      const nextWidth = Math.min(
        MAX_SIDEBAR_WIDTH,
        Math.max(MIN_SIDEBAR_WIDTH, resizeState.current.startWidth + delta),
      );
      setSidebarWidth(nextWidth);
    };

    const handleUp = () => {
      setIsResizing(false);
    };

    window.addEventListener('pointermove', handleMove);
    window.addEventListener('pointerup', handleUp);

    return () => {
      window.removeEventListener('pointermove', handleMove);
      window.removeEventListener('pointerup', handleUp);
    };
  }, [isResizing]);

  useEffect(() => {
    setPage(0);
    setAccumulatedImages([]);
    setSelectedImageIds(new Set());
  }, [selectedPath, sortBy, sortDirection, pageSize]);

  const handlePathSelect = (path: string) => {
    setSelectedPath(path);
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
    if (typeof window !== 'undefined') {
      const ids = images.map((img) => img.id);
      window.sessionStorage.setItem(`image-nav:${driveId}`, JSON.stringify({ ids }));
    }
    router.push(`/image/${driveId}/${imageId}`);
  };

  const handleSelectAll = () => {
    if (selectedImageIds.size === visibleImages.length) {
      setSelectedImageIds(new Set());
    } else {
      setSelectedImageIds(new Set(visibleImages.map((img) => img.id)));
    }
  };

  const handleLoadMore = () => {
    if (!hasMore) {
      return;
    }
    setPage((prev) => prev + 1);
  };

  useEffect(() => {
    if (imagesResponse?.content) {
      setAccumulatedImages((prev) => {
        if (page === 0) {
          return imagesResponse.content;
        }
        const existing = new Set(prev.map((img) => img.id));
        const next = [...prev];
        for (const img of imagesResponse.content) {
          if (!existing.has(img.id)) {
            next.push(img);
          }
        }
        return next;
      });
    }
  }, [imagesResponse?.content, page]);

  return (
    <div className="min-h-screen">
      <Header />

      <main className="mx-auto max-w-[1800px] px-4 py-6 sm:px-6 lg:px-8">
        {/* Breadcrumb */}
        <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
          <div className="flex items-center gap-3">
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

          <div className="flex items-center gap-2">
            <ArrowUpDown className="h-4 w-4 text-[var(--muted)]" />
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value as SortOption)}
              className="rounded-lg border border-[var(--border)] bg-white px-3 py-1.5 text-sm font-medium text-[var(--text)] focus:border-[var(--accent)] focus:outline-none"
              aria-label="Sort images"
            >
              <option value="date">Date</option>
              <option value="name">Name</option>
              <option value="size">Size</option>
            </select>
            <button
              type="button"
              onClick={() =>
                setSortDirection((prev) => (prev === 'asc' ? 'desc' : 'asc'))
              }
              className="rounded-lg border border-[var(--border)] bg-white p-2 text-[var(--text)] transition hover:bg-[var(--surface-muted)]"
              aria-label={`Sort ${sortDirection === 'asc' ? 'descending' : 'ascending'}`}
              title={`Sort ${sortDirection === 'asc' ? 'descending' : 'ascending'}`}
            >
              {sortDirection === 'asc' ? (
                <ArrowUpAZ className="h-4 w-4" />
              ) : (
                <ArrowDownAZ className="h-4 w-4" />
              )}
            </button>
          </div>
        </div>

        <div
          className="grid grid-cols-1 gap-6 lg:grid-cols-[var(--tree-sidebar-width)_1fr]"
          style={{
            ['--tree-sidebar-width' as string]: isTreeCollapsed
              ? `${COLLAPSED_SIDEBAR_WIDTH}px`
              : `${sidebarWidth}px`,
          } as CSSProperties}
        >
          {/* Left Sidebar - Directory Tree */}
          <div className="relative lg:sticky lg:top-20 lg:h-[calc(100vh-120px)]">
            <div className="mb-3 flex items-center justify-between">
              {!isTreeCollapsed && (
                <h2 className="text-sm font-semibold uppercase tracking-wide text-[var(--muted)]">
                  Directory Tree
                </h2>
              )}
              <button
                onClick={() => setIsTreeCollapsed((prev) => !prev)}
                className="rounded-lg border border-[var(--border)] bg-white/70 p-2 text-[var(--text)] transition hover:bg-[var(--surface-muted)]"
                aria-label={isTreeCollapsed ? 'Expand directory tree' : 'Collapse directory tree'}
              >
                {isTreeCollapsed ? (
                  <PanelLeftOpen className="h-4 w-4" />
                ) : (
                  <PanelLeftClose className="h-4 w-4" />
                )}
              </button>
            </div>
            {!isTreeCollapsed && (
              <DirectoryTree
                rootNode={treeData}
                selectedPath={selectedPath}
                onPathSelect={handlePathSelect}
                loading={treeLoading}
              />
            )}
            {!isTreeCollapsed && (
              <div
                className="absolute right-0 top-0 h-full w-2 cursor-col-resize"
                onPointerDown={(event) => {
                  setIsResizing(true);
                  resizeState.current = { startX: event.clientX, startWidth: sidebarWidth };
                  event.currentTarget.setPointerCapture(event.pointerId);
                }}
                aria-hidden="true"
              />
            )}
          </div>

          {/* Right Main Area - Image Grid */}
          <div className="space-y-4 lg:h-[calc(100vh-120px)] lg:overflow-y-auto lg:pr-2">
            {/* Controls */}
            <div className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-[var(--border)] bg-white/70 p-4">
              <div className="flex items-center gap-3">
                <span className="text-sm text-[var(--muted)]">
                  {selectedPath ? `Path: ${selectedPath}` : 'Select a folder'}
                </span>
                {visibleImages.length > 0 && (
                  <span className="rounded-full bg-[var(--accent-soft)] px-2.5 py-0.5 text-xs font-semibold text-[var(--accent-ink)]">
                    {visibleImages.length} images
                  </span>
                )}
              </div>

              <div className="flex items-center gap-3">
                <div className="flex items-center gap-2">
                  <span className="text-xs font-semibold uppercase tracking-wide text-[var(--muted)]">
                    Thumbnails
                  </span>
                  <select
                    value={thumbnailSize}
                    onChange={(e) => setThumbnailSize(e.target.value as ThumbnailSize)}
                    className="rounded-lg border border-[var(--border)] bg-white px-3 py-1.5 text-sm font-medium text-[var(--text)] focus:border-[var(--accent)] focus:outline-none"
                  >
                    <option value="small">Small</option>
                    <option value="medium">Medium</option>
                    <option value="large">Large</option>
                  </select>
                </div>

                {/* Batch Actions */}
                {selectedImageIds.size > 0 && (
                  <>
                    <button
                      onClick={handleSelectAll}
                      className="rounded-lg bg-[var(--surface-muted)] px-3 py-1.5 text-sm font-semibold text-[var(--text)] transition hover:bg-[var(--surface-strong)]"
                    >
                  {selectedImageIds.size === visibleImages.length ? 'Deselect All' : 'Select All'}
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
              images={visibleImages}
              loading={imagesLoading}
              selectedImageIds={selectedImageIds}
              onImageSelect={handleImageSelect}
              onImageClick={handleImageClick}
              resetKey={selectedPath}
              onLoadMore={handleLoadMore}
              hasMore={hasMore}
              size={thumbnailSize}
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
