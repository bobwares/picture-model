Creator: Codex

# Picture Model App — Web App Actions (Phase 3)

Date: January 27, 2026

## 1. Purpose
Define the actions required to implement web app behavior for the multi‑device, same‑Wi‑Fi setup. Actions are grouped by page and mapped to API interactions, UI states, and expected side effects.

## 2. Global Actions

### 2.1 Connect to Server
- **Trigger**: User enters server address or selects discovered server.
- **UI**: Validate input → show connecting state → success or error.
- **API**: `GET /api/health` or `GET /api/status` (verify).
- **State**: Store server base URL locally.
- **Side effects**: Redirect to Library Dashboard.

### 2.2 Global Search
- **Trigger**: User submits search in top bar.
- **API**: `GET /api/images?query=...` or `GET /api/search`.
- **UI**: Route to Gallery with query applied.
- **State**: Persist recent searches (local).

### 2.3 Fetch App Status
- **Trigger**: App shell loads or reconnects.
- **API**: `GET /api/status`.
- **UI**: Display server availability + last crawl.

## 3. Landing / Connect Actions

### 3.1 Discover Servers (optional)
- **Trigger**: User clicks “Discover servers”.
- **API**: Network discovery (mDNS / broadcast) if supported.
- **UI**: Show list of found servers.

### 3.2 Connect
- **Trigger**: User submits address.
- **API**: `GET /api/health`.
- **UI**: Success → dashboard; failure → error with retry.

## 4. Library Dashboard Actions

### 4.1 Load Libraries
- **Trigger**: Page load.
- **API**: `GET /api/libraries`.
- **UI**: Render library cards and stats.

### 4.2 Add Library
- **Trigger**: User adds folder.
- **API**: `POST /api/libraries`.
- **UI**: Show new library card.

### 4.3 Remove Library
- **Trigger**: User deletes library.
- **API**: `DELETE /api/libraries/{id}`.
- **UI**: Remove card, confirm prompt.

### 4.4 Start Crawl
- **Trigger**: Start crawl from library card.
- **API**: `POST /api/crawls`.
- **UI**: Navigate to Crawl Status.

## 5. Gallery / Search Actions

### 5.1 Load Gallery
- **Trigger**: Page load or query change.
- **API**: `GET /api/images` with filters.
- **UI**: Render thumbnails + pagination state.

### 5.2 Apply Filters
- **Trigger**: User adjusts filter controls.
- **API**: `GET /api/images` with filter params.
- **UI**: Update grid and filter chips.

### 5.3 Sort
- **Trigger**: User changes sort order.
- **API**: `GET /api/images?sort=...`.
- **UI**: Reorder gallery results.

### 5.4 Open Image
- **Trigger**: Click thumbnail.
- **API**: `GET /api/images/{id}`.
- **UI**: Navigate to Image Detail.

## 6. Image Detail Actions

### 6.1 Load Image Detail
- **Trigger**: Page load.
- **API**: `GET /api/images/{id}`.
- **UI**: Render image viewer and metadata.

### 6.2 Fetch Image File
- **Trigger**: Viewer requests full image.
- **API**: `GET /api/images/{id}/file` or `/files/{id}`.
- **UI**: Display image.

### 6.3 Update Metadata
- **Trigger**: Save metadata edits.
- **API**: `PUT /api/images/{id}/metadata`.
- **UI**: Show success toast + updated fields.

### 6.4 Add/Remove Tags
- **Trigger**: Tag add/remove.
- **API**: `POST /api/images/{id}/tags`, `DELETE /api/images/{id}/tags/{tagId}`.
- **UI**: Update tag list.

### 6.5 Next/Previous Navigation
- **Trigger**: User clicks next/prev.
- **API**: optional prefetch `GET /api/images?cursor=...`.
- **UI**: Swap to next image.

## 7. Crawl Status & History Actions

### 7.1 Load Crawl Status
- **Trigger**: Page load.
- **API**: `GET /api/crawls/{id}` (active crawl), `GET /api/crawls` (history).
- **UI**: Progress bar and stats.

### 7.2 Cancel Crawl
- **Trigger**: User clicks cancel.
- **API**: `POST /api/crawls/{id}/cancel`.
- **UI**: Show cancelled state.

### 7.3 View Crawl Errors
- **Trigger**: User opens errors panel.
- **API**: `GET /api/crawls/{id}/errors`.
- **UI**: Render error list.

## 8. Settings Actions

### 8.1 Load Settings
- **Trigger**: Page load.
- **API**: `GET /api/settings`.
- **UI**: Populate forms.

### 8.2 Update Settings
- **Trigger**: User saves.
- **API**: `PUT /api/settings`.
- **UI**: Show success toast.

### 8.3 Clear Cache
- **Trigger**: User clicks clear cache.
- **API**: `POST /api/cache/clear`.
- **UI**: Confirm + toast.

## 9. Error Handling Actions
- **Network failure**: show offline banner, allow retry.
- **Unauthorized**: show re‑connect prompt.
- **Server busy**: show queued state.

## 10. Open Decisions
- Polling vs server‑sent events for crawl progress.
- Pagination vs infinite scroll for gallery.
- Auth model (none vs PIN).
