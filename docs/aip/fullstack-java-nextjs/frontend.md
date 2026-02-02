# Frontend (Next.js App Router)

## App Structure

- Pages live under `ui/app/*` and should focus on composition:
  - data fetching via React Query
  - routing and view state
  - composing reusable components
- Components live under `ui/components/*` and should be reusable.

## API Integration

- Centralize API calls in `ui/lib/api-client.ts`.
- Use `NEXT_PUBLIC_API_BASE` to point the UI at the API host:
  - default: `http://localhost:8080`
- Prefer React Query for:
  - caching
  - pagination
  - mutation invalidation

This repo's client uses axios with `baseURL = ${NEXT_PUBLIC_API_BASE}/api` and also exports helpers for
file URLs under `/api/files/*` (full image + thumbnails).

## Styling

- Tailwind for layout and tokens.
- Prefer a small set of semantic CSS variables (e.g., `--text`, `--muted`, `--border`) for theme cohesion.

## Page Patterns

- Dashboard:
  - summarize drives / system status / recent activity
- Directory tree:
  - left navigation for folders
  - grid for thumbnails
  - paginate or infinite-scroll for large directories
- Image detail:
  - large image panel + right metadata panel
  - file name + breadcrumb + controls (zoom/fullscreen)
- Tags/Search:
  - list + filters; results render in a consistent `ImageGrid`
