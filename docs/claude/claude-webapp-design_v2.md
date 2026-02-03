---
**Created by:** Claude (AI Coding Agent)
**Date:** 2026-01-27
**Version:** 2.0
---

# Picture Model App - Web Application Design (v2)
## Based on User Decisions

## 1. Application Overview

The web app is a single-page application (SPA) that provides an intuitive interface for connecting to remote file drives, browsing directory trees, and managing image collections with multiple view modes (directory tree, tags, search).

**Selected Design Choices:**
- ✅ Landing Page: Dashboard-First with remote file drives
- ✅ Image Detail Layout: Right Sidebar (image left, metadata right)
- ✅ Thumbnail Size: Medium (250x250px, 4-6 per row)
- ✅ Color Theme: Light mode

## 2. Architecture Update: Remote File Drives

### 2.1 New Domain Concepts

#### RemoteFileDrive
- **Attributes:**
  - `id`: Unique identifier
  - `name`: User-friendly name (e.g., "Home NAS", "External Drive 1")
  - `type`: LOCAL, NETWORK_SHARE, FTP, SFTP, S3, etc.
  - `connectionUrl`: Connection string or path
  - `credentials`: Encrypted credentials (if needed)
  - `status`: CONNECTED, DISCONNECTED, ERROR
  - `lastConnected`: Timestamp
  - `rootPath`: Root directory on the drive

- **Connection Types:**
  - **Local:** Direct file system access (`/Users/user/Pictures`)
  - **Network Share:** SMB/CIFS (`smb://server/share`)
  - **SFTP:** SSH file transfer (`sftp://user@server/path`)
  - **FTP:** File transfer protocol (`ftp://server/path`)
  - **Cloud (future):** S3, Google Drive, Dropbox

### 2.2 Updated System Flow

```
1. User lands on Dashboard
2. User sees list of configured remote drives
3. User clicks "Connect" on a drive
4. System establishes connection
5. System displays directory tree
6. User browses directories OR switches to tag view OR searches
7. User selects images to view
```

---

## 3. Page Structure & Navigation

### 3.1 App Shell Architecture

```
┌─────────────────────────────────────────────────┐
│              App Shell (Persistent)              │
│  ┌────────────────────────────────────────┐     │
│  │  Header / Navigation Bar               │     │
│  │  - Logo/App Name                       │     │
│  │  - View Selector: [Dir Tree|Tags|Search]│    │
│  │  - Settings Icon                       │     │
│  └────────────────────────────────────────┘     │
│                                                  │
│  ┌────────────────────────────────────────┐     │
│  │         Main Content Area              │     │
│  │         (Dynamic Pages)                │     │
│  │                                         │     │
│  │  - Dashboard (Landing)                 │     │
│  │  - Directory Tree View                 │     │
│  │  - Tag View                            │     │
│  │  - Search Results View                 │     │
│  │  - Image Detail Page                   │     │
│  │  - Settings Page                       │     │
│  └────────────────────────────────────────┘     │
└─────────────────────────────────────────────────┘
```

---

## 4. Page Definitions

### 4.1 LANDING PAGE: Dashboard (`/`)

**Purpose:** Connect to remote drives and view system statistics

