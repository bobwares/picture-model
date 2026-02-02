# Architecture Decision Record

No architectural decisions required for right panel UI behavior changes.

**Turn**: 7

**Date**: 2026-02-02 - 06:48

**Context**  
Turn focused on UI adjustments for thumbnail rendering and scrolling.

**Options Considered**
- Adjust the tree page query and controls to show all thumbnails.
- Defer to backend paging or virtualized list changes.

**Decision**  
Update the tree page UI to remove the 24-image cap, add size controls, and enable scrolling.


**Result**
- Right panel loads all fetched thumbnails and supports size selection.

**Consequences**  
- Large directories may increase initial load time until pagination or virtualization is introduced.
