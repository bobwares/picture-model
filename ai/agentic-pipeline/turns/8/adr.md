# Architecture Decision Record

No architectural decisions required for image grid pagination.

**Turn**: 8

**Date**: 2026-02-02 - 07:00

**Context**  
Turn focused on UI pagination behavior for large directories.

**Options Considered**
- Use existing infinite scroll pagination in the UI.
- Introduce virtualization library.

**Decision**  
Reuse the existing infinite scroll pattern with page sizing tuned to thumbnail size.


**Result**
- Tree view now fetches images page-by-page as users scroll.

**Consequences**  
- Large directories load incrementally with lower initial payloads.
