/**
 * App: Picture Model
 * Package: docs
 * File: image-detail-elements.md
 * Version: 0.1.0
 * Turns: 24
 * Author: Claude Sonnet 4.5
 * Date: 2026-02-02T02:30:00Z
 * Exports: None
 * Description: Reference map of image detail page elements and component responsibilities.
 */

# Image Detail Page Reference

This document names the visible UI regions and the component pieces used on the image detail page so you can request precise edits.

## 1) Image Detail View
**Route:** `/image/[driveId]/[imageId]`
**File:** `ui/app/image/[driveId]/[imageId]/page.tsx`

### Layout Overview
- **Global Header**: Top navigation (`Header` component).
- **Two-Panel Split**: 70/30 horizontal layout with no scrolling on parent.
  - **Left Panel (70%)**: Image viewer with controls and navigation.
  - **Right Sidebar (30%)**: Metadata editor panel.

### Left Panel - Image Viewer (70%)
Contains three main regions stacked vertically.

#### Top Bar
- **Back Button**: "Back to View" link with chevron icon, returns to previous page.
- **Zoom Controls Group**:
  - **Zoom Out Button**: decreases zoom level, disabled when at "Fit".
  - **Zoom Level Indicator**: displays "Fit" or percentage (100%–200%).
  - **Zoom In Button**: increases zoom level, disabled at 200%.
  - **Fit Button**: resets zoom to container-fit mode.
  - **Fullscreen Toggle Button**: enters/exits fullscreen mode, icon changes based on state.

#### Image Display Area
- **Container**: scrollable region with centered content, ref for fullscreen API.
- **Image Wrapper**: applies zoom transform when at numeric zoom level.
- **Image Element**: Next.js `Image` component with:
  - **Image Source**: `image.imageUrl` from API.
  - **Alt Text**: `image.fileName`.
  - **Dimensions**: `image.width` × `image.height` or defaults.
  - **Error State**: displays "Failed to load image" on load error.
- **Loading State**: spinner + "Loading image…" (shown before image data loads).
- **Not Found State**: "Image not found" + "Go back" link.

#### Bottom Bar
- **Navigation Controls**:
  - **Previous Button**: navigates to prior image in sequence, disabled when first.
  - **Next Button**: navigates to next image in sequence, disabled when last.
- **Navigation Context**: uses session storage (`image-nav:{driveId}`) for image IDs, falls back to loading first 500 images from drive.

### Right Sidebar - Metadata (30%)
- **Metadata Editor Panel**: `MetadataEditor` component, scrollable.
  - **Read-Only Fields**:
    - **Drive**: source drive name.
    - **Filename**: image file name.
    - **Size**: formatted file size (B/KB/MB/GB).
    - **Dimensions**: width × height in pixels (if available).
    - **Captured**: capture timestamp, absolute + relative (if available).
    - **Camera**: camera model from EXIF (if available).
    - **Path**: relative file path on drive.
  - **Tags Section**:
    - **Tag Pills**: removable chips for each attached tag.
    - **Add Tag Link**: placeholder for attaching new tags.
  - **Editable Custom Fields**:
    - **Description**: multiline text area, maps to metadata key `description`.
    - **Location**: single-line text input, maps to metadata key `location`.
    - **Notes**: multiline text area, maps to metadata key `notes`.
  - **Save Button**: persists custom metadata, enabled only when fields are modified (dirty state).
  - **Copy Full Path Button**: copies absolute path to clipboard.

## 2) Metadata Editor Component
**File:** `ui/components/metadata-editor.tsx`

- **Container**: white/70 background, full height with padding.
- **Sections**: grouped fields with headers and spacing.
  - **File Information Section**: read-only drive, filename, size, dimensions.
  - **Capture Information Section**: timestamp and camera (conditional).
  - **Tags Section**: tag pills + add link.
  - **Custom Metadata Section**: editable description, location, notes.
  - **File Path Section**: path display + copy button.
- **Tag Pill Component**:
  - **Tag Name**: label from tag data.
  - **Remove Button**: X icon, triggers tag removal mutation.
- **Save Changes Mutation**: calls `imageApi.updateMetadata()`.
- **Tag Mutations**: `imageApi.addTags()` and `imageApi.removeTag()`.

## 3) State Management

### Zoom State
- **Levels**: `'fit'` | `'full'` | number (100–200 in 25% increments).
- **Fit Mode**: image constrained to container with `max-width/max-height`.
- **Numeric Mode**: CSS transform scale applied to wrapper.