**Layout:**
```
┌──────────────────────────────────────────────────────────┐
│  Picture Model App                          [⚙ Settings] │
├──────────────────────────────────────────────────────────┤
│                                                           │
│  ┌─────────────────────────────────────────────────┐    │
│  │  Statistics Overview                            │    │
│  │  ────────────────────────────────────────────   │    │
│  │  Total Images: 10,234    |  Total Drives: 3    │    │
│  │  Total Size: 45.3 GB     |  Tagged: 8,945      │    │
│  └─────────────────────────────────────────────────┘    │
│                                                           │
│  Remote File Drives                                       │
│  ─────────────────────────────────────────────────────   │
│                                                           │
│  ┌───────────────────────────────────────────────────┐  │
│  │  💾 Home NAS - /media/photos                      │  │
│  │  Status: ● CONNECTED                              │  │
│  │  Images: 5,234  |  Last Crawled: 2 hours ago     │  │
│  │  [Browse Tree] [View by Tags] [Disconnect]       │  │
│  └───────────────────────────────────────────────────┘  │
│                                                           │
│  ┌───────────────────────────────────────────────────┐  │
│  │  💻 Local Pictures - /Users/user/Pictures         │  │
│  │  Status: ○ DISCONNECTED                           │  │
│  │  Images: 3,500  |  Last Crawled: 1 day ago        │  │
│  │  [Connect] [Edit] [Delete]                        │  │
│  └───────────────────────────────────────────────────┘  │
│                                                           │
│  ┌───────────────────────────────────────────────────┐  │
│  │  ☁️ External Drive 1 - /Volumes/Photos            │  │
│  │  Status: ○ DISCONNECTED                           │  │
│  │  Images: 1,500  |  Last Crawled: 3 days ago       │  │
│  │  [Connect] [Edit] [Delete]                        │  │
│  └───────────────────────────────────────────────────┘  │
│                                                           │
│  [+ Add New Remote Drive]                                 │
│                                                           │
│  Quick Actions                                            │
│  ─────────────────────────────────────────────────────   │
│  [Start Full Crawl]  [Manage Tags]  [View All Images]    │
│                                                           │
│  Recent Activity                                          │
│  ─────────────────────────────────────────────────────   │
│  • Crawl completed on Home NAS (2 hours ago)             │
│  • 45 new images added to Local Pictures                 │
│  • Tag "vacation" created (Yesterday)                    │
│                                                           │
└──────────────────────────────────────────────────────────┘
```

**Features:**
- Statistics cards (total images, drives, size, tagged images)
- List of all configured remote drives
- Drive status indicators (connected/disconnected)
- Quick actions for each drive
- Recent activity log
- Add new drive button
- Quick navigation to different views

**Key Actions:**
- Connect to a remote drive
- Browse directory tree of a connected drive
- View images by tags across all drives
- Add new remote drive
- Edit/delete drive configurations
- Start crawl jobs

---

### 4.2 Add/Edit Remote Drive Modal

**Purpose:** Configure new or existing remote drives

**Layout:**
```
┌──────────────────────────────────────────────────┐
│  Add New Remote Drive                         [×]│
├──────────────────────────────────────────────────┤
│                                                   │
│  Drive Name: [____________________________]       │
│               (e.g., "Home NAS", "External 1")    │
│                                                   │
│  Drive Type:                                      │
│  ● Local File System                              │
│  ○ Network Share (SMB/CIFS)                       │
│  ○ SFTP                                           │
│  ○ FTP                                            │
│                                                   │
│  Path/URL: [____________________________]         │
│             (e.g., /Users/user/Pictures)          │
│                                                   │
│  [Advanced Options ▼]                             │
│                                                   │
│  ┌─ Advanced ─────────────────────────────────┐  │
│  │                                             │  │
│  │ ☑ Auto-connect on startup                  │  │
│  │ ☑ Auto-crawl when connected                │  │
│  │                                             │  │
│  │ Credentials (if required):                 │  │
│  │ Username: [______________]                 │  │
│  │ Password: [______________]                 │  │
│  │                                             │  │
│  └─────────────────────────────────────────────┘  │
│                                                   │
│  [Test Connection]                                │
│                                                   │
│              [Cancel]  [Save & Connect]           │
└──────────────────────────────────────────────────┘
```

---

### 4.3 Directory Tree View (`/tree/:driveId`)

**Purpose:** Browse images organized by directory structure

**Layout:**
```
┌──────────────────────────────────────────────────────────┐
│  [← Dashboard]  Home NAS - /media/photos  [⚙ Settings]   │
│  View: [● Directory Tree] [○ Tags] [○ Search]            │
├──────────────────────────────────────────────────────────┤
│  Directory Tree        │  Image Grid (250x250 thumbnails)│
│  ┌──────────────────┐  │  ┌───┐ ┌───┐ ┌───┐ ┌───┐       │
│  │ ▼ 📁 /media/     │  │  │img│ │img│ │img│ │img│       │
│  │   ▼ 📁 photos/   │  │  └───┘ └───┘ └───┘ └───┘       │
│  │     ▶ 📁 2024/   │  │  vacation1.jpg  beach.jpg       │
│  │     ▼ 📁 2025/   │  │                                  │
│  │       ► 📁 Jan/  │  │  ┌───┐ ┌───┐ ┌───┐ ┌───┐       │
│  │       ▼ 📁 Feb/  │  │  │img│ │img│ │img│ │img│       │
│  │         📷 img1  │  │  └───┘ └───┘ └───┘ └───┘       │
│  │         📷 img2  │  │  sunset.jpg  family.jpg          │
│  │         📷 img3  │  │                                  │
│  │       ► 📁 Mar/  │  │  Path: /media/photos/2025/Feb   │
│  │     ► 📁 Family/ │  │  Images: 24                      │
│  │     ► 📁 Travel/ │  │                                  │
│  └──────────────────┘  │  Sort: [Date ▼] [Name] [Size]   │
│                        │                                  │
│                        │  [Select All] [Batch Tag]        │
│                        │                                  │
│                        │  [Load More...]                  │
└──────────────────────────────────────────────────────────┘
```

