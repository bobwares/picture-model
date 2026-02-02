/**
 * App: Picture Model
 * Package: docs
 * File: dashboard-elements.md
 * Version: 0.1.0
 * Turns: 24
 * Author: Claude Sonnet 4.5
 * Date: 2026-02-02T02:35:00Z
 * Exports: None
 * Description: Reference map of dashboard page elements and component responsibilities.
 */

# Dashboard Page Reference

This document names the visible UI regions and the component pieces used on the dashboard page so you can request precise edits.

## 1) Dashboard Landing Page
**Route:** `/`
**File:** `ui/app/page.tsx`

### Layout Overview
- **Global Header**: Top navigation (`Header` component).
- **Main Container**: centered max-width content area with vertical sections.
  - **Hero Section**: welcome banner with title and CTA.
  - **Drive List Section**: remote file drives management area.
  - **Stats Row**: ❌ **NOT IMPLEMENTED** (designed but missing from code).
  - **Recent Activity**: ❌ **NOT RENDERED** (component exists but not used).

---

## 2) Hero Section

### Container
- **Card**: rounded-3xl border with white/70 background, backdrop blur, shadow.
- **Decorative Element**: circular gradient blob (accent-soft) positioned top-right.
- **Animation**: `animate-rise` on card enter.

### Content
- **Dashboard Label**: uppercase tracking-widest text, muted color, reads "Dashboard".
- **Hero Title**: large font (3xl/4xl), semibold, reads "Remote drive control center".
- **Hero Description**: smaller text, muted color, explains purpose:
  - "Monitor connections, trigger crawls, and stay on top of your image library across every drive."
- **Add New Drive Button**:
  - Accent background, white text, rounded-lg
  - Label: "+ Add New Drive"
  - Action: opens `AddDriveModal`

### Responsive Behavior
- **Desktop**: flex-row layout, title left, button right.
- **Mobile**: flex-col layout, button below title.

---

## 3) Drive List Section

### Section Header
- **Title**: "Remote File Drives", semibold, large text.
- **Subtitle**: "Connect, browse, and manage your active storage locations.", muted text.

### Loading State
- **Container**: rounded-2xl card with border, white/70 background, centered content.
- **Spinner**: animated circular border spinner (accent color on top).
- **Message**: "Loading drives..." in muted text.

### Empty State (No Drives)
- **Container**: rounded-2xl card with dashed border, centered content.
- **Icon**: `HardDrive` icon (12×12), muted color.
- **Title**: "No drives configured", semibold.
- **Message**: "Add a remote drive to start indexing your images."
- **CTA Button**: "+ Add Your First Drive", opens modal.

### Drives Present State
- **Container**: vertical space-y-4 stack.
- **Drive Cards**: maps over drives array, renders `DriveCard` for each.

---

## 4) Drive Card Component
**File:** `ui/components/drive-card.tsx`

### Card Container
- **Layout**: rounded-2xl border, white/75 background, backdrop blur, padding, shadow.
- **Hover Effect**: translates up slightly, increases shadow.
- **Top Accent**: gradient line (transparent → accent → transparent), opacity 0→100 on hover.

### Main Content Area
Two-column layout: Drive Info (flex-1) + Action Buttons (right-aligned).

#### Drive Icon Section
- **Icon Container**: rounded-xl with surface-muted background.
- **Icon**: `HardDrive` component (5×5), muted color.

#### Drive Info Section
- **Drive Name**: large semibold text, truncated if long.
- **Status Badge**: inline pill with icon + text:
  - **CONNECTED**: emerald-700 text, emerald-50 bg, emerald-200 border, `Wifi` icon.
  - **CONNECTING**: amber-700 text, amber-50 bg, amber-200 border, `Wifi` icon (pulsing).
  - **ERROR**: rose-700 text, rose-50 bg, rose-200 border, `AlertCircle` icon.
  - **DISCONNECTED**: slate-600 text, slate-50 bg, slate-200 border, `WifiOff` icon.
- **Connection URL**: truncated muted text, shows connection string.
- **Metadata Row**: flex wrap with muted text:
  - **Image Count**: "Images: {count}" (localized number).
  - **Last Crawled**: "Last Crawled: {relative time}" (if available).

