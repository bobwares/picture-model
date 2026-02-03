# Architecture Decision Record

No ADR required.

**Turn**: 20

**Date**: 2026-02-03 - 08:07

**Context**  
SMB file reads failed because rootPath was not merged into the SMB connection URL.

**Options Considered**
1. Require users to include rootPath inside connectionUrl manually.
2. Merge rootPath into SMB connectionUrl in the provider factory when missing.

**Decision**  
Chose option 2 to make SMB configuration more forgiving without altering the overall architecture.

**Result**
Updated `FileSystemProviderFactory` to build SMB URLs that include rootPath when needed.

**Consequences**  
- SMB read paths now align with list paths when rootPath is configured separately.
- Existing SMB URLs already containing the root path are left unchanged.
