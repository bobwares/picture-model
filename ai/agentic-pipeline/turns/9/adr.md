# Architecture Decision Record

No architectural decisions required for breadcrumb alignment.

**Turn**: 9

**Date**: 2026-02-02 - 19:00

**Context**  
Align the image view breadcrumb to the left edge to match the full-width layout.

**Options Considered**
- Keep the breadcrumb inside a centered max-width container.
- Remove the max-width container so the breadcrumb spans the full width.

**Decision**  
Remove the max-width container so the breadcrumb aligns with the page edge and image panel padding.


**Result**
- Image view breadcrumb is left-aligned with consistent padding.

**Consequences**  
- Breadcrumb now matches the full-width image layout without introducing new layout wrappers.
