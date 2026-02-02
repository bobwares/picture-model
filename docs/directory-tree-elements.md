/**
 * App: Picture Model
 * Package: docs
 * File: directory-tree-elements.md
 * Version: 0.1.0
 * Turns: 1
 * Author: Codex
 * Date: 2026-02-02T06:26:38Z
 * Exports: None
 * Description: Reference map of directory tree page elements and component responsibilities.
 */

# Directory Tree Page Reference

This document names the visible UI regions and the component pieces used on the directory tree pages so you can request precise edits.

## 1) Directory Tree View (drive-specific)
**Route:** `/tree/[driveId]`
**File:** `ui/app/tree/[driveId]/page.tsx`

### Layout Regions
- **Global Header**: Top navigation (`Header` component).
- **Breadcrumb Bar**: “Dashboard / {Drive Name} / {Selected Path}” with back button.
- **Main Split Layout**: Two-column grid on large screens.
  - **Left Sidebar**: Directory tree container (collapsible).
  - **Right Content**: Controls + image grid.

### Left Sidebar
- **Sidebar Header**: “Directory Tree” label + collapse/expand button.
- **Directory Tree Panel**: Renders `DirectoryTree` component.
  - **Loading State**: spinner + “Loading directory tree…”.
  - **Empty State**: folder icon + “No directories found”.

### Right Content
- **Controls Bar**:
  - **Path Label**: “Path: {selectedPath}” or “Select a folder”.
  - **Image Count Pill**: number of visible images when > 0.
  - **Sort Dropdown**: Date / Name / Size.
  - **Batch Actions**: Select All / Deselect All + Batch Tag when selections exist.
- **Image Grid**: `ImageGrid` component with selection, click-to-open, and load-more.
  - **Empty Message**: “No images in this directory” or “Select a folder to view images”.

## 2) Directory Tree Placeholder (no drive selected)
**Route:** `/tree`
**File:** `ui/app/tree/page.tsx`

- **Centered Empty Card**: icon, “Directory Tree” title, short description, and “Tree navigation coming soon”.

## 3) Directory Tree Component (container)
**File:** `ui/components/directory-tree.tsx`

- **Tree Container**: Scrollable panel with padding and rounded border.
- **States**:
  - **Loading**: spinner + “Loading directory tree…”.
  - **Empty**: folder icon + “No directories found”.
- **Root Node Render**: `DirectoryTreeNodeComponent` for the root node and children.

## 4) Directory Tree Node (row)
**File:** `ui/components/directory-tree-node.tsx`

- **Node Row**:
  - **Chevron**: expand/collapse (only if children).
  - **Folder Icon**: closed or open based on expanded state.
  - **Folder Name**: truncated label.
  - **Image Count**: right-aligned count when > 0.
- **Row States**:
  - **Selected**: accent background + bold text.
  - **Hover**: surface-muted background.
- **Indentation**: left padding increases by depth level.

## 5) Quick Reference Names (for edit requests)
Use these names when requesting changes:
- **Global Header**
- **Breadcrumb Bar**
- **Left Sidebar**
- **Sidebar Header**
- **Directory Tree Panel**
- **Controls Bar**
- **Path Label**
- **Image Count Pill**
- **Sort Dropdown**
- **Batch Actions**
- **Image Grid**
- **Placeholder Card**
- **Tree Container**
- **Tree Node Row**
- **Chevron**
- **Folder Icon**
- **Folder Name**
- **Image Count**
