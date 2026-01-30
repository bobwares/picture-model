---
**Created by:** Claude (AI Coding Agent)
**Date:** 2026-01-27
**Version:** 2.0
---

# Picture Model App - Web Application Design

## 1. Application Overview

The web app is a single-page application (SPA) that provides an intuitive interface for browsing, searching, and managing image collections indexed from the local hard drive.

## 2. Page Structure & Navigation

### 2.1 Recommended Page Architecture


```
┌─────────────────────────────────────────────────┐
│              App Shell (Persistent)              │
│  ┌────────────────────────────────────────┐     │
│  │  Header / Navigation Bar               │     │
│  │  - Logo/App Name                       │     │
│  │  - Search Bar (global)                 │     │
│  │  - Settings Icon                       │     │
│  └────────────────────────────────────────┘     │
│                                                  │
│  ┌────────────────────────────────────────┐     │
│  │         Main Content Area              │     │
│  │         (Dynamic Pages)                │     │
│  │                                         │     │
│  │  - Gallery Page (Landing)              │     │
│  │  - Image Detail Page                   │     │
│  │  - Search Results Page                 │     │
│  │  - Crawler Management Page             │     │
│  │  - Tags Management Page                │     │
│  │  - Settings Page                       │     │
│  └────────────────────────────────────────┘     │
└─────────────────────────────────────────────────┘
```

### 2.2 Page Definitions

#### **LANDING PAGE: Gallery View** (`/` or `/gallery`)

**Purpose:** Primary interface for browsing all indexed images

**Layout:**
```
┌──────────────────────────────────────────────────────┐
│  Header: [Logo] [Search: ____________] [⚙ Settings]  │
├──────────────────────────────────────────────────────┤
│  Filters Sidebar (Collapsible)    │  Image Grid      │
│  ┌─────────────────────────┐      │  ┌───┐ ┌───┐    │
│  │ □ All Images            │      │  │img│ │img│    │
│  │ □ Recent (Last 30 days) │      │  └───┘ └───┘    │
│  │ □ This Year             │      │  ┌───┐ ┌───┐    │
│  │                         │      │  │img│ │img│    │
│  │ Tags:                   │      │  └───┘ └───┘    │
│  │ ☑ vacation              │      │                  │
│  │ ☐ family                │      │  [Load More...] │
│  │ ☐ work                  │      │                  │
│  │                         │      │                  │
│  │ Sort by:                │      │                  │
│  │ ● Date (newest)         │      │                  │
│  │ ○ Date (oldest)         │      │                  │
│  │ ○ File name             │      │                  │
│  │ ○ File size             │      │                  │
│  └─────────────────────────┘      │                  │
└──────────────────────────────────────────────────────┘
```

**Features:**
- Grid of image thumbnails (responsive: 2-8 columns based on screen size)
- Hover: Show filename, date, size
- Click: Navigate to Image Detail Page
- Infinite scroll or pagination
- Multi-select mode (checkbox overlay) for batch operations
- View options: Grid size (small/medium/large thumbnails)

**Key Actions:**
- Filter by date range, tags, file type
- Sort by various criteria
- Select multiple images for batch tagging
- Quick view (lightbox without leaving page)

---

#### **Image Detail Page** (`/image/{id}`)

**Purpose:** View full-resolution image with complete metadata

**Layout:**
```
┌──────────────────────────────────────────────────────┐
│  [← Back to Gallery]                    [⚙ Settings] │
├──────────────────────────────────────────────────────┤
│                                          │            │
│                                          │ METADATA   │
│                                          │ ────────── │
│         ┌────────────────────┐          │ Filename:  │
│         │                    │          │ vacation.  │
│         │                    │          │   jpg      │
│         │    FULL IMAGE      │          │            │
│         │     DISPLAY        │          │ Size:      │
│         │                    │          │ 2.1 MB     │
│         │                    │          │            │
│         └────────────────────┘          │ Dimensions:│
│                                          │ 1920x1080  │
│    [◄ Previous]  [Next ►]              │            │
│                                          │ Created:   │
│                                          │ 2025-06-15│
│                                          │            │
│                                          │ Camera:    │
│                                          │ Canon R5   │
│                                          │            │
│                                          │ Tags:      │
│                                          │ [vacation] │
│                                          │ [travel]   │
│                                          │ [+ Add]    │
│                                          │            │
│                                          │ Custom     │
│                                          │ Metadata:  │
│                                          │ ────────── │
│                                          │ Description│
│                                          │ [_______ ] │
│                                          │            │
│                                          │ Location:  │
│                                          │ [_______ ] │
│                                          │            │
│                                          │ [Save]     │
└──────────────────────────────────────────────────────┘
```

