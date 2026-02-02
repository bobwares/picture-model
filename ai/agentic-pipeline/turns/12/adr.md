# Architecture Decision Record

Create a generic AIP v2 intended for coding-agent reuse, and require context-based discovery.

**Turn**: 12

**Date**: 2026-02-02 - 23:28

**Context**  
The existing AIP (v1) was derived from this specific app. We need a generic v2 that guides a coding agent creating new applications and ensures the agent reliably references the AIP via project context.

**Options Considered**
- Update v1 in place.
- Create a v2 directory with agent-oriented guidance and explicit context integration rules.

**Decision**  
Create `docs/aip/fullstack-java-nextjs/v2` with an agent guide, definition-of-done, and context wiring instructions.

**Result**
- Added a generic AIP v2 bundle designed to be referenced from `ai/context/project_context.md`.

**Consequences**  
- New apps can be created with consistent structure and conventions.
- The agent has an explicit rule to read the AIP from context before generating code.
