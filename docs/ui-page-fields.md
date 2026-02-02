# UI Page Fields Reference

Each section identifies a page by route, lists every visible field or control, describes what it represents, and notes which component renders it.

---

## 1. Dashboard (`/`)

**Page:** `ui/app/page.tsx`

### Hero Section

| Field | Description | Component |
|---|---|---|
| Dashboard label | Static uppercase label above the hero title | `page.tsx` |
| Hero title | "Remote drive control center" — the main heading | `page.tsx` |
| Hero description | Explanatory text beneath the title | `page.tsx` |
| Add New Drive button | Opens the Add Drive modal | `page.tsx` → `AddDriveModal` |

### Stats Row

| Field | Description | Component |
|---|---|---|
| Total Images | Count of all indexed images across all drives | `StatsCard` via `systemApi.getStatus().totalImages` |
| Total Drives | Number of configured drives | `StatsCard` via `systemStatus.totalDrives` |
| Total Tags | Number of tags in the system | `StatsCard` via `systemStatus.totalTags` |
| Active Crawls | Number of crawl jobs currently running | `StatsCard` via `systemStatus.activeCrawls` |

### Drive List

| Field | Description | Component |
|---|---|---|
| Drive name | User-assigned label for the drive | `DriveCard` — `drive.name` |
| Connection status badge | CONNECTED / CONNECTING / DISCONNECTED / ERROR with color and icon | `DriveCard` — `drive.status` |
| Connection URL | The filesystem path or network address | `DriveCard` — `drive.connectionUrl` |
| Image count | Number of images indexed on this drive | `DriveCard` — `drive.imageCount` |
| Last crawled | Relative timestamp of the most recent crawl | `DriveCard` — `drive.lastCrawled` |
| Connect / Disconnect button | Toggles the drive connection state | `DriveCard` |
| Browse Tree button | Navigates to `/tree/{driveId}` (visible when connected) | `DriveCard` |
| View by Tags button | Placeholder action (visible when connected) | `DriveCard` |
| Edit button | Opens the Edit Drive modal | `DriveCard` → `EditDriveModal` |
| Delete button | Confirms and deletes the drive (visible when disconnected) | `DriveCard` |

### Recent Activity

| Field | Description | Component |
|---|---|---|
| Status icon | Visual indicator: green check (COMPLETED), red X (FAILED / CANCELLED), pulsing blue (IN_PROGRESS), clock (other) | `RecentActivity` |
| Activity label | "Crawl {status} on {driveName}" | `RecentActivity` — `job.status`, `job.driveName` |
| Files processed | Total file count from the crawl job | `RecentActivity` — `job.filesProcessed` |
| Crawl time | Relative timestamp of when the crawl started | `RecentActivity` — `job.startTime` |

---

## 2. Directory Tree Placeholder (`/tree`)

**Page:** `ui/app/tree/page.tsx`

Static placeholder page. No data-driven fields — displays a "coming soon" message with a folder icon.

---

## 3. Directory Tree View (`/tree/[driveId]`)

**Page:** `ui/app/tree/[driveId]/page.tsx`

### Breadcrumb

| Field | Description | Component |
|---|---|---|
| Dashboard link | Back-navigation to `/` | `page.tsx` |
| Drive name | Name of the currently selected drive | `page.tsx` — `drive.name` |
| Selected path | The currently active directory path | `page.tsx` — `selectedPath` |

### Left Sidebar — Directory Tree

| Field | Description | Component |
|---|---|---|
| Folder name | Name of each directory node | `DirectoryTreeNodeComponent` — `node.name` |
| Expand/collapse chevron | Toggles child directories | `DirectoryTreeNodeComponent` |
| Folder icon | Open or closed folder depending on expand state | `DirectoryTreeNodeComponent` |
| Image count | Number of images directly in that folder | `DirectoryTreeNodeComponent` — `node.imageCount` |
| Selected state highlight | Accent background on the active path | `DirectoryTreeNodeComponent` — compared against `selectedPath` |

### Right Panel — Controls

| Field | Description | Component |
|---|---|---|
| Current path label | Displays the selected directory path | `page.tsx` |
| Image count badge | Total images in the selected path | `page.tsx` |
| Sort dropdown | Orders images by Date / Name / Size | `page.tsx` — `sortBy` |
| Select All / Deselect All button | Toggles selection on all visible images | `page.tsx` |
| Batch Tag button | Action button showing count of selected images | `page.tsx` |

### Right Panel — Image Grid

| Field | Description | Component |
|---|---|---|
| Thumbnail | Image preview at medium (250×250) size | `ImageThumbnail` — `image.thumbnailUrl` |
| Selection checkbox | Circle overlay on hover; filled when selected | `ImageThumbnail` |
| Filename overlay | Filename shown on hover over thumbnail | `ImageThumbnail` — `image.fileName` |
| Captured date overlay | Capture date shown on hover beneath filename | `ImageThumbnail` — `image.capturedAt` |
| Load more / end indicator | Infinite scroll trigger or "All images loaded" text | `ImageGrid` |