#### Latest Crawl Section
Embedded panel with rounded border, white/70 background.

- **Section Header**:
  - **Label**: "Latest Crawl" (left, semibold).
  - **View History Link**: "View crawl history" (right, accent color, hover underline).
    - Action: opens `CrawlHistoryModal`.

- **No Jobs State**:
  - Message: "No crawls yet." in muted text.

- **Job Present State**:
  - **Status**: "Status: {status}" (replaces underscores with spaces).
  - **Timestamps**: "Started {date} • Elapsed {duration}".
  - **Files Processed**: "{count} files processed".

- **IN_PROGRESS State** (additional):
  - **Progress Bar**: rounded-full with accent background, animated pulse.
  - **Percentage**: "{progress}% complete" in accent ink.
  - **Current Path**: "Processing {path}" (truncated, if available).

### Action Buttons

#### When CONNECTED
- **Browse Tree Button**: accent-soft background, accent-ink text, `FolderTree` icon.
  - Action: navigates to `/tree/{driveId}`.
- **Start Crawl Button**: accent background, white text, `Play` icon.
  - Action: triggers `crawlerApi.startCrawl()`.
  - Disabled state: shows "Starting..." when pending.
- **Edit Button**: surface-muted background, text color.
  - Action: opens `EditDriveModal`.
- **Disconnect Button**: surface-muted background, muted text.
  - Action: triggers `driveApi.disconnect()`.
  - Disabled state: shows "Disconnecting..." when pending.

#### When DISCONNECTED
- **Connect Button**: accent background, white text.
  - Action: triggers `driveApi.connect()`, navigates to tree view on success.
  - Disabled state: shows "Connecting..." when pending or status is CONNECTING.
- **Edit Button**: same as connected state.
- **Delete Button**: rose-50 background, rose-700 text.
  - Action: shows confirmation dialog, then triggers `driveApi.delete()`.
  - Disabled state: shows "Deleting..." when pending.

### Real-Time Updates
- **Latest Job Query**: polls every 4 seconds when job status is `IN_PROGRESS` or `PENDING`.
- **Query Keys**: `['drive-latest-job', driveId]`.

---

## 5) Add Drive Modal
**File:** `ui/components/add-drive-modal.tsx`
**Trigger:** "Add New Drive" button on hero or empty state.

### Modal Structure
- **Backdrop**: fixed inset-0, slate-900/40 with backdrop blur, closes on click.
- **Modal Panel**: centered, max-w-2xl, rounded-2xl, white/90 background, shadow-xl.

### Header
- **Title**: "Add New Drive", large semibold.
- **Subtitle**: "Connect a local or network drive to start indexing.", muted text.
- **Close Button**: rounded-full with X icon, top-right.

### Form Fields

#### Drive Name (required)
- **Label**: "Drive Name *"
- **Input**: text field, placeholder "My Pictures Drive".
- **Validation**: min 1 char, max 200 chars.

#### Drive Type (required)
- **Label**: "Drive Type *"
- **Input**: select dropdown with options:
  - Local Filesystem
  - SMB/CIFS Network Share
  - SFTP (SSH)
  - FTP
- **Validation**: required enum.

#### Connection URL (required)
- **Label**: "Connection URL *"
- **Input**: text field with dynamic placeholder based on drive type:
  - LOCAL: "/path/to/directory"
  - SMB: "smb://server/share"
  - SFTP: "sftp://server.com"
  - FTP: "ftp://server.com"
- **Validation**: min 1 char, max 500 chars.

#### Root Path (optional)
- **Label**: "Root Path"
- **Input**: text field, placeholder "/", default "/".
- **Validation**: max 500 chars.

#### Credentials (conditional)
- **Label**: "Credentials (JSON)"
- **Input**: textarea (3 rows), monospace font, placeholder `{"username": "user", "password": "pass"}`.
- **Visibility**: shown only when drive type is SMB, SFTP, or FTP (hidden for LOCAL).
- **Validation**: must be valid JSON if provided.
- **Help Text**: "Optional: JSON object with authentication credentials".