### Navigation State
- **navIds**: array of image IDs for prev/next sequencing.
- **currentIndex**: position of current image in navIds.
- **previousId**: ID of prior image or null.
- **nextId**: ID of next image or null.
- **Fallback**: queries first 500 images from drive if session storage empty.

### Fullscreen State
- **isFullscreen**: boolean tracking fullscreen API state.
- **Event Listener**: updates on `fullscreenchange` event.
- **Target**: `imageContainerRef` div becomes fullscreen element.

### Loading & Error States
- **isLoading**: shows spinner while fetching image data.
- **imageError**: shows error message if image fails to load.
- **Image not found**: renders 404 state if API returns no data.

## 4) Quick Reference Names (for edit requests)

Use these names when requesting changes:

### Layout & Structure
- **Global Header**
- **Left Panel** (70%)
- **Right Sidebar** (30%)
- **Top Bar**
- **Image Display Area**
- **Bottom Bar**

### Top Bar Elements
- **Back Button**
- **Zoom Controls Group**
- **Zoom Out Button**
- **Zoom In Button**
- **Zoom Level Indicator**
- **Fit Button**
- **Fullscreen Toggle Button**

### Image Display Elements
- **Image Container**
- **Image Wrapper**
- **Image Element**
- **Error State**
- **Loading State**
- **Not Found State**

### Navigation Elements
- **Previous Button**
- **Next Button**
- **Navigation Controls**

### Metadata Elements
- **Metadata Editor Panel**
- **Drive Field**
- **Filename Field**
- **Size Field**
- **Dimensions Field**
- **Captured Field**
- **Camera Field**
- **Tags Section**
- **Tag Pills**
- **Add Tag Link**
- **Description Field**
- **Location Field**
- **Notes Field**
- **Save Button**
- **Path Field**
- **Copy Full Path Button**

### Component References
- **MetadataEditor** component (`ui/components/metadata-editor.tsx`)
- **Tag Pill** component (within MetadataEditor)

## 5) Data Flow

### Image Data
```
Query: ['image', imageId] → imageApi.getById(imageId)
Returns: Image object with metadata, URLs, EXIF data
Used by: Image display, metadata editor
```

### Tags Data
```
Query: ['tags'] → tagApi.getAll()
Returns: Array of all system tags
Used by: Tag selection in metadata editor
```

### Navigation Data
```
Session Storage: image-nav:{driveId} → {ids: string[]}
Fallback Query: ['image-nav', driveId] → driveApi.getImages() (first 500)
Used by: Previous/Next navigation
```

### Mutations
```
updateMetadataMutation → imageApi.updateMetadata(imageId, metadata)
addTagMutation → imageApi.addTags(imageId, [tagId])
removeTagMutation → imageApi.removeTag(imageId, tagId)
All invalidate: ['image', imageId] query
```

## 6) URL Parameters

- **driveId**: UUID of the source drive (from route params).
- **imageId**: UUID of the image to display (from route params).

## 7) Keyboard & Interaction Patterns

### Zoom Interactions
- Click Zoom Out: decrease by 25% or return to Fit
- Click Zoom In: increase by 25% (max 200%)
- Click Fit: reset to container-fit mode
- Click Fullscreen: toggle fullscreen API

### Navigation Interactions
- Click Previous: navigate to prior image in sequence
- Click Next: navigate to next image in sequence
- Disabled state: when at sequence boundaries

### Metadata Interactions
- Edit fields: triggers dirty state
- Click Save: persists changes via mutation
- Click tag X: removes tag via mutation
- Click Add Tag: (placeholder, not yet functional)
- Click Copy Path: copies to clipboard

## 8) Responsive Behavior

- **Desktop (default)**: 70/30 split, all controls visible.
- **Tablet/Mobile**: Layout remains fixed at 70/30 (no responsive breakpoints in current implementation).
- **Fullscreen**: Image container expands to full viewport, controls overlay.

## 9) Related Components

- **Header** (`ui/components/header.tsx`): Global navigation bar.
- **MetadataEditor** (`ui/components/metadata-editor.tsx`): Right sidebar content.
- **Image** (Next.js): Optimized image component with error handling.

## 10) API Dependencies

- `imageApi.getById(imageId)`: Fetch image details.
- `imageApi.updateMetadata(imageId, metadata)`: Save custom metadata.
- `imageApi.addTags(imageId, tagIds)`: Attach tags to image.
- `imageApi.removeTag(imageId, tagId)`: Remove tag from image.
- `tagApi.getAll()`: Fetch all available tags.
- `driveApi.getImages(driveId, params)`: Fetch images for fallback navigation.
