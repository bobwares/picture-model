# Picture Model UI

Next.js 15 frontend for the Picture Model multi-drive image management system.

## Tech Stack

- **Framework:** Next.js 15 (App Router)
- **Language:** TypeScript 5.9
- **Styling:** Tailwind CSS 3.4
- **State Management:** Zustand 4 + React Query 5
- **Forms:** react-hook-form 7 + zod 3
- **Icons:** Lucide React
- **HTTP Client:** Axios

## Getting Started

### Prerequisites

- Node.js 20 LTS or later
- npm or yarn
- Backend API running on http://localhost:8080

### Installation

```bash
# Install dependencies
npm install

# or
yarn install
```

### Development

```bash
# Start development server
npm run dev

# The UI will be available at http://localhost:3000
```

### Build

```bash
# Build for production
npm run build

# Start production server
npm run start
```

## Project Structure

```
ui/
├── app/                    # Next.js App Router pages
│   ├── layout.tsx         # Root layout with providers
│   ├── page.tsx           # Dashboard (landing page)
│   ├── tree/              # Directory tree view
│   ├── tags/              # Tags view
│   ├── search/            # Search page
│   └── settings/          # Settings page
├── components/            # Reusable UI components
│   ├── header.tsx         # Main navigation header
│   ├── drive-card.tsx     # Drive display card
│   ├── stats-card.tsx     # Statistics card
│   └── recent-activity.tsx # Activity feed
├── lib/                   # Utilities and configuration
│   ├── api-client.ts      # API client with axios
│   └── query-provider.tsx # React Query provider
├── store/                 # Zustand state stores
│   └── use-app-store.ts   # Global app state
├── types/                 # TypeScript type definitions
│   └── index.ts           # Domain types from JSON schemas
└── public/                # Static assets
```

## Pages

### Dashboard (`/`)
- Statistics overview (drives, images, tags)
- Remote file drives list with connection status
- Quick actions (connect, browse, manage)
- Recent activity feed

### Directory Tree (`/tree`)
- Hierarchical folder navigation
- Image browsing by directory structure

### Tags (`/tags`)
- Tag management
- Images organized by tags

### Search (`/search`)
- Full-text search across all images
- Advanced filters (date, drive, tags)
- Results with pagination

### Settings (`/settings`)
- Application configuration
- Drive management
- Crawl settings

## API Integration

The UI connects to the Spring Boot backend API:

**Base URL:** `http://localhost:8080/api`

**Key Endpoints:**
- `/drives` - Drive management
- `/images` - Image search and retrieval
- `/crawler` - Crawl job management
- `/tags` - Tag operations
- `/files` - Image file serving

## Environment Variables

Create a `.env.local` file:

```env
NEXT_PUBLIC_API_BASE=http://localhost:8080
API_BASE=http://localhost:8080
```

## UI Design Specifications

Based on `claude-webapp-design_v2.md`:

- **Landing Page:** Dashboard-first with drive list
- **Layout:** Right sidebar for image details
- **Thumbnails:** Medium size (250x250px, 4-6 per row)
- **Theme:** Light mode
- **Navigation:** Persistent header with view selector

## Development

### Type Safety

Types are auto-generated from backend JSON schemas in `types/index.ts`.

### State Management

- **React Query:** Server state (API calls, caching)
- **Zustand:** Client state (UI preferences, selections)

### Styling

Tailwind CSS with custom configuration. Component classes follow utility-first approach.

## Current Status

✅ **Completed:**
- Project structure and configuration
- Dashboard page with drive list
- API client with React Query integration
- Core components (Header, DriveCard, StatsCard)
- Zustand store setup
- TypeScript types from JSON schemas

⏳ **To Do:**
- Directory tree view implementation
- Tags view with tag management
- Search page with filters
- Image detail modal/page
- Image grid component
- Metadata editor
- WebSocket integration for real-time updates
- Comprehensive testing (Vitest + Playwright)

## Contributing

Follow the agentic pipeline process for all changes. See `../ai/` directory for pipeline documentation.

## License

Private project - Bobwares