#### Options (checkboxes)
- **Auto-connect on startup**: checkbox, default false.
- **Auto-crawl after connection**: checkbox, default false.
- **Hover Effect**: border and background change on hover.

### Error Display
- **Banner**: rounded-lg, rose-50 background, rose-700 text, rose-200 border.
- **Content**: displays API error message or validation errors.

### Actions
- **Cancel Button**: surface-muted background, closes modal.
- **Create Drive Button**: accent background, white text.
  - Disabled state: shows "Creating..." when pending, opacity-60.
  - Action: validates, then triggers `driveApi.create()`.

### Form Validation
- Uses `react-hook-form` with `zod` schema resolver.
- Shows field-level errors below each input.

---

## 6) Edit Drive Modal
**File:** `ui/components/edit-drive-modal.tsx`
**Trigger:** "Edit" button on DriveCard.

### Differences from Add Drive Modal
- **Title**: "Edit Drive" instead of "Add New Drive".
- **Subtitle**: "Update the connection settings for this remote drive."
- **Drive Type Field**: disabled/read-only, includes help text "Drive type cannot be changed after creation."
- **Credentials Field**: starts empty, leaving blank preserves existing credentials.
- **Submit Button**: "Save Changes" instead of "Create Drive", shows "Saving..." when pending.
- **Portal Rendering**: uses `createPortal(modal, document.body)` for better z-index handling.
- **Form Reset**: resets to drive data when modal opens.

---

## 7) Crawl History Modal
**File:** `ui/components/crawl-history-modal.tsx`
**Trigger:** "View crawl history" link on DriveCard.

### Modal Structure
- **Backdrop**: fixed inset-0, slate-900/40 with backdrop blur.
- **Modal Panel**: centered, max-w-3xl, rounded-2xl, white/90 background, **max-h-[90vh]**.

### Header
- **Title**: "Crawl History", large semibold.
- **Subtitle**: "Drive: {driveName}", muted text.
- **Close Button**: rounded-full with X icon, top-right.

### Confirmation Banner (conditional)
- **Container**: rounded-xl, amber-50 background, amber-800 text, amber-200 border.
- **Title**: "Clear crawl history?"
- **Message**: "This will remove all crawl records for \"{driveName}\"."
- **Actions**:
  - **Confirm Clear Button**: rose-600 background, white text, shows "Clearing..." when pending.
  - **Cancel Button**: white background, closes confirmation.

### Job List
Scrollable area with **max-h-[520px]**, space-y-3 gap.

#### Loading State
- **Card**: white/70 background, centered text "Loading crawl history...".

#### Empty State
- **Card**: white/70 background, centered text "No crawl history yet.".

#### Job Card
- **Container**: rounded-xl, white/70 background, border, padding, **min-h-[92px]**.
- **Status Row**:
  - **Icon**: colored by status (green check, red X, pulsing blue, clock).
  - **Status Label**: "{STATUS}" (replaces underscores with spaces).
  - **Timestamps**: "Started {date} • Elapsed {duration}".
  - **Metadata**: "{filesProcessed} files processed", "Incremental crawl" if applicable.

#### IN_PROGRESS Job (additional)
- **Progress Bar**: rounded-full, accent background, animated pulse.
- **Percentage**: "{progress}% complete" in accent ink.

#### FAILED Job (additional)
- **Error Banner**: rounded-lg, rose-50 background, rose-700 text, rose-200 border.
- **Errors**: truncated error messages (parsed from JSON array or plain string).

### Footer Actions
- **Clear History Button**: rose-50 background, rose-700 text, shows "Clearing..." when pending.
  - Action: sets confirmation state to true.
- **Close Button**: surface-muted background.

### Real-Time Updates
- **Job Query**: polls every 4 seconds when any job status is `IN_PROGRESS` or `PENDING`.
- **Query Keys**: `['drive-crawl-history', driveId]`.
- **Page Size**: fetches last 20 jobs.

---

## 8) Missing Components (Designed but Not Implemented)

### Stats Row
**Design Location:** Between hero and drive list.
**Expected Content:**
- **Total Images Card**: count of all indexed images.
- **Total Drives Card**: number of configured drives.
- **Total Tags Card**: number of tags in system.
- **Active Crawls Card**: number of running crawl jobs.

