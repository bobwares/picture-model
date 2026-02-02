# Context Integration (Required)

The coding agent must be able to discover the active Application Implementation Pattern (AIP) from
project context. For new apps, wire this pattern into the project's context file.

## Required Project Context Fields

Add (or update) the "Application Implementation Pattern" section in `ai/context/project_context.md`
to include:

```md
## Application Implementation Pattern
- **Pattern Id:** fullstack-java-nextjs
- **Pattern Version:** 2
- **Pattern Path:** docs/aip/fullstack-java-nextjs/v2
- **Pattern Description:** Next.js App Router UI + Spring Boot REST API with JPA and PostgreSQL/H2.
```

## Agent Rule

When creating a new app (or a new major feature), the coding agent should:

1. Open the project context (`ai/context/project_context.md`).
2. Resolve the `Pattern Path`.
3. Read `README.md` and `agent-guide.md` from that path before generating code.

If the pattern path is missing, the agent should stop and ask for the desired pattern to use.

