# Architecture Decision Record

Eagerly fetch CrawlJob.drive via repository entity graphs to avoid LazyInitializationException.

**Turn**: 14

**Date**: 2026-02-03 - 02:44

**Context**  
The API returns CrawlJob entities directly from paged endpoints. These entities expose computed JSON fields (driveName/driveId) that dereference a LAZY ManyToOne association. Because the controller executes asynchronously, JSON serialization happens after the Hibernate session is closed, causing LazyInitializationException.

**Options Considered**
- Return DTOs instead of entities for crawl job endpoints.
- Keep returning entities but ensure required associations are fetched eagerly for these queries.
- Enable Open Session In View (OSIV) (not suitable for async/reactive-style execution).

**Decision**  
Keep the current response shape, but override CrawlJobRepository query methods with `@EntityGraph(attributePaths = "drive")` so the drive association is initialized before serialization.

**Result**
- Crawl job list and detail endpoints can serialize driveName without triggering lazy loading.

**Consequences**  
- Slightly more data fetched per crawl job row (ManyToOne drive), but avoids runtime serialization failures.
