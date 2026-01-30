/**
 * App: Picture Model
 * Package: ui/components
 * File: tag-pill.tsx
 * Version: 0.1.0
 * Turns: 4
 * Author: Claude
 * Date: 2026-01-29
 * Exports: TagPill
 * Description: Tag display component with optional remove functionality
 */
'use client';

import { X } from 'lucide-react';
import type { Tag } from '@/types';

interface TagPillProps {
  tag: Tag;
  removable?: boolean;
  selected?: boolean;
  onRemove?: (tagId: string) => void;
  onClick?: (tagId: string) => void;
  showCount?: boolean;
}

export function TagPill({
  tag,
  removable = false,
  selected = false,
  onRemove,
  onClick,
  showCount = false,
}: TagPillProps) {
  const handleRemove = (e: React.MouseEvent) => {
    e.stopPropagation();
    onRemove?.(tag.id);
  };

  const handleClick = () => {
    onClick?.(tag.id);
  };

  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full border px-3 py-1 text-sm font-medium transition-all ${
        selected
          ? 'border-[var(--accent)] bg-[var(--accent-soft)] text-[var(--accent-ink)]'
          : 'border-[var(--border)] bg-white/70 text-[var(--text)] hover:border-[var(--accent)] hover:bg-[var(--accent-soft)]/50'
      } ${onClick ? 'cursor-pointer' : ''}`}
      onClick={onClick ? handleClick : undefined}
      style={
        tag.color
          ? {
              borderColor: tag.color,
              backgroundColor: selected ? tag.color + '20' : 'white',
              color: tag.color,
            }
          : undefined
      }
    >
      <span>{tag.name}</span>
      {showCount && <span className="text-xs opacity-70">({tag.usageCount})</span>}
      {removable && (
        <button
          onClick={handleRemove}
          className="rounded-full p-0.5 hover:bg-black/10"
          aria-label={`Remove ${tag.name} tag`}
        >
          <X className="h-3 w-3" />
        </button>
      )}
    </span>
  );
}
