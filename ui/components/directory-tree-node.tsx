/**
 * App: Picture Model
 * Package: ui/components
 * File: directory-tree-node.tsx
 * Version: 0.1.0
 * Turns: 4
 * Author: Claude
 * Date: 2026-01-29
 * Exports: DirectoryTreeNodeComponent
 * Description: Single directory tree node with expand/collapse functionality
 */
'use client';

import { ChevronRight, Folder, FolderOpen } from 'lucide-react';
import type { DirectoryTreeNode } from '@/types';

interface DirectoryTreeNodeProps {
  node: DirectoryTreeNode;
  depth: number;
  selectedPath?: string;
  onToggle: (path: string) => void;
  onSelect: (path: string) => void;
}

export function DirectoryTreeNodeComponent({
  node,
  depth,
  selectedPath,
  onToggle,
  onSelect,
}: DirectoryTreeNodeProps) {
  const isExpanded = node.expanded || false;
  const isSelected = selectedPath === node.path;
  const hasChildren = node.children && node.children.length > 0;

  const handleToggle = (e: React.MouseEvent) => {
    e.stopPropagation();
    onToggle(node.path);
  };

  const handleSelect = () => {
    onSelect(node.path);
  };

  return (
    <div className="select-none">
      <div
        className={`flex items-center gap-1.5 rounded-lg px-2 py-1.5 text-sm transition-colors ${
          isSelected
            ? 'bg-[var(--accent-soft)] text-[var(--accent-ink)] font-semibold'
            : 'text-[var(--text)] hover:bg-[var(--surface-muted)]'
        }`}
        style={{ paddingLeft: `${depth * 12 + 8}px` }}
        onClick={handleSelect}
      >
        {/* Expand/Collapse Button */}
        {hasChildren ? (
          <button
            onClick={handleToggle}
            className="flex h-4 w-4 items-center justify-center rounded hover:bg-black/5"
          >
            <ChevronRight
              className={`h-3.5 w-3.5 transition-transform ${isExpanded ? 'rotate-90' : ''}`}
            />
          </button>
        ) : (
          <div className="h-4 w-4" />
        )}

        {/* Folder Icon */}
        {isExpanded ? (
          <FolderOpen className="h-4 w-4 flex-shrink-0 text-[var(--accent)]" />
        ) : (
          <Folder className="h-4 w-4 flex-shrink-0 text-[var(--muted)]" />
        )}

        {/* Folder Name */}
        <span className="flex-1 truncate">{node.name}</span>

        {/* Image Count */}
        {node.imageCount > 0 && (
          <span className="ml-auto flex-shrink-0 text-xs text-[var(--muted)]">
            {node.imageCount}
          </span>
        )}
      </div>

      {/* Children */}
      {isExpanded && hasChildren && (
        <div className="mt-0.5">
          {node.children.map((child) => (
            <DirectoryTreeNodeComponent
              key={child.path}
              node={child}
              depth={depth + 1}
              selectedPath={selectedPath}
              onToggle={onToggle}
              onSelect={onSelect}
            />
          ))}
        </div>
      )}
    </div>
  );
}
