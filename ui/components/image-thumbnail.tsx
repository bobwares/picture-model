/**
 * App: Picture Model
 * Package: ui/components
 * File: image-thumbnail.tsx
 * Version: 0.1.0
 * Turns: 4
 * Author: Claude
 * Date: 2026-01-29
 * Exports: ImageThumbnail
 * Description: Image thumbnail component with selection and hover states
 */
'use client';

import { useState } from 'react';
import Image from 'next/image';
import { Check } from 'lucide-react';
import type { Image as ImageType } from '@/types';

interface ImageThumbnailProps {
  image: ImageType;
  selected?: boolean;
  onSelect?: (imageId: string) => void;
  onClick?: (imageId: string) => void;
  size?: 'small' | 'medium' | 'large';
}

export function ImageThumbnail({
  image,
  selected = false,
  onSelect,
  onClick,
  size = 'medium',
}: ImageThumbnailProps) {
  const [imageError, setImageError] = useState(false);

  const sizeClasses = {
    small: 'h-[150px] w-[150px]',
    medium: 'h-[250px] w-[250px]',
    large: 'h-[350px] w-[350px]',
  };

  const handleClick = (e: React.MouseEvent) => {
    if (e.shiftKey || e.ctrlKey || e.metaKey) {
      e.preventDefault();
      onSelect?.(image.id);
    } else {
      onClick?.(image.id);
    }
  };

  const handleSelectClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    onSelect?.(image.id);
  };

  return (
    <div
      className={`group relative cursor-pointer overflow-hidden rounded-xl border-2 transition-all ${
        selected
          ? 'border-[var(--accent)] shadow-lg ring-2 ring-[var(--accent)]/30'
          : 'border-transparent hover:border-[var(--border)] hover:shadow-md'
      } ${sizeClasses[size]}`}
      onClick={handleClick}
    >
      {/* Selection Checkbox */}
      {onSelect && (
        <div
          className={`absolute left-2 top-2 z-10 flex h-6 w-6 items-center justify-center rounded-full border-2 transition-all ${
            selected
              ? 'border-[var(--accent)] bg-[var(--accent)] text-white'
              : 'border-white bg-white/80 opacity-0 group-hover:opacity-100'
          }`}
          onClick={handleSelectClick}
        >
          {selected && <Check className="h-4 w-4" />}
        </div>
      )}

      {/* Image */}
      <div className="relative h-full w-full bg-[var(--surface-muted)]">
        {!imageError ? (
          <Image
            src={image.thumbnailUrl}
            alt={image.fileName}
            fill
            sizes={size === 'small' ? '150px' : size === 'medium' ? '250px' : '350px'}
            className="object-cover"
            onError={() => setImageError(true)}
          />
        ) : (
          <div className="flex h-full w-full items-center justify-center text-[var(--muted)]">
            <span className="text-xs">Failed to load</span>
          </div>
        )}
      </div>

      {/* Overlay with filename */}
      <div className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/70 to-transparent p-3 opacity-0 transition-opacity group-hover:opacity-100">
        <p className="truncate text-xs font-medium text-white">{image.fileName}</p>
        {image.capturedAt && (
          <p className="mt-0.5 text-xs text-white/70">
            {new Date(image.capturedAt).toLocaleDateString()}
          </p>
        )}
      </div>
    </div>
  );
}
