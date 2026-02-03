# Architecture Decision Record

No ADR required.

**Turn**: 19

**Date**: 2026-02-03 - 07:36

**Context**  
Minor error-handling adjustment in crawler to skip transient missing files from SMB shares.

**Options Considered**
1. Leave current behavior and fail crawl on missing files.
2. Skip missing files and continue crawl while recording errors.

**Decision**  
Chose option 2 to improve crawl resilience without changing the overall architecture or data model.

**Result**
Updated `CrawlerJobRunner` to treat missing-file exceptions as non-fatal and record them in job errors.

**Consequences**  
- Crawl continues despite transient SMB file disappearance.
- Missing files are logged and recorded, but not retried automatically.
