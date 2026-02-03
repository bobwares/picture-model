Creator: Codex

# Picture Model App — Web App Design

Date: January 27, 2026

## 1. Overview
The Picture Model App web UI is a single‑page application that connects to a local server on the same Wi‑Fi. The server exposes REST endpoints, serves image files, and persists metadata in a database.

Primary goals:
- Fast browsing of large image libraries.
- Powerful search and filters across filename + metadata.
- Simple metadata editing with immediate persistence.
- Multi‑device access on the same network.

## 2. Page Map

Primary pages (MVP):
- **Landing / Connect** (`/`)
- **Library Dashboard** (`/library`)
- **Gallery / Search** (`/gallery`)
- **Image Detail** (`/image/:id`)
- **Crawl Status & History** (`/crawl`)
- **Settings** (`/settings`)

Secondary pages (post‑MVP):
- **Tags Management** (`/tags`)
- **System Health** (`/system`)

## 3. Landing / Connect Page

**Goal:** Help any device on the Wi‑Fi connect to the server quickly.

Sections:
- **Hero**: “Your image library, anywhere on your Wi‑Fi.”
- **Primary CTA**: “Connect to Server” (manual entry + discovery)
- **Secondary CTA**: “Learn how it works”
- **How it works** strip: Server indexes → Browser connects → Search & edit
- **Status teaser** (if reachable): last crawl time, images indexed
- **Footer**: local‑network note + supported formats

## 4. Navigation Model

Persistent top bar:
- App name (left) → Gallery
- Global search (center)
- Icons: Library, Crawl, Settings (right)

Routing map:
- `/` → Landing / Connect
- `/library` → Library Dashboard
- `/gallery` → Gallery + Search
- `/image/:id` → Image Detail
- `/crawl` → Crawl Status & History
- `/settings` → Settings
- `/tags` → Tags (optional)

## 5. Page Details

### 5.1 Library Dashboard
- Library cards (root folders)
- Last crawl time, image counts
- Quick actions: Start crawl, View gallery
- Storage summary (optional)

### 5.2 Gallery / Search
- Global search bar
- Filters: date range, tags, file type, size
- Sort: date, name, size
- Pagination or infinite scroll
- Empty state with “Start Crawl” CTA

### 5.3 Image Detail
- Large image viewer (zoom / fit)
- Metadata editor (title, description, tags, EXIF)
- Next/Previous navigation
- Copy path / open in file system (optional)

### 5.4 Crawl Status & History
- Active crawl progress bar
- Crawl history list with stats
- Error list (non‑blocking)
- Start new crawl with options

### 5.5 Settings
- Libraries: add/remove root folders
- Crawl options: incremental, file types
- Performance: thumbnail size, cache
- Network: server address management

### 5.6 Tags (post‑MVP)
- Create/edit/delete tags
- Usage counts
- Click to filter gallery by tag

## 6. Key Workflows

### 6.1 First‑time connection
1. User opens web app on device.
2. App prompts for server address or discovery.
3. App verifies connection and stores it locally.
4. User lands on Library Dashboard.

### 6.2 Crawl and index
1. User starts a crawl from Library Dashboard or Crawl page.
2. Progress updates in real time.
3. Results appear in Gallery.

### 6.3 Search and view
1. User searches by filename or metadata.
2. Gallery shows results with filters.
3. User opens Image Detail to view and edit metadata.

### 6.4 Metadata edit
1. User edits metadata fields and tags.
2. Changes persist to server.
3. Updates are immediately searchable.

## 7. UI Components
- **SearchBar** (global)
- **ImageGrid** (responsive)
- **ImageThumbnail**
- **FilterSidebar** (collapsible)
- **MetadataEditor**
- **TagPill**
- **ProgressBar** (crawl status)
- **Toast / Notification** (save success/errors)

## 8. Responsive Behavior
- **Mobile**: single column, drawer navigation
- **Tablet**: 2–4 columns
- **Desktop**: 4–8 columns

## 9. MVP vs Later

MVP:
- Connect page
- Library dashboard
- Gallery + search
- Image detail + metadata edit
- Crawl status
- Settings (basic)

Later:
- Tags management
- Bulk edits
- Lightbox quick view
- System health
- Keyboard shortcuts

## 10. Open Decisions
- Server discovery: broadcast vs manual entry.
- Authentication: local only vs simple PIN.
- Thumbnail storage: file cache vs on‑demand.
- Search: full‑text index or DB‑level filtering.