**Features:**
- Collapsible directory tree (left sidebar)
- Current path breadcrumbs
- Image thumbnails for selected directory
- Multi-select for batch operations
- Sorting options
- Folder metadata (image count, size)
- Keyboard navigation (arrow keys, enter to open)

**Key Actions:**
- Expand/collapse folders
- Navigate directory hierarchy
- View images in selected folder
- Select multiple images for batch tagging
- Click thumbnail to view image detail

---

### 4.4 Tag View (`/tags/:driveId?`)

**Purpose:** Browse images organized by tags across all or specific drives

**Layout:**
```
┌──────────────────────────────────────────────────────────┐
│  [← Dashboard]  All Drives                  [⚙ Settings] │
│  View: [○ Directory Tree] [● Tags] [○ Search]            │
├──────────────────────────────────────────────────────────┤
│  Tags (Sidebar)            │  Image Grid (250x250)       │
│  ┌──────────────────────┐  │                             │
│  │ All Tags (32)        │  │  Selected: [vacation] (234) │
│  │ ────────────────────  │  │  ────────────────────────  │
│  │                      │  │                             │
│  │ ☑ vacation (234)     │  │  ┌───┐ ┌───┐ ┌───┐ ┌───┐  │
│  │ ☐ family (156)       │  │  │img│ │img│ │img│ │img│  │
│  │ ☐ travel (201)       │  │  └───┘ └───┘ └───┘ └───┘  │
│  │ ☐ work (89)          │  │                             │
│  │ ☐ landscape (145)    │  │  ┌───┐ ┌───┐ ┌───┐ ┌───┐  │
│  │ ☐ portrait (78)      │  │  │img│ │img│ │img│ │img│  │
│  │ ☐ food (34)          │  │  └───┘ └───┘ └───┘ └───┘  │
│  │                      │  │                             │
│  │ [+ Create Tag]       │  │  Sort: [Recent ▼]           │
│  │ [Manage Tags]        │  │                             │
│  │                      │  │  Filter by Drive:           │
│  │ Filter by Drive:     │  │  ☑ All Drives               │
│  │ ☑ All Drives         │  │  ☐ Home NAS                 │
│  │ ☐ Home NAS           │  │  ☐ Local Pictures           │
│  │ ☐ Local Pictures     │  │                             │
│  │ ☐ External Drive 1   │  │  [Select All] [Batch Tag]   │
│  │                      │  │                             │
│  │ Sort tags:           │  │  [Load More...]             │
│  │ ● Name               │  │                             │
│  │ ○ Most Used          │  │                             │
│  │ ○ Recent             │  │                             │
│  └──────────────────────┘  │                             │
└──────────────────────────────────────────────────────────┘
```

**Features:**
- Tag list with image counts
- Multi-select tags (AND/OR filter)
- Filter by specific drive or all drives
- Create new tags
- Manage existing tags
- Sort tags by name, usage, or recency
- Image grid for selected tag(s)

**Key Actions:**
- Select one or multiple tags to filter
- View all images with selected tag(s)
- Create new tag
- Manage tag library
- Filter by drive
- Batch operations on tagged images

---

### 4.5 Search Results View (`/search?q=...`)

**Purpose:** Display search results with advanced filtering

