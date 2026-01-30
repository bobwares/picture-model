/**
 * App: Picture Model
 * Package: ui/components
 * File: directory-tree.tsx
 * Version: 0.1.0
 * Turns: 4
 * Author: Claude
 * Date: 2026-01-29
 * Exports: DirectoryTree
 * Description: Full directory tree component with keyboard navigation
 */
'use client';

import { useState, useEffect } from 'react';
import { DirectoryTreeNodeComponent } from './directory-tree-node';
import type { DirectoryTreeNode } from '@/types';
import { FolderTree } from 'lucide-react';

interface DirectoryTreeProps {
  rootNode: DirectoryTreeNode;
  selectedPath?: string;
  onPathSelect: (path: string) => void;
  loading?: boolean;
}

export function DirectoryTree({
  rootNode,
  selectedPath,
  onPathSelect,
  loading = false,
}: DirectoryTreeProps) {
  const [treeData, setTreeData] = useState<DirectoryTreeNode>(rootNode);

  useEffect(() => {
    setTreeData(rootNode);
  }, [rootNode]);

  const toggleNode = (path: string) => {
    const updateNode = (node: DirectoryTreeNode): DirectoryTreeNode => {
      if (node.path === path) {
        return { ...node, expanded: !node.expanded };
      }
      if (node.children) {
        return {
          ...node,
          children: node.children.map(updateNode),
        };
      }
      return node;
    };

    setTreeData(updateNode(treeData));
  };

  if (loading) {
    return (
      <div className="flex h-full items-center justify-center rounded-xl border border-[var(--border)] bg-white/70 p-6">
        <div className="text-center">
          <div className="mx-auto h-8 w-8 animate-spin rounded-full border-2 border-[var(--border)] border-t-[var(--accent)]" />
          <p className="mt-3 text-sm text-[var(--muted)]">Loading directory tree...</p>
        </div>
      </div>
    );
  }

  if (!treeData || !treeData.children || treeData.children.length === 0) {
    return (
      <div className="flex h-full items-center justify-center rounded-xl border border-dashed border-[var(--border)] bg-white/70 p-6">
        <div className="text-center">
          <FolderTree className="mx-auto h-10 w-10 text-[var(--muted)]" />
          <p className="mt-3 text-sm text-[var(--muted)]">No directories found</p>
        </div>
      </div>
    );
  }

  return (
    <div className="h-full overflow-y-auto rounded-xl border border-[var(--border)] bg-white/70 p-3">
      <div className="space-y-0.5">
        <DirectoryTreeNodeComponent
          node={treeData}
          depth={0}
          selectedPath={selectedPath}
          onToggle={toggleNode}
          onSelect={onPathSelect}
        />
      </div>
    </div>
  );
}
