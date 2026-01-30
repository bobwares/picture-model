/**
 * App: Picture Model
 * Package: ui/components
 * File: image-grid.tsx
 * Version: 0.1.0
 * Turns: 4
 * Author: Claude
 * Date: 2026-01-29
 * Exports: ImageGrid
 * Description: Responsive image grid with infinite scroll support
 */
'use client';

import { useEffect, useRef, useState } from 'react';
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
}: ImageGridProps) {
  const observerTarget = useRef<HTMLDivElement>(null);
  const [isIntersecting, setIsIntersecting] = useState(false);

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

  const gridClasses = {
    small: 'grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 xl:grid-cols-8',
    medium: 'grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6',
    large: 'grid-cols-1 sm:grid-cols-2 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4',
  };

  if (!loading && images.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-[var(--border)] bg-white/70 py-16 text-center backdrop-blur">
        <ImageIcon className="h-12 w-12 text-[var(--muted)]" />
        <p className="mt-4 text-sm text-[var(--muted)]">{emptyMessage}</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className={`grid gap-4 ${gridClasses[size]}`}>
        {images.map((image) => (
          <ImageThumbnail
            key={image.id}
            image={image}
            selected={selectedImageIds.has(image.id)}
            onSelect={onImageSelect}
            onClick={onImageClick}
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
      {!hasMore && images.length > 0 && (
        <p className="py-4 text-center text-sm text-[var(--muted)]">
          All images loaded ({images.length} total)
        </p>
      )}
    </div>
  );
}