**Features:**
- Full-size image display (with zoom controls)
- Previous/Next navigation (keyboard arrows)
- Read-only metadata from EXIF
- Editable user metadata fields
- Tag management (add/remove)
- Copy file path button
- Download/Share options
- Delete button (marks as deleted in DB)

**Key Actions:**
- Navigate between images (← →)
- Zoom in/out/fit
- Edit custom metadata
- Add/remove tags
- Save changes

---

#### **Search Results Page** (`/search?q=...`)

**Purpose:** Display results from search queries

**Layout:**
```
┌──────────────────────────────────────────────────────┐
│  Search: [paris eiffel tower________] [🔍]           │
├──────────────────────────────────────────────────────┤
│  Showing 24 results for "paris eiffel tower"         │
│                                                       │
│  Filters: [All] [This Month] [Has Tags] [Large]     │
│                                                       │
│  ┌───┐ ┌───┐ ┌───┐ ┌───┐                            │
│  │img│ │img│ │img│ │img│  vacation.jpg               │
│  └───┘ └───┘ └───┘ └───┘  Matches: filename          │
│                                                       │
│  ┌───┐ ┌───┐ ┌───┐ ┌───┐                            │
│  │img│ │img│ │img│ │img│  paris_2024.jpg             │
│  └───┘ └───┘ └───┘ └───┘  Matches: filename, location│
│                                                       │
│  [Load More Results...]                              │
└──────────────────────────────────────────────────────┘
```

**Features:**
- Same grid layout as Gallery
- Search term highlighting/matching indicators
- Relevance sorting
- Advanced filters specific to search
- "Did you mean..." suggestions
- Search within results

**Key Actions:**
- Refine search
- Same actions as Gallery (view, select, batch operations)

---

#### **Crawler Management Page** (`/crawler`)

**Purpose:** Control and monitor image indexing process

**Layout:**
```
┌──────────────────────────────────────────────────────┐
│  Crawler Management                                   │
├──────────────────────────────────────────────────────┤
│  ┌────────────────────────────────────────────────┐  │
│  │ Start New Crawl                                │  │
│  │                                                │  │
│  │ Directory Path: [/Users/user/Pictures____]    │  │
│  │                                                │  │
│  │ ☑ Incremental (only check for changes)        │  │
│  │ ☑ Extract EXIF metadata                       │  │
│  │ ☑ Generate thumbnails                         │  │
│  │                                                │  │
│  │         [Start Crawl]                          │  │
│  └────────────────────────────────────────────────┘  │
│                                                       │
│  Current Crawl Job                                    │
│  ────────────────────────────────────────────────    │
│  Status: IN PROGRESS                                  │
│  ████████████░░░░░░░░░░  60% complete                │
│  Files processed: 6,234 / 10,000                      │
│  New images: 145                                      │
│  Updated: 23                                          │
│  Errors: 2                                            │
│  Elapsed time: 5 minutes 32 seconds                   │
│                                                       │
│  [Pause] [Cancel]                                     │
│                                                       │
│  ─────────────────────────────────────────────────   │
│  Crawl History                                        │
│  ─────────────────────────────────────────────────   │
│  ┌────────────────────────────────────────────────┐  │
│  │ 2025-01-27 10:30 AM    COMPLETED    ✓         │  │
│  │ /Users/user/Pictures                           │  │
│  │ Processed: 10,000 | Added: 145 | Updated: 23   │  │
│  └────────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────────┐  │
│  │ 2025-01-26 03:00 PM    COMPLETED    ✓         │  │
│  │ /Users/user/Pictures                           │  │
│  │ Processed: 9,850 | Added: 320 | Updated: 15    │  │
│  └────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────┘
```

**Features:**
- Directory picker/input
- Crawl configuration options
- Real-time progress indicator
- Pause/resume/cancel controls
- History of previous crawl jobs
- Error log viewer
- Scheduled crawl setup (future feature)

**Key Actions:**
- Start new crawl
- Monitor progress
- View crawl history
- Review errors

---

#### **Tags Management Page** (`/tags`)

**Purpose:** Manage all tags used in the system

**Layout:**
```
┌──────────────────────────────────────────────────────┐
│  Tags Management                                      │
├──────────────────────────────────────────────────────┤
│  [+ Create New Tag]                                   │
│                                                       │
│  All Tags (24)                                        │
│  ─────────────────────────────────────────────────   │
│  ┌──────────────────────────────────────────┐        │
│  │ [vacation] 🟠           234 images  [✎][×]│        │
│  └──────────────────────────────────────────┘        │
│  ┌──────────────────────────────────────────┐        │
│  │ [family] 🔵             156 images  [✎][×]│        │
│  └──────────────────────────────────────────┘        │
│  ┌──────────────────────────────────────────┐        │
│  │ [work] 🟢                89 images  [✎][×]│        │
│  └──────────────────────────────────────────┘        │
│  ┌──────────────────────────────────────────┐        │
│  │ [travel] 🟡             201 images  [✎][×]│        │
│  └──────────────────────────────────────────┘        │
│                                                       │
│  Sort by: [Name ▼] [Most Used] [Recently Used]       │
└──────────────────────────────────────────────────────┘

Modal: Create/Edit Tag
┌────────────────────────────────────┐
│  Create New Tag                    │
│                                    │
│  Tag Name: [______________]        │
│                                    │
│  Color: [🔴][🟠][🟡][🟢][🔵][🟣] │
│                                    │
│  [Cancel]  [Save]                  │
└────────────────────────────────────┘
```