**Component Type:** `StatsCard` (not created).
**Data Source:** `systemApi.getStatus()` (API exists, UI missing).

### Recent Activity Section
**File:** `ui/components/recent-activity.tsx` (EXISTS but not rendered).
**Expected Location:** Below drive list or in sidebar.
**Content:**
- Last 5 crawl jobs across all drives.
- Status icons, drive names, file counts, timestamps.
- Progress bars for active jobs.
- Error messages for failed jobs.

**Query Key:** `['recent-jobs']`.
**Data Source:** `crawlerApi.listJobs(0, 5)`.

---

## 9) Quick Reference Names (for edit requests)

### Page Regions
- **Global Header**
- **Hero Section**
- **Dashboard Label**
- **Hero Title**
- **Hero Description**
- **Add New Drive Button**
- **Decorative Blob**
- **Drive List Section**
- **Section Header**
- **Section Subtitle**

### States
- **Loading State**
- **Empty State** (no drives)
- **Drives Present State**

### Drive Card Elements
- **Drive Card**
- **Card Container**
- **Top Accent Line**
- **Drive Icon Container**
- **Drive Icon**
- **Drive Name**
- **Status Badge**
- **Connection URL**
- **Metadata Row**
- **Image Count**
- **Last Crawled**
- **Latest Crawl Section**
- **Section Header**
- **View History Link**
- **Status Label**
- **Timestamps**
- **Files Processed**
- **Progress Bar**
- **Percentage Display**
- **Current Path**
- **Action Buttons**
- **Browse Tree Button**
- **Start Crawl Button**
- **Edit Button**
- **Disconnect Button**
- **Connect Button**
- **Delete Button**

### Modal Elements
- **Modal Backdrop**
- **Modal Panel**
- **Modal Header**
- **Modal Title**
- **Modal Subtitle**
- **Close Button**
- **Error Banner**
- **Form Fields**
- **Drive Name Field**
- **Drive Type Field**
- **Connection URL Field**
- **Root Path Field**
- **Credentials Field**
- **Auto-connect Checkbox**
- **Auto-crawl Checkbox**
- **Cancel Button**
- **Create Drive Button** (Add Modal)
- **Save Changes Button** (Edit Modal)

### Crawl History Modal Elements
- **Confirmation Banner**
- **Confirm Clear Button**
- **Job List**
- **Job Card**
- **Status Icon**
- **Status Label**
- **Error Banner** (in job card)
- **Clear History Button**

### Component References
- **Header** (`ui/components/header.tsx`)
- **DriveCard** (`ui/components/drive-card.tsx`)
- **AddDriveModal** (`ui/components/add-drive-modal.tsx`)
- **EditDriveModal** (`ui/components/edit-drive-modal.tsx`)
- **CrawlHistoryModal** (`ui/components/crawl-history-modal.tsx`)
- **RecentActivity** (`ui/components/recent-activity.tsx`) - NOT RENDERED

---

## 10) Data Flow

### Drives List
```
Query: ['drives'] → driveApi.getAll()
Returns: Array of RemoteFileDrive objects
Used by: Drive list section, map to DriveCards
Invalidated by: create, update, delete, connect, disconnect mutations
```

### Latest Job per Drive
```
Query: ['drive-latest-job', driveId] → crawlerApi.listJobsByDrive(driveId, 0, 1)
Returns: Most recent CrawlJob for drive
Used by: DriveCard Latest Crawl section
Polling: Every 4s when status is IN_PROGRESS or PENDING
```

### Crawl History
```
Query: ['drive-crawl-history', driveId] → crawlerApi.listJobsByDrive(driveId, 0, 20)
Returns: Last 20 CrawlJob records for drive
Used by: CrawlHistoryModal
Polling: Every 4s when any job is IN_PROGRESS or PENDING
```