**Layout:**
```
┌──────────────────────────────────────────────────────────┐
│  [← Dashboard]                              [⚙ Settings] │
│  View: [○ Directory Tree] [○ Tags] [● Search]            │
├──────────────────────────────────────────────────────────┤
│  Search: [paris eiffel tower___________________] [🔍]    │
│                                                           │
│  Filters (Collapsible)     │  Results (24 images)        │
│  ┌──────────────────────┐  │  ─────────────────────────  │
│  │ Drives:              │  │                             │
│  │ ☑ All Drives         │  │  ┌───┐ ┌───┐ ┌───┐ ┌───┐  │
│  │                      │  │  │img│ │img│ │img│ │img│  │
│  │ Date Range:          │  │  └───┘ └───┘ └───┘ └───┘  │
│  │ From: [2024-01-01]   │  │  paris.jpg  eiffel.jpg      │
│  │ To:   [2025-12-31]   │  │  Match: filename, location  │
│  │                      │  │                             │
│  │ File Type:           │  │  ┌───┐ ┌───┐ ┌───┐ ┌───┐  │
│  │ ☑ JPEG               │  │  │img│ │img│ │img│ │img│  │
│  │ ☑ PNG                │  │  └───┘ └───┘ └───┘ └───┘  │
│  │ ☐ RAW                │  │  tower.jpg  france.jpg      │
│  │                      │  │  Match: metadata, tags      │
│  │ Size:                │  │                             │
│  │ ☐ < 1MB              │  │  Sort: [Relevance ▼]        │
│  │ ☑ 1-5MB              │  │                             │
│  │ ☐ > 5MB              │  │  [Select All] [Batch Tag]   │
│  │                      │  │                             │
│  │ Tags:                │  │  [Load More...]             │
│  │ ☑ vacation           │  │                             │
│  │ ☐ travel             │  │                             │
│  │                      │  │                             │
│  │ [Clear Filters]      │  │                             │
│  └──────────────────────┘  │                             │
└──────────────────────────────────────────────────────────┘
```

**Features:**
- Full-text search across filename, metadata, tags
- Advanced filters (date, file type, size, tags, drive)
- Search result highlighting
- Sort by relevance, date, name, size
- "Search within results" option
- Save search queries (future)

**Key Actions:**
- Enter search query
- Apply filters
- Sort results
- View matching images
- Batch operations

---

### 4.6 Image Detail Page (`/image/:driveId/:imageId`)

**Purpose:** View full-resolution image with metadata (Right Sidebar Layout)

**Layout:**
```
┌──────────────────────────────────────────────────────────┐
│  [← Back to View]                           [⚙ Settings] │
├──────────────────────────────────────────────────────────┤
│                                          │                │
│                                          │ METADATA       │
│                                          │ ─────────────  │
│         ┌────────────────────┐          │ 📁 Drive:      │
│         │                    │          │ Home NAS       │
│         │                    │          │                │
│         │    FULL IMAGE      │          │ 📄 Filename:   │
│         │     DISPLAY        │          │ vacation.jpg   │
│         │                    │          │                │
│         │                    │          │ 📊 Size:       │
│         └────────────────────┘          │ 2.1 MB         │
│                                          │                │
│    [◄ Previous]  [Next ►]               │ 📐 Dimensions: │
│                                          │ 1920x1080      │
│    [Zoom In] [Zoom Out] [Fit]           │                │
│                                          │ 📅 Created:    │
│                                          │ 2025-06-15     │
│                                          │ 10:30 AM       │
│                                          │                │
│                                          │ 📷 Camera:     │
│                                          │ Canon EOS R5   │
│                                          │                │
│                                          │ 🏷 Tags:       │
│                                          │ [vacation]     │
│                                          │ [travel] [×]   │
│                                          │ [+ Add Tag]    │
│                                          │                │
│                                          │ ──────────────│
│                                          │ Custom Data:   │
│                                          │ ──────────────│
│                                          │ Description:   │
│                                          │ [__________ ] │
│                                          │                │
│                                          │ Location:      │
│                                          │ [__________ ] │
│                                          │                │
│                                          │ Notes:         │
│                                          │ [__________ ] │
│                                          │                │
│                                          │ [Save Changes] │
│                                          │                │
│                                          │ 📂 Path:       │
│                                          │ /media/photos/ │
│                                          │ 2025/vacation  │
│                                          │ [Copy Path]    │
└──────────────────────────────────────────────────────────┘
```

