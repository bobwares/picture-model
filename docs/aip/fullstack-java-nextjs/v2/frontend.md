# Frontend (Next.js)

## Defaults

- Next.js App Router
- TypeScript
- Tailwind CSS
- React Query for server state
- axios or fetch for HTTP (centralize in `ui/lib/api-client.*`)

## Agent Implementation Rules

- One API client module, used everywhere.
- One consistent query key strategy for React Query.
- Mutations must invalidate or update the correct query keys.
- Keep route state in the URL when it affects shareability (filters, page, sort).

