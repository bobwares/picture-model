/**
 * App: Picture Model
 * Package: ui/components
 * File: image-grid.tsx
 * Version: 0.1.1
 * Turns: 4,9
 * Author: Claude
 * Date: 2026-01-31T21:53:39Z
 * Exports: ImageGrid
 * Description: Responsive image grid with infinite scroll support
 */
'use client';

import { useEffect, useMemo, useRef, useState } from 'react';
import { ImageThumbnail } from './image-thumbnail';
import type { Image } from '@/types';
import { Image as ImageIcon } from 'lucide-react';

interface ImageGridProps {
  images: Image[];
  loading?: boolean;
  selectedImageIds?: Set<string>;
  onImageSelect?: (imageId: string) => void;
  onImageClick?: (imageId: string) => void;
  onLoadMore?: () => void;
  hasMore?: boolean;
  size?: 'small' | 'medium' | 'large';
  emptyMessage?: string;
  resetKey?: string;
}

export function ImageGrid({
  images,
  loading = false,
  selectedImageIds = new Set(),
  onImageSelect,
  onImageClick,
  onLoadMore,
  hasMore = false,
  size = 'medium',
  emptyMessage = 'No images found',
  resetKey,
}: ImageGridProps) {
  const observerTarget = useRef<HTMLDivElement>(null);
  const [isIntersecting, setIsIntersecting] = useState(false);
  const [failedImages, setFailedImages] = useState<
    { id: string; fileName: string; order: number }[]
  >([]);
  const [showFailed, setShowFailed] = useState(false);

  // Infinite scroll observer
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) {
          setIsIntersecting(true);
        }
      },
      { threshold: 0.1 }
    );

    const currentTarget = observerTarget.current;
    if (currentTarget) {
      observer.observe(currentTarget);
    }

    return () => {
      if (currentTarget) {
        observer.unobserve(currentTarget);
      }
    };
  }, []);

  // Trigger load more when scrolled to bottom
  useEffect(() => {
    if (isIntersecting && hasMore && !loading && onLoadMore) {
      onLoadMore();
      setIsIntersecting(false);
    }
  }, [isIntersecting, hasMore, loading, onLoadMore]);

  useEffect(() => {
    setFailedImages([]);
    setShowFailed(false);
  }, [resetKey]);

  const gridClasses = {
    small: 'grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 xl:grid-cols-8',
    medium: 'grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6',
    large: 'grid-cols-1 sm:grid-cols-2 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4',
  };

  const failedIds = useMemo(() => new Set(failedImages.map((failed) => failed.id)), [failedImages]);
  const visibleImages = images.filter((image) => !failedIds.has(image.id));

  const registerFailedImage = (image: Image) => {
    setFailedImages((prev) => {
      if (prev.some((entry) => entry.id === image.id)) {
        return prev;
      }
      const order = images.findIndex((item) => item.id === image.id) + 1;
      return [...prev, { id: image.id, fileName: image.fileName, order }];
    });
  };

  if (!loading && visibleImages.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-[var(--border)] bg-white/70 py-16 text-center backdrop-blur">
        <ImageIcon className="h-12 w-12 text-[var(--muted)]" />
        <p className="mt-4 text-sm text-[var(--muted)]">{emptyMessage}</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {failedImages.length > 0 && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
          <button
            onClick={() => setShowFailed(true)}
            className="font-semibold underline decoration-rose-300 underline-offset-4 hover:text-rose-800"
          >
            {failedImages.length} images failed to load
          </button>
        </div>
      )}

      <div className={`grid gap-4 ${gridClasses[size]}`}>
        {visibleImages.map((image) => (
          <ImageThumbnail
            key={image.id}
            image={image}
            selected={selectedImageIds.has(image.id)}
            onSelect={onImageSelect}
            onClick={onImageClick}
            onError={registerFailedImage}
            size={size}
          />
        ))}
      </div>

      {/* Loading indicator */}
      {loading && (
        <div className="flex justify-center py-8">
          <div className="h-8 w-8 animate-spin rounded-full border-2 border-[var(--border)] border-t-[var(--accent)]" />
        </div>
      )}

      {/* Infinite scroll trigger */}
      {hasMore && !loading && <div ref={observerTarget} className="h-4" />}

      {/* End of results */}
      {!hasMore && visibleImages.length > 0 && (
        <p className="py-4 text-center text-sm text-[var(--muted)]">
          All images loaded ({visibleImages.length} total)
        </p>
      )}

      {showFailed && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="w-full max-w-2xl rounded-2xl border border-[var(--border)] bg-white p-6 shadow-xl">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold text-[var(--text)]">Failed Images</h3>
              <button
                onClick={() => setShowFailed(false)}
                className="rounded-lg bg-[var(--surface-muted)] px-3 py-1.5 text-sm font-semibold text-[var(--text)]"
              >
                Close
              </button>
            </div>
            <div className="mt-4 max-h-[50vh] overflow-y-auto">
              {failedImages.map((failed) => (
                <div
                  key={failed.id}
                  className="flex items-center justify-between border-b border-[var(--border)] py-2 text-sm text-[var(--text)] last:border-b-0"
                >
                  <div className="min-w-0">
                    <p className="truncate font-semibold">
                      #{failed.order} {failed.fileName}
                    </p>
                    <p className="text-xs text-[var(--muted)]">{failed.id}</p>
                  </div>
                  {onImageClick && (
                    <button
                      onClick={() => onImageClick(failed.id)}
                      className="rounded-lg bg-[var(--accent-soft)] px-3 py-1.5 text-xs font-semibold text-[var(--accent-ink)]"
                    >
                      Open
                    </button>
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
