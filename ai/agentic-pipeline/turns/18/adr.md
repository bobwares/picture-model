# Architecture Decision Record

No architectural decisions required for UI navigation change.

**Turn**: 18

**Date**: 2026-02-03 - 05:26

**Context**  
After connecting a drive, the UI auto-navigated to the directory tree page. The desired behavior is to only navigate when the user explicitly clicks the "Browse tree" action.

**Options Considered**
- Keep auto-navigation after successful connect.
- Remove auto-navigation and rely on the explicit browse button/link.

**Decision**  
Remove the automatic navigation after connect success.

**Result**
- Connecting a drive updates the drive list state but does not change routes.