---

## 4. Tags (`/tags`)

**Page:** `ui/app/tags/page.tsx`

### Left Sidebar — Tag List

| Field | Description | Component |
|---|---|---|
| Tag count header | "All Tags (N)" showing total number of tags | `page.tsx` |
| Tag sort dropdown | Orders tags by Name / Most Used / Recent | `page.tsx` — `tagSortBy` |
| Tag checkbox | Selectable tag; highlights when active | `page.tsx` |
| Tag name | Label for the tag | `page.tsx` — `tag.name` |
| Tag usage count | Number of images using the tag, shown in parentheses | `page.tsx` — `tag.usageCount` |
| Create Tag button | Placeholder action to add a new tag | `page.tsx` |

### Left Sidebar — Drive Filter

| Field | Description | Component |
|---|---|---|
| All Drives checkbox | Clears the drive filter | `page.tsx` |
| Drive checkbox | Filters images to a specific drive | `page.tsx` — `drive.name` |

### Right Panel — Selected Tags Bar

| Field | Description | Component |
|---|---|---|
| Selected tag pills | Chips showing each active tag and the matching image count | `page.tsx` — `tag.name`, `totalImages` |

### Right Panel — Controls & Image Grid

Same fields as the Directory Tree View right panel (sort dropdown, batch actions, image grid with thumbnails).

---

## 5. Search (`/search`)

**Page:** `ui/app/search/page.tsx`

### Search Bar

| Field | Description | Component |
|---|---|---|
| Search input | Free-text query for filename, metadata, or tags | `page.tsx` — `searchInput` |
| Search button | Submits the query and updates the URL | `page.tsx` |

### Left Sidebar — Filter Panel

| Field | Description | Component |
|---|---|---|
| **Drives** section header | Collapsible; lists all drives as checkboxes | `FilterPanel` |
| Drive checkbox | Filters results to a single drive | `FilterPanel` — `drive.name` |
| **Date Range** section header | Collapsible | `FilterPanel` |
| Date From | Start date picker | `FilterPanel` — `dateFrom` |
| Date To | End date picker | `FilterPanel` — `dateTo` |
| **File Type** section header | Collapsible | `FilterPanel` |
| File type checkbox | Filters by format: JPEG / PNG / RAW / GIF / WEBP | `FilterPanel` — `selectedFileTypes` |
| **Tags** section header | Collapsible | `FilterPanel` |
| Tag checkbox | Filters by tag; shows name and usage count | `FilterPanel` — `tag.name`, `tag.usageCount` |
| Clear All link | Resets all active filters | `FilterPanel` |

### Right Panel — Results Summary

| Field | Description | Component |
|---|---|---|
| Search query heading | "Search Results for {query}" | `page.tsx` |
| Result count | "Found N image(s)" with active-filter note | `page.tsx` — `totalImages` |
| Sort dropdown | Orders by Relevance / Date / Name / Size | `page.tsx` — `sortBy` |

### Right Panel — Batch Actions & Image Grid

Same fields as Directory Tree View: selection count, Select All / Deselect All, Batch Tag button, and the image grid with thumbnails.

---

## 6. Image Detail (`/image/[driveId]/[imageId]`)

**Page:** `ui/app/image/[driveId]/[imageId]/page.tsx`

### Left Panel (70%) — Image Viewer

| Field | Description | Component |
|---|---|---|
| Back to View link | Returns to the previous page | `page.tsx` |
| Zoom Out button | Decreases zoom; disabled at "Fit" | `page.tsx` |
| Zoom level indicator | Shows "Fit" or current percentage (100%–200%) | `page.tsx` — `zoomLevel` |
| Zoom In button | Increases zoom; disabled at 200% | `page.tsx` |
| Fit button | Resets zoom to container-fit | `page.tsx` |
| Image display | The full image, scaled to zoom level | `page.tsx` — `image.imageUrl` |
| Previous button | Navigates to the prior image (stub) | `page.tsx` |
| Next button | Navigates to the next image (stub) | `page.tsx` |

### Right Sidebar (30%) — Metadata Editor

| Field | Description | Component |
|---|---|---|
| Drive | Name of the source drive | `MetadataEditor` — `image.driveName` |
| Filename | The image file name | `MetadataEditor` — `image.fileName` |
| Size | File size formatted (B / KB / MB / GB) | `MetadataEditor` — `image.fileSize` |
| Dimensions | Width × Height in pixels (if available) | `MetadataEditor` — `image.width`, `image.height` |
| Captured | Absolute and relative capture timestamp (if available) | `MetadataEditor` — `image.capturedAt` |
| Camera | Camera model from EXIF metadata (if available) | `MetadataEditor` — metadata key `camera` |
| Tags | Pills for each tag on the image; removable | `MetadataEditor` → `TagPill` |
| Add Tag link | Placeholder to attach a tag | `MetadataEditor` |
| Description (editable) | User-entered text area; maps to metadata key `description` | `MetadataEditor` — `customDescription` |
| Location (editable) | User-entered text input; maps to metadata key `location` | `MetadataEditor` — `customLocation` |
| Notes (editable) | User-entered text area; maps to metadata key `notes` | `MetadataEditor` — `customNotes` |
| Save Changes button | Persists editable custom metadata; enabled only when dirty | `MetadataEditor` |
| Path | Relative file path on the drive | `MetadataEditor` — `image.filePath` |
| Copy Full Path | Copies the absolute path to clipboard | `MetadataEditor` — `image.fullPath` |

