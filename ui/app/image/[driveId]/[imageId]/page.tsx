/**
 * App: Picture Model
 * Package: ui/app/image/[driveId]/[imageId]
 * File: page.tsx
 * Version: 0.1.15
 * Turns: 4,8,10,16,17,18,19,20,21,22,23,27,28,29,30,31
 * Author: Claude
 * Date: 2026-02-02T18:59:26Z
 * Exports: ImageDetailPage
 * Description: Image detail page with right sidebar layout (70/30 split)
 */
'use client';

import { useEffect, useMemo, useRef, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import Image from 'next/image';
import { Header } from '@/components/header';
import { MetadataEditor } from '@/components/metadata-editor';
import { driveApi, imageApi, tagApi } from '@/lib/api-client';
import {
  ChevronLeft,
  ChevronRight,
  ZoomIn,
  ZoomOut,
  Maximize2,
  Minimize2,
  PanelRightClose,
  PanelRightOpen,
} from 'lucide-react';

type ZoomLevel = 'fit' | 'full' | number;

export default function ImageDetailPage() {
  const params = useParams();
  const router = useRouter();
  const queryClient = useQueryClient();
  const imageContainerRef = useRef<HTMLDivElement>(null);
  const restoreFullscreenRef = useRef(false);

  const driveId = params.driveId as string;
  const imageId = params.imageId as string;

  const [zoomLevel, setZoomLevel] = useState<ZoomLevel>('fit');
  const [imageError, setImageError] = useState(false);
  const [navIds, setNavIds] = useState<string[]>([]);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(true);

  // Fetch image details
  const { data: image, isLoading } = useQuery({
    queryKey: ['image', imageId],
    queryFn: async () => {
      const response = await imageApi.getById(imageId);
      return response.data;
    },
  });

  // Fetch all tags
  const { data: tags = [] } = useQuery({
    queryKey: ['tags'],
    queryFn: async () => {
      const response = await tagApi.getAll();
      return response.data;
    },
  });

  const { data: fallbackNav } = useQuery({
    queryKey: ['image-nav', driveId],
    queryFn: async () => {
      const response = await driveApi.getImages(driveId, { page: 0, size: 500, sort: 'date' });
      return response.data.content.map((img) => img.id);
    },
    enabled: !navIds.length && !!driveId,
  });

  // Update metadata mutation
  const updateMetadataMutation = useMutation({
    mutationFn: (metadata: Partial<typeof image>) =>
      imageApi.updateMetadata(imageId, metadata as any),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['image', imageId] });
    },
  });

  // Add tag mutation
  const addTagMutation = useMutation({
    mutationFn: (tagId: string) => imageApi.addTags(imageId, [tagId]),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['image', imageId] });
    },
  });

  // Remove tag mutation
  const removeTagMutation = useMutation({
    mutationFn: (tagId: string) => imageApi.removeTag(imageId, tagId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['image', imageId] });
    },
  });

  const handleZoomIn = () => {
    if (zoomLevel === 'fit') {
      setZoomLevel(100);
    } else if (typeof zoomLevel === 'number') {
      setZoomLevel(Math.min(zoomLevel + 25, 200));
    }
  };

  const handleZoomOut = () => {
    if (typeof zoomLevel === 'number') {
      if (zoomLevel <= 100) {
        setZoomLevel('fit');
      } else {
        setZoomLevel(Math.max(zoomLevel - 25, 100));
      }
    }
  };

  const handleZoomFit = () => {
    setZoomLevel('fit');
  };

  const handleToggleFullscreen = async () => {
    if (!document.fullscreenElement && imageContainerRef.current) {
      await imageContainerRef.current.requestFullscreen();
      return;
    }
    if (document.fullscreenElement) {
      await document.exitFullscreen();
    }
  };

  const handleSaveMetadata = (metadata: Partial<typeof image>) => {
    updateMetadataMutation.mutate(metadata);
  };

  const handleAddTag = (tagId: string) => {
    addTagMutation.mutate(tagId);
  };

  const handleRemoveTag = (tagId: string) => {
    removeTagMutation.mutate(tagId);
  };

  useEffect(() => {
    if (typeof window === 'undefined') {
      return;
    }
    const stored = window.sessionStorage.getItem(`image-nav:${driveId}`);
    if (stored) {
      try {
        const parsed = JSON.parse(stored) as { ids: string[] };
        if (parsed.ids && Array.isArray(parsed.ids)) {
          setNavIds(parsed.ids);
        }
      } catch {
        // ignore malformed storage
      }
    }
  }, [driveId]);

  useEffect(() => {
    const handleFullscreenChange = () => {
      setIsFullscreen(!!document.fullscreenElement);
    };
    document.addEventListener('fullscreenchange', handleFullscreenChange);
    return () => document.removeEventListener('fullscreenchange', handleFullscreenChange);
  }, []);

  useEffect(() => {
    if (!restoreFullscreenRef.current) {
      return;
    }
    if (!image || !imageContainerRef.current) {
      return;
    }
    if (document.fullscreenElement) {
      restoreFullscreenRef.current = false;
      return;
    }
    const target = imageContainerRef.current;
    const restore = async () => {
      try {
        await target.requestFullscreen();
      } catch {
        // ignore failure; user gesture may be required
      } finally {
        restoreFullscreenRef.current = false;
      }
    };
    const timer = window.setTimeout(restore, 0);
    return () => window.clearTimeout(timer);
  }, [imageId, image]);

  const effectiveNavIds = useMemo(() => {
    if (navIds.length && navIds.includes(imageId)) {
      return navIds;
    }
    return fallbackNav || navIds;
  }, [fallbackNav, imageId, navIds]);
  const currentIndex = useMemo(
    () => effectiveNavIds.findIndex((id) => id === imageId),
    [effectiveNavIds, imageId]
  );
  const previousId = currentIndex > 0 ? effectiveNavIds[currentIndex - 1] : null;
  const nextId =
    currentIndex >= 0 && currentIndex < effectiveNavIds.length - 1
      ? effectiveNavIds[currentIndex + 1]
      : null;

  const handlePrevious = () => {
    if (previousId) {
      restoreFullscreenRef.current = isFullscreen;
      router.push(`/image/${driveId}/${previousId}`);
    }
  };

  const handleNext = () => {
    if (nextId) {
      restoreFullscreenRef.current = isFullscreen;
      router.push(`/image/${driveId}/${nextId}`);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen">
        <Header />
        <div className="flex h-[calc(100vh-64px)] items-center justify-center">
          <div className="text-center">
            <div className="mx-auto h-12 w-12 animate-spin rounded-full border-4 border-[var(--border)] border-t-[var(--accent)]" />
            <p className="mt-4 text-sm text-[var(--muted)]">Loading image...</p>
          </div>
        </div>
      </div>
    );
  }

  if (!image) {
    return (
      <div className="min-h-screen">
        <Header />
        <div className="flex h-[calc(100vh-64px)] items-center justify-center">
          <div className="text-center">
            <p className="text-lg font-semibold text-[var(--text)]">Image not found</p>
            <button
              onClick={() => router.back()}
              className="mt-4 text-sm font-semibold text-[var(--accent)] hover:underline"
            >
              Go back
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen flex-col">
      <Header />

      <div className="w-full px-4 pt-6 sm:px-6 lg:px-8">
        <div className="flex w-full items-center gap-4">
          <nav className="flex flex-none items-center gap-2 text-sm font-semibold text-[var(--muted)]">
            <button
              onClick={() => router.push('/')}
              className="transition hover:text-[var(--text)]"
            >
              Dashboard
            </button>
            <span className="text-[var(--muted)]">/</span>
            <button
              onClick={() => router.push(`/tree/${driveId}`)}
              className="transition hover:text-[var(--text)]"
            >
              Directory Tree
            </button>
            <span className="text-[var(--muted)]">/</span>
            <span className="text-[var(--text)]">Image View</span>
          </nav>
          <div className="ml-auto min-w-0 flex-1 text-right">
            <span className="block truncate text-sm font-semibold text-[var(--text)]">
              {image?.fileName || 'Image'}
            </span>
          </div>
        </div>
      </div>

      <div className="flex min-h-0 flex-1">
        {/* Left Panel - Image Display (70%) */}
        <div
          className={`flex min-h-0 flex-col bg-[var(--surface-muted)] ${
            isSidebarCollapsed ? 'w-full' : 'w-[70%] border-r border-[var(--border)]'
          }`}
        >
          {/* Top Bar - Breadcrumb and Controls */}
          <div className="border-b border-[var(--border)] bg-white/70 px-6 py-3">
            <div className="flex items-center justify-end">
              {/* Zoom Controls */}
              <div className="flex items-center gap-2">
                <button
                  onClick={handleZoomOut}
                  className="rounded-lg border border-[var(--border)] bg-white p-2 text-[var(--text)] transition hover:bg-[var(--surface-muted)]"
                  disabled={zoomLevel === 'fit'}
                >
                  <ZoomOut className="h-4 w-4" />
                </button>
                <span className="min-w-[60px] text-center text-sm font-medium text-[var(--text)]">
                  {zoomLevel === 'fit' ? 'Fit' : `${zoomLevel}%`}
                </span>
                <button
                  onClick={handleZoomIn}
                  className="rounded-lg border border-[var(--border)] bg-white p-2 text-[var(--text)] transition hover:bg-[var(--surface-muted)]"
                  disabled={typeof zoomLevel === 'number' && zoomLevel >= 200}
                >
                  <ZoomIn className="h-4 w-4" />
                </button>
                <button
                  onClick={handleZoomFit}
                  className="rounded-lg border border-[var(--border)] bg-white p-2 text-[var(--text)] transition hover:bg-[var(--surface-muted)]"
                >
                  <Minimize2 className="h-4 w-4" />
                </button>
                <button
                  onClick={handleToggleFullscreen}
                  className="rounded-lg border border-[var(--border)] bg-white p-2 text-[var(--text)] transition hover:bg-[var(--surface-muted)]"
                >
                  {isFullscreen ? (
                    <Minimize2 className="h-4 w-4" />
                  ) : (
                    <Maximize2 className="h-4 w-4" />
                  )}
                </button>
                <button
                  onClick={() => setIsSidebarCollapsed((prev) => !prev)}
                  className="rounded-lg border border-[var(--border)] bg-white p-2 text-[var(--text)] transition hover:bg-[var(--surface-muted)]"
                  aria-label={
                    isSidebarCollapsed ? 'Expand metadata sidebar' : 'Collapse metadata sidebar'
                  }
                >
                  {isSidebarCollapsed ? (
                    <PanelRightOpen className="h-4 w-4" />
                  ) : (
                    <PanelRightClose className="h-4 w-4" />
                  )}
                </button>
              </div>
            </div>
          </div>

          {/* Image Display Area */}
          <div
            className={`group relative flex-1 ${
              zoomLevel === 'fit' ? 'overflow-hidden' : 'overflow-auto'
            }`}
            ref={imageContainerRef}
          >
            <div className="flex h-full items-center justify-center p-8">
              {!imageError ? (
                <div
                  className={`relative ${
                    zoomLevel === 'fit' ? 'max-h-full max-w-full' : ''
                  }`}
                  style={
                    typeof zoomLevel === 'number'
                      ? { transform: `scale(${zoomLevel / 100})`, transformOrigin: 'center' }
                      : undefined
                  }
                >
                  <Image
                    src={image.imageUrl}
                    alt={image.fileName}
                    width={image.width || 1920}
                    height={image.height || 1080}
                    className="max-h-full max-w-full object-contain"
                    unoptimized
                    onError={() => setImageError(true)}
                    priority
                  />
                </div>
              ) : (
                <div className="text-center">
                  <p className="text-[var(--muted)]">Failed to load image</p>
                </div>
              )}
            </div>

            <>
              <button
                onClick={handlePrevious}
                disabled={!previousId}
                className="absolute left-6 top-1/2 -translate-y-1/2 rounded-full border border-[var(--border)] bg-white/90 p-3 text-[var(--text)] opacity-0 shadow-lg transition hover:bg-white group-hover:opacity-100 disabled:pointer-events-none disabled:opacity-0"
                aria-label="Previous image"
              >
                <ChevronLeft className="h-5 w-5" />
              </button>
              <button
                onClick={handleNext}
                disabled={!nextId}
                className="absolute right-6 top-1/2 -translate-y-1/2 rounded-full border border-[var(--border)] bg-white/90 p-3 text-[var(--text)] opacity-0 shadow-lg transition hover:bg-white group-hover:opacity-100 disabled:pointer-events-none disabled:opacity-0"
                aria-label="Next image"
              >
                <ChevronRight className="h-5 w-5" />
              </button>
            </>
          </div>

          {/* Bottom Bar - Navigation */}
          <div className="sticky bottom-0 border-t border-[var(--border)] bg-white/90 px-6 py-3 backdrop-blur">
            <div className="flex items-center justify-center gap-4">
              <button
                onClick={handlePrevious}
                disabled={!previousId}
                className="flex items-center gap-2 rounded-lg bg-[var(--surface-muted)] px-4 py-2 text-sm font-semibold text-[var(--text)] transition hover:bg-[var(--surface-strong)] disabled:opacity-50"
              >
                <ChevronLeft className="h-4 w-4" />
                <span>Previous</span>
              </button>
              <button
                onClick={handleNext}
                disabled={!nextId}
                className="flex items-center gap-2 rounded-lg bg-[var(--surface-muted)] px-4 py-2 text-sm font-semibold text-[var(--text)] transition hover:bg-[var(--surface-strong)] disabled:opacity-50"
              >
                <span>Next</span>
                <ChevronRight className="h-4 w-4" />
              </button>
            </div>
          </div>
        </div>

        {/* Right Sidebar - Metadata (30%) */}
        {!isSidebarCollapsed && (
          <div className="w-[30%] overflow-y-auto bg-white/70">
            <MetadataEditor
              image={image}
              availableTags={tags}
              onSave={handleSaveMetadata}
              onAddTag={handleAddTag}
              onRemoveTag={handleRemoveTag}
            />
          </div>
        )}
      </div>
    </div>
  );
}