**Features:**
- List all tags with usage count
- Create new tags
- Edit tag name and color
- Delete tags (with confirmation)
- Merge tags (future feature)
- Click tag to view all images with that tag

**Key Actions:**
- Create tag
- Edit tag properties
- Delete unused tags
- View images by tag

---

#### **Settings Page** (`/settings`)

**Purpose:** Configure application preferences

**Layout:**
```
┌──────────────────────────────────────────────────────┐
│  Settings                                             │
├──────────────────────────────────────────────────────┤
│  ┌─ Display ───────────────────────────────────────┐ │
│  │                                                  │ │
│  │ Thumbnail Size:                                  │ │
│  │ ○ Small  ● Medium  ○ Large                      │ │
│  │                                                  │ │
│  │ Images per page: [24 ▼]                         │ │
│  │                                                  │ │
│  │ Theme:                                           │ │
│  │ ○ Light  ● Dark  ○ Auto                         │ │
│  │                                                  │ │
│  └──────────────────────────────────────────────────┘ │
│                                                       │
│  ┌─ Crawler ───────────────────────────────────────┐ │
│  │                                                  │ │
│  │ Default Crawl Directory:                         │ │
│  │ [/Users/user/Pictures____________]               │ │
│  │                                                  │ │
│  │ ☑ Auto-crawl on startup                         │ │
│  │ ☑ Watch for file changes                        │ │
│  │                                                  │ │
│  └──────────────────────────────────────────────────┘ │
│                                                       │
│  ┌─ Performance ───────────────────────────────────┐ │
│  │                                                  │ │
│  │ Thumbnail quality: [High ▼]                      │ │
│  │                                                  │ │
│  │ Maximum concurrent crawls: [4 ▼]                 │ │
│  │                                                  │ │
│  │ ☑ Cache full images                             │ │
│  │                                                  │ │
│  └──────────────────────────────────────────────────┘ │
│                                                       │
│  ┌─ Database ──────────────────────────────────────┐ │
│  │                                                  │ │
│  │ Total Images: 10,234                             │ │
│  │ Database Size: 245 MB                            │ │
│  │                                                  │ │
│  │ [Clear Thumbnail Cache]                          │ │
│  │ [Rebuild Search Index]                           │ │
│  │ [Export Database]                                │ │
│  │                                                  │ │
│  └──────────────────────────────────────────────────┘ │
│                                                       │
│  [Save Settings]                                      │
└──────────────────────────────────────────────────────┘
```

**Features:**
- Display preferences
- Crawler configuration
- Performance tuning
- Database maintenance tools
- Theme selection
- Keyboard shortcuts reference

---

## 3. Navigation Structure

### 3.1 Primary Navigation (Always Visible)

**Top Navigation Bar:**
- **Logo/App Name** (left) → Click returns to Gallery
- **Global Search Bar** (center)
- **Navigation Links:**
  - Gallery (home icon)
  - Crawler (refresh icon)
  - Tags (tag icon)
- **Settings Icon** (right)

### 3.2 Routing Map

```
/ or /gallery          → Gallery View (Landing Page)
/image/:id             → Image Detail Page
/search?q=term         → Search Results Page
/crawler               → Crawler Management Page
/tags                  → Tags Management Page
/settings              → Settings Page
```

### 3.3 User Flow Examples

**Flow 1: First-time User**
1. Land on Gallery (empty state)
2. Click "Start Crawl" button in empty state
3. Navigate to Crawler page
4. Configure and start crawl
5. Return to Gallery to see results

**Flow 2: Browse and Tag**
1. Land on Gallery with images
2. Click thumbnail → Image Detail page
3. Add tags to image
4. Click Next to move through images
5. Return to Gallery

**Flow 3: Search and Organize**
1. Use global search bar
2. View Search Results page
3. Multi-select matching images
4. Batch add tags
5. Return to Gallery with filters applied

---

## 4. UI Components Library

### 4.1 Core Components

1. **ImageThumbnail**
   - Props: imageUrl, fileName, date, size, selected
   - Events: onClick, onSelect, onHover

2. **ImageGrid**
   - Props: images[], columns, loading
   - Features: Responsive, infinite scroll

3. **MetadataEditor**
   - Props: metadata{}, editable
   - Events: onSave, onCancel