**Features:**
- Full-size image display (left panel, 70% width)
- Zoom controls (in, out, fit to screen)
- Previous/Next navigation (keyboard arrow support)
- Metadata sidebar (right panel, 30% width)
- Read-only EXIF data (camera, dimensions, dates)
- Editable custom metadata (description, location, notes)
- Tag management (add/remove tags)
- Copy file path to clipboard
- Drive source indicator
- Scroll for long metadata

**Key Actions:**
- Navigate between images
- Zoom image
- Edit custom metadata
- Add/remove tags
- Copy file path
- Save changes

---

### 4.7 Settings Page (`/settings`)

**Purpose:** Configure application preferences

**Layout:**
```
┌──────────────────────────────────────────────────────────┐
│  Settings                                                 │
├──────────────────────────────────────────────────────────┤
│  ┌─ Display ───────────────────────────────────────────┐ │
│  │                                                      │ │
│  │ Thumbnail Size:                                      │ │
│  │ ○ Small (150px)  ● Medium (250px)  ○ Large (350px) │ │
│  │                                                      │ │
│  │ Images per page: [24 ▼]                             │ │
│  │                                                      │ │
│  │ Theme:                                               │ │
│  │ ● Light  ○ Dark  ○ Auto                             │ │
│  │                                                      │ │
│  │ Default View:                                        │ │
│  │ ○ Directory Tree  ● Tags  ○ Search                 │ │
│  │                                                      │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                           │
│  ┌─ Remote Drives ─────────────────────────────────────┐ │
│  │                                                      │ │
│  │ Connection Timeout: [30 seconds ▼]                  │ │
│  │                                                      │ │
│  │ ☑ Auto-reconnect on startup                         │ │
│  │ ☑ Remember credentials (encrypted)                  │ │
│  │                                                      │ │
│  │ [Manage Remote Drives] → (goes to dashboard)        │ │
│  │                                                      │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                           │
│  ┌─ Crawler ───────────────────────────────────────────┐ │
│  │                                                      │ │
│  │ ☑ Auto-crawl on drive connection                    │ │
│  │ ☑ Extract EXIF metadata                             │ │
│  │ ☑ Generate thumbnails                               │ │
│  │                                                      │ │
│  │ Crawl Schedule:                                      │ │
│  │ ○ Manual only                                        │ │
│  │ ● Daily at [03:00 AM ▼]                             │ │
│  │ ○ Weekly on [Sunday ▼]                              │ │
│  │                                                      │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                           │
│  ┌─ Performance ───────────────────────────────────────┐ │
│  │                                                      │ │
│  │ Thumbnail quality: [High ▼]                          │ │
│  │                                                      │ │
│  │ Maximum concurrent crawls: [4 ▼]                     │ │
│  │                                                      │ │
│  │ ☑ Cache thumbnails locally                          │ │
│  │ ☑ Preload next/previous images                      │ │
│  │                                                      │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                           │
│  ┌─ Database ──────────────────────────────────────────┐ │
│  │                                                      │ │
│  │ Total Images: 10,234                                 │ │
│  │ Total Drives: 3                                      │ │
│  │ Database Size: 245 MB                                │ │
│  │                                                      │ │
│  │ [Clear Thumbnail Cache]                              │ │
│  │ [Rebuild Search Index]                               │ │
│  │ [Export Database]                                    │ │
│  │ [Import Database]                                    │ │
│  │                                                      │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                           │
│  [Save Settings]                                          │
└──────────────────────────────────────────────────────────┘
```

**Features:**
- Display preferences (thumbnail size, theme, default view)
- Remote drive settings (timeout, auto-reconnect)
- Crawler configuration (auto-crawl, schedule)
- Performance tuning
- Database maintenance tools

---

## 5. Navigation Structure

### 5.1 Routing Map

```
/                          → Dashboard (Landing Page)
/tree/:driveId             → Directory Tree View
/tags                      → Tag View (all drives)
/tags/:driveId             → Tag View (specific drive)
/search?q=term             → Search Results View
/image/:driveId/:imageId   → Image Detail Page
/settings                  → Settings Page
/drives/add                → Add Remote Drive Modal
/drives/edit/:driveId      → Edit Remote Drive Modal
```