### Mutations
```
Create Drive: driveApi.create(data) → invalidates ['drives']
Update Drive: driveApi.update(driveId, data) → invalidates ['drives']
Delete Drive: driveApi.delete(driveId) → invalidates ['drives']
Connect Drive: driveApi.connect(driveId) → invalidates ['drives'], navigates to /tree/{driveId}
Disconnect Drive: driveApi.disconnect(driveId) → invalidates ['drives']
Start Crawl: crawlerApi.startCrawl({driveId}) → invalidates ['drives'], ['drive-latest-job'], ['drive-crawl-history']
Clear History: crawlerApi.clearDriveHistory(driveId) → invalidates ['drive-crawl-history'], ['drive-latest-job']
```

---

## 11) State Management

### Modal States
- **isAddDriveModalOpen**: boolean, controls Add Drive Modal visibility.
- **isEditOpen**: boolean per DriveCard, controls Edit Drive Modal visibility.
- **isHistoryOpen**: boolean per DriveCard, controls Crawl History Modal visibility.
- **isConfirmingClear**: boolean in CrawlHistoryModal, controls confirmation banner.

### Form States (react-hook-form)
- **Add/Edit Modal**: form data, validation errors, dirty state.
- **Zod Schema**: validates all fields, enforces types and constraints.

### Loading States
- **drivesLoading**: boolean, shows spinner while fetching drives.
- **createMutation.isPending**: disables submit button, shows "Creating...".
- **updateMutation.isPending**: disables submit button, shows "Saving...".
- **deleteMutation.isPending**: disables delete button, shows "Deleting...".
- **connectMutation.isPending**: disables connect button, shows "Connecting...".
- **disconnectMutation.isPending**: disables disconnect button, shows "Disconnecting...".
- **startCrawlMutation.isPending**: disables crawl button, shows "Starting...".
- **clearMutation.isPending**: disables clear button, shows "Clearing...".

---

## 12) Responsive Behavior

- **Hero Section**: flex-col on mobile, flex-row on desktop.
- **Drive Cards**: full width on all screen sizes, internal flex wrapping for action buttons.
- **Modals**: max-w-2xl (Add/Edit) or max-w-3xl (History), centered with padding.

---

## 13) Animations & Transitions

- **Hero Card**: `animate-rise` class (fade in + slide up).
- **Drive Card Hover**: `-translate-y-0.5` + shadow increase.
- **Top Accent Line**: opacity 0→100 on hover.
- **Progress Bars**: `animate-pulse` class.
- **Status Icons**: `animate-pulse` for CONNECTING and IN_PROGRESS states.
- **Backdrop**: `transition-opacity` on modal open/close.

---

## 14) API Dependencies

- `driveApi.getAll()`: Fetch all drives.
- `driveApi.create(data)`: Create new drive.
- `driveApi.update(driveId, data)`: Update drive settings.
- `driveApi.delete(driveId)`: Delete drive.
- `driveApi.connect(driveId)`: Connect to drive.
- `driveApi.disconnect(driveId)`: Disconnect from drive.
- `crawlerApi.startCrawl({driveId})`: Trigger manual crawl.
- `crawlerApi.listJobsByDrive(driveId, page, size)`: Fetch crawl jobs for drive.
- `crawlerApi.clearDriveHistory(driveId)`: Delete all crawl records.
- `systemApi.getStatus()`: (NOT USED) Would provide stats for missing Stats Row.

---

## 15) Error Handling

### API Errors
- **Display**: error banner in modals, alert dialogs on mutations.
- **Format**: `err.response?.data?.message || 'Failed to {action}'`.

### Form Validation Errors
- **Display**: below each input field, rose-700 text.
- **Source**: zod schema validation via react-hook-form.

### Confirmation Dialogs
- **Delete Drive**: `window.confirm()` before deletion.
- **Clear History**: inline confirmation banner in modal.

---

## 16) Related Files

- `ui/app/page.tsx`: Dashboard page.
- `ui/components/header.tsx`: Global navigation.
- `ui/components/drive-card.tsx`: Drive display card.
- `ui/components/add-drive-modal.tsx`: Create drive form.
- `ui/components/edit-drive-modal.tsx`: Update drive form.
- `ui/components/crawl-history-modal.tsx`: Crawl history viewer.
- `ui/components/recent-activity.tsx`: Recent jobs (not rendered).
- `ui/lib/api-client.ts`: API functions.
- `ui/types/index.ts`: TypeScript interfaces.