4. **TagPill**
   - Props: tagName, color, removable
   - Events: onClick, onRemove

5. **SearchBar**
   - Props: placeholder, value
   - Events: onSearch, onChange
   - Features: Autocomplete, recent searches

6. **FilterSidebar**
   - Props: filters{}, activeFilters
   - Events: onFilterChange

7. **ProgressBar**
   - Props: current, total, status
   - Features: Percentage, time estimate

8. **Lightbox**
   - Props: imageUrl, metadata
   - Events: onClose, onNext, onPrevious
   - Features: Zoom, keyboard navigation

---

## 5. Responsive Design Breakpoints

```
Mobile:      < 768px   → 1-2 columns, hamburger menu
Tablet:      768-1024  → 3-4 columns, side navigation
Desktop:     > 1024px  → 4-8 columns, full navigation
Large:       > 1440px  → 6-8 columns, spacious layout
```

---

## 6. Empty States

**Gallery (No Images):**
```
┌──────────────────────────────────────┐
│         📷                           │
│                                      │
│    No images found                   │
│                                      │
│    Start by crawling a directory     │
│    to index your images.             │
│                                      │
│    [Go to Crawler]                   │
└──────────────────────────────────────┘
```

**Search (No Results):**
```
┌──────────────────────────────────────┐
│         🔍                           │
│                                      │
│    No results for "xyz"              │
│                                      │
│    Try different keywords or         │
│    check your filters.               │
│                                      │
│    [Clear Search]                    │
└──────────────────────────────────────┘
```

---

## 7. Key Features Summary

### Must-Have (MVP)
- ✅ Gallery View with thumbnail grid
- ✅ Image Detail View with metadata
- ✅ Search functionality
- ✅ Tag management (add/remove)
- ✅ Crawler management interface
- ✅ Filtering by date and tags

### Nice-to-Have (v2)
- 🔹 Lightbox quick view
- 🔹 Batch operations (multi-select)
- 🔹 Keyboard shortcuts
- 🔹 Advanced search filters
- 🔹 Sort options
- 🔹 Tag color coding

### Future Enhancements
- 🔮 Slideshows
- 🔮 Albums/Collections
- 🔮 Image editing (crop, rotate)
- 🔮 Face detection
- 🔮 Map view (geolocation)
- 🔮 Timeline view
- 🔮 Favorites/Ratings
- 🔮 Comments on images

---

## 8. Design Questions for Decision

### 8.1 Landing Page Question
**Which landing page style do you prefer?**

**Option A: Gallery-First (Recommended)**
- Immediately show image grid
- Filters in collapsible sidebar
- Best for users who browse regularly
- Minimal friction to content

**Option B: Dashboard-First**
- Statistics overview (total images, recent additions)
- Quick actions (start crawl, recent searches)
- Trending tags widget
- Better for periodic users

**Option C: Search-First**
- Large search bar as hero element
- Suggested tags below
- Recent images underneath
- Best for users who know what they want

### 8.2 Image Detail Layout
**Metadata placement preference?**

**Option A: Right Sidebar (Recommended)**
- Image on left, metadata on right
- Good for landscape images
- Vertical scroll for long metadata

**Option B: Bottom Panel**
- Image on top, metadata below
- Better for portrait images
- Horizontal layout

**Option C: Overlay**
- Metadata overlays bottom of image
- Cleaner look, more screen space for image
- Toggleable (press 'i' for info)

### 8.3 Thumbnail Size
**Default thumbnail size?**

- Small: 150x150px (8-10 per row)
- Medium: 250x250px (4-6 per row) ← Recommended
- Large: 350x350px (2-4 per row)

### 8.4 Color Scheme
**Application theme?**

- Light mode (white/gray backgrounds)
- Dark mode (black/dark gray) ← Recommended for image viewing
- Auto (follow system preference)

---

## 9. Recommendations Summary

### Landing Page
**Recommendation:** Gallery-First (Option A)
- Most intuitive for image browsing app
- Users want to see their images immediately
- Filters available but not intrusive

### Layout Priority
1. **Desktop-first design** (primary use case)
2. Responsive mobile view as secondary
3. Focus on image browsing efficiency

### MVP Pages (Phase 5)
1. **Gallery View** (Landing) - Priority 1
2. **Image Detail View** - Priority 1
3. **Crawler Management** - Priority 2
4. **Settings** - Priority 3
5. **Tags Management** - Priority 3
6. **Search Results** - Can reuse Gallery view with filters

---

## 10. Next Steps

Once you decide on the design questions above, we can:
1. Create wireframes/mockups for each page
2. Define the component hierarchy
3. Choose a frontend framework (React/Vue/Vanilla)
4. Set up the frontend project structure
5. Begin implementing the UI components

What are your preferences for the design questions in Section 8?