### 5.2 View Switcher (Always Visible)

In the header bar, after connecting to a drive or viewing all drives:
```
[● Directory Tree] [○ Tags] [○ Search]
```

Clicking switches between views while maintaining context (current drive, filters, etc.)

### 5.3 User Flow Examples

**Flow 1: First-time User**
1. Land on Dashboard (empty state)
2. Click "[+ Add New Remote Drive]"
3. Configure drive (name, type, path)
4. Click "Save & Connect"
5. System starts auto-crawl (if enabled)
6. User clicks "Browse Tree" to explore

**Flow 2: Browse by Directory**
1. Dashboard → Click "Browse Tree" on a connected drive
2. Navigate directory tree (expand folders)
3. Click folder to view images in that folder
4. Click thumbnail → Image Detail page
5. Add tags, edit metadata
6. Click Next/Previous to navigate images

**Flow 3: Browse by Tags**
1. Dashboard → Click "View by Tags"
2. Select one or more tags from sidebar
3. View all images with those tags
4. Click thumbnail → Image Detail page

**Flow 4: Search Across All Drives**
1. Use global search bar or switch to Search view
2. Enter search query
3. Apply filters (drive, date, file type, tags)
4. View results
5. Click thumbnail → Image Detail page

**Flow 5: Batch Tagging**
1. Any view (tree, tags, search)
2. Enable multi-select mode
3. Select multiple images
4. Click "Batch Tag"
5. Add/remove tags for all selected images

---

## 6. UI Components Library

### 6.1 New Components (Remote Drives)

1. **DriveCard**
   - Props: drive{id, name, type, status, imageCount, lastCrawled}
   - Events: onConnect, onDisconnect, onBrowse, onEdit, onDelete
   - Features: Status indicator, action buttons

2. **DriveStatusIndicator**
   - Props: status (CONNECTED, DISCONNECTED, ERROR)
   - Visual: Colored dot (green, gray, red)

3. **DirectoryTreeNode**
   - Props: path, name, isExpanded, imageCount, depth
   - Events: onExpand, onCollapse, onClick
   - Features: Folder icon, expand/collapse arrow

4. **DirectoryTree**
   - Props: rootPath, currentPath, onPathSelect
   - Features: Recursive rendering, keyboard navigation

5. **ViewSwitcher**
   - Props: activeView (tree, tags, search)
   - Events: onViewChange
   - Visual: Tab-like selector

### 6.2 Existing Components (from v1)

6. **ImageThumbnail**
   - Props: imageUrl, fileName, date, size, selected, driveId
   - Events: onClick, onSelect, onHover

7. **ImageGrid**
   - Props: images[], columns, loading
   - Features: Responsive, infinite scroll

8. **MetadataEditor**
   - Props: metadata{}, editable
   - Events: onSave, onCancel

9. **TagPill**
   - Props: tagName, removable
   - Events: onClick, onRemove

10. **SearchBar**
    - Props: placeholder, value
    - Events: onSearch, onChange
    - Features: Autocomplete, recent searches

11. **FilterPanel**
    - Props: filters{}, activeFilters
    - Events: onFilterChange

---

## 7. Empty States

### Dashboard (No Drives)
```
┌──────────────────────────────────────┐
│         💾                           │
│                                      │
│    No remote drives configured       │
│                                      │
│    Add a remote drive to start       │
│    browsing your images.             │
│                                      │
│    [+ Add Remote Drive]              │
└──────────────────────────────────────┘
```

### Directory Tree (No Images)
```
┌──────────────────────────────────────┐
│         📁                           │
│                                      │
│    No images in this directory       │
│                                      │
│    Try selecting a different folder  │
│    or start a crawl to index images. │
│                                      │
└──────────────────────────────────────┘
```

### Tag View (No Tags)
```
┌──────────────────────────────────────┐
│         🏷                           │
│                                      │
│    No tags created yet               │
│                                      │
│    Add tags to organize your images. │
│                                      │
│    [+ Create First Tag]              │
└──────────────────────────────────────┘
```

---

## 8. Responsive Design