---

## 7. Settings (`/settings`)

**Page:** `ui/app/settings/page.tsx`

### Display

| Field | Description | Component |
|---|---|---|
| Thumbnail Size | Radio: small / medium / large | `page.tsx` — `thumbnailSize` |
| Images per page | Dropdown: 12 / 24 / 48 / 96 | `page.tsx` — `imagesPerPage` |
| Theme | Radio: light / dark / auto | `page.tsx` — `theme` |
| Default View | Radio: Directory Tree / tags / search | `page.tsx` — `defaultView` |

### Remote Drives

| Field | Description | Component |
|---|---|---|
| Connection Timeout | Dropdown: 10 / 30 / 60 / 120 seconds | `page.tsx` — `connectionTimeout` |
| Auto-reconnect on startup | Checkbox | `page.tsx` — `autoReconnect` |
| Remember credentials (encrypted) | Checkbox | `page.tsx` — `rememberCredentials` |
| Manage Remote Drives link | Navigates back to Dashboard | `page.tsx` |

### Crawler

| Field | Description | Component |
|---|---|---|
| Auto-crawl on drive connection | Checkbox | `page.tsx` — `autoCrawl` |
| Extract EXIF metadata | Checkbox | `page.tsx` — `extractExif` |
| Generate thumbnails | Checkbox | `page.tsx` — `generateThumbnails` |
| Crawl Schedule | Radio: Manual only / Daily at {time} / Weekly on {day} | `page.tsx` — `crawlSchedule` |
| Crawl time | Time picker; enabled only when schedule is Daily | `page.tsx` — `crawlTime` |
| Crawl day | Dropdown (Sun–Sat); enabled only when schedule is Weekly | `page.tsx` — `crawlDay` |

### Performance

| Field | Description | Component |
|---|---|---|
| Thumbnail quality | Dropdown: low / medium / high | `page.tsx` — `thumbnailQuality` |
| Maximum concurrent crawls | Dropdown: 1 / 2 / 4 / 8 | `page.tsx` — `maxConcurrentCrawls` |
| Cache thumbnails locally | Checkbox | `page.tsx` — `cacheThumbnails` |
| Preload next/previous images | Checkbox | `page.tsx` — `preloadImages` |

### Database

| Field | Description | Component |
|---|---|---|
| Total Images | Live count from system status | `page.tsx` — `systemStatus.totalImages` |
| Total Drives | Live count from system status | `page.tsx` — `systemStatus.totalDrives` |
| Database Size | Placeholder value (~245 MB) | `page.tsx` |
| Clear Thumbnail Cache | Action button (stub) | `page.tsx` |
| Rebuild Search Index | Action button (stub) | `page.tsx` |
| Export Database | Action button (stub) | `page.tsx` |
| Import Database | Action button (stub) | `page.tsx` |

### Save

| Field | Description | Component |
|---|---|---|
| Save Settings button | Submits all settings; disabled until a change is made | `page.tsx` — `hasChanges` |

---

## 8. Add Drive Modal

**Trigger:** "Add New Drive" button on Dashboard
**Component:** `ui/components/add-drive-modal.tsx`

| Field | Description | Validation |
|---|---|---|
| Drive Name | User-facing label for the drive | Required, max 200 chars |
| Drive Type | Dropdown: Local Filesystem / SMB / SFTP / FTP | Required |
| Connection URL | Filesystem path or network address; placeholder updates per type | Required, max 500 chars |
| Root Path | Starting directory within the connection | Optional, defaults to `/`, max 500 chars |
| Credentials (JSON) | Auth payload as a JSON object; only shown for SMB / SFTP / FTP | Optional, must be valid JSON |
| Auto-connect on startup | Checkbox | Defaults to false |
| Auto-crawl after connection | Checkbox | Defaults to false |
| Error banner | Displays API or validation errors | — |
| Cancel / Create Drive buttons | Dismiss or submit the form | Create disabled while pending |

---

## 9. Edit Drive Modal

**Trigger:** "Edit" button on a DriveCard
**Component:** `ui/components/edit-drive-modal.tsx`

Same fields as Add Drive Modal with two differences:

| Difference | Detail |
|---|---|
| Drive Type | Read-only / disabled — type cannot change after creation |
| Credentials field | Starts empty; leaving it blank does not overwrite existing credentials |