### Breakpoints
```
Mobile:      < 768px   → Stack views, hide sidebar, hamburger menu
Tablet:      768-1024  → Collapsible sidebar, 3-4 columns
Desktop:     > 1024px  → Full sidebar, 4-6 columns (medium thumbs)
Large:       > 1440px  → Spacious layout, 6-8 columns
```

### Mobile Adaptations
- Directory Tree: Drawer/modal overlay
- Tag Sidebar: Bottom sheet or drawer
- Image Grid: 2 columns on mobile
- View Switcher: Dropdown instead of tabs

---

## 9. Updated Domain Requirements

### New API Endpoints

**Remote Drive Management:**
- `POST /api/drives` - Add new remote drive
- `GET /api/drives` - List all drives
- `GET /api/drives/{id}` - Get drive details
- `PUT /api/drives/{id}` - Update drive config
- `DELETE /api/drives/{id}` - Remove drive
- `POST /api/drives/{id}/connect` - Connect to drive
- `POST /api/drives/{id}/disconnect` - Disconnect from drive
- `GET /api/drives/{id}/status` - Get connection status

**Directory Tree:**
- `GET /api/drives/{id}/tree` - Get full directory tree
- `GET /api/drives/{id}/tree?path=/some/path` - Get subtree
- `GET /api/drives/{id}/images?path=/some/path&sort=date,desc` - Get images in path (sort supports date|name|size with optional direction)

**Cross-Drive Operations:**
- `GET /api/images?driveId={id}` - Filter images by drive
- `GET /api/tags?driveId={id}` - Filter tags by drive
- `GET /api/search?driveId={id}&q=term` - Search within drive

---

## 10. Technology Stack Updates

### Backend Additions
- **File System Abstraction Layer:**
  - Interface: `FileSystemProvider`
  - Implementations: `LocalFileSystem`, `SmbFileSystem`, `SftpFileSystem`
  - Library: Apache Commons VFS or SSHJ for SFTP

- **Connection Management:**
  - Connection pooling for network drives
  - Credential encryption (AES-256)
  - Health checks and reconnection logic

### Frontend Considerations
- **Tree Component:** Use existing libraries like:
  - React: `react-virtualized-tree`, `rc-tree`
  - Vue: `vue-jstree`, `vue-tree`
- **State Management:** Consider Redux/Vuex for complex drive/view state
- **WebSocket:** For real-time crawl progress and drive status updates

---

## 11. Implementation Priority

### Phase 1: Core Dashboard & Local Drives
1. Dashboard landing page
2. Add local drive (file system only)
3. Basic connection management
4. Drive list and status display

### Phase 2: Directory Tree View
1. Directory tree component
2. Tree navigation and expansion
3. Image grid for selected folder
4. Basic metadata display

### Phase 3: Tag & Search Views
1. Tag view with filtering
2. Search functionality
3. View switcher
4. Cross-view navigation

### Phase 4: Image Detail & Metadata
1. Image detail page (right sidebar layout)
2. Metadata editor
3. Tag management
4. Previous/Next navigation

### Phase 5: Network Drives
1. SMB/CIFS support
2. SFTP support
3. Credential management
4. Advanced connection options

### Phase 6: Polish & Optimization
1. Light theme styling
2. Responsive design
3. Performance optimization
4. Error handling and edge cases

---

## 12. Key Decisions Summary

✅ **Landing Page:** Dashboard-First
- Statistics overview
- Remote drive management
- Quick actions

✅ **Primary Views:**
- Directory Tree (hierarchical browsing)
- Tags (tag-based organization)
- Search (query-based discovery)

✅ **Image Detail:** Right Sidebar Layout
- Image left (70%), Metadata right (30%)

✅ **Thumbnail Size:** Medium (250x250px, 4-6 per row)

✅ **Color Theme:** Light mode

✅ **Remote Drives:** Multi-drive support
- Local file system (MVP)
- Network shares (SMB, SFTP) - Phase 5
- Cloud storage - Future

---

## 13. Next Steps

1. **Review and Approve** this design
2. **Update Domain Design** document with Remote Drive entities
3. **Create Database Schema** for drives and updated relationships
4. **Set up Backend Project** (Spring Boot + domain entities)
5. **Choose Frontend Framework** (React/Vue/Vanilla)
6. **Begin Phase 1 Implementation**

Would you like me to proceed with updating the Domain Design document to include the Remote Drive architecture?
