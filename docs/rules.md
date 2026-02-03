# Picture Model — Code-Generation Rules

Derived from turns 1-19. Each rule below maps to one or more errors that were hit in production and fixed. Follow these rules when generating code for this project to avoid repeating those failures.

---

## 1. Hybrid Servlet/Reactive Runtime

The application can boot as either Spring MVC (Servlet) or WebFlux (Reactive) depending on the dependency graph. Do not assume one or the other.

- Any bean whose implementation differs between the two stacks **must** be split with `@ConditionalOnWebApplication(type = ...)`.
- This applies to: CORS configuration, request-timing filters, and any other infrastructure bean that uses stack-specific APIs.
- Example pattern (see `CorsConfig.java`):

```java
@Bean
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public WebMvcConfigurer corsConfigurer(...) { ... }

@Bean
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public CorsWebFilter corsWebFilter(...) { ... }
```

**Origin:** Turn 13 (`fix-cors`), Turn 17 (`fix-async-timeout-logging`).

---

## 2. CORS

CORS must be configured for both runtimes (see rule 1). Do not rely on a single filter or configurer.

- Scope the CORS mapping to `/api/**` only.
- Provide hard-coded fallback origins (`http://localhost:3000`, `http://127.0.0.1:3000`) and methods when the property source is empty or null. The app must not fail to start if CORS properties are missing.
- Allow all headers (`*`) and set `allowCredentials = true`.

**Origin:** Turn 13 (`fix-cors`).

---

## 3. Lazy-Loading / Serialization — The Cardinal Rule

> **Never return a JPA entity directly from a `@RestController` method when that entity has LAZY-loaded associations.**

Jackson serializes the response body **after** the Hibernate session is closed (especially under async dispatch). Any lazy proxy accessed at that point throws `LazyInitializationException`. Do **not** enable Open Session In View (`spring.jpa.open-in-view`) — it does not work reliably with async or reactive dispatch.

Three techniques, applied in order of preference:

### 3a. Use DTOs for all API responses

Map entities to DTOs before returning from the controller. Split mapping into two tiers:

| Tier | When to use | What it reads |
|---|---|---|
| **Summary DTO** | List / grid endpoints | Only columns and associations already initialized by the query (typically `drive`). Never touches `metadata`, `tags`, or other lazy collections. |
| **Detail DTO** | Single-resource endpoints | Reads all associations, but only after they have been fetched via an entity graph (see 3b). |

Example (see `DtoMapper.java`):
```java
public ImageDto toImageSummaryDto(Image image) { ... } // safe for list pages
public ImageDto toImageDetailDto(Image image) { ... } // only call after findDetailById
```

### 3b. Use `@EntityGraph` on repository methods to fetch only what is needed

Never eagerly fetch everything globally. Pin the associations to the specific query that needs them.

```java
// Fetch drive only — used for list/stream endpoints
@EntityGraph(attributePaths = "drive")
Optional<Image> findWithDriveById(UUID id);

// Fetch drive + metadata + tags — used for detail endpoint only
@EntityGraph(attributePaths = {"drive", "metadata", "tags"})
Optional<Image> findDetailById(UUID id);
```

Override JpaRepository defaults (`findAll(Pageable)`, `findById`) with `@EntityGraph` when those defaults are called on entities whose lazy associations will be serialized (see `CrawlJobRepository.java`).

If the controller uses `JpaSpecificationExecutor`, also override `findAll(Specification, Pageable)` with `@EntityGraph` (see `ImageRepository.java`).

### 3c. Wrap blocking repository calls in the reactive layer

All repository calls go through `ReactiveRepositoryWrapper`. The entity graph is resolved inside `Mono.fromCallable(...)` on `boundedElastic`, which keeps the Hibernate session alive for the duration of the call. The DTO mapping must happen inside the same `map(...)` operator, before the Mono completes.

**Origin:** Turn 14 (`fix-lazy-serialization` — CrawlJob), Turn 15 (`fix-image-lazy-serialization` — Image).

---

## 4. Reactive Controller Shape

All `@RestController` endpoints in this project return `Mono<ResponseEntity<...>>` or `Flux<...>`. The underlying data access is blocking JPA.

- Wrap every blocking call with `Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic())`.
- Filesystem / service-layer calls that block (e.g., `DriveService`, `ConnectionManager`) follow the same pattern.
- Never block the Netty event loop. No `.block()` calls inside a controller or WebFilter.

Example (see `DriveController.java`):
```java
@PostMapping("/{id}/connect")
public Mono<ResponseEntity<RemoteFileDriveDto>> connect(@PathVariable UUID id) {
    return Mono.fromCallable(() -> {
        RemoteFileDrive drive = driveService.connect(id);
        return ResponseEntity.ok(dtoMapper.toDto(drive));
    }).subscribeOn(Schedulers.boundedElastic());
}
```

**Origin:** Turns 14, 15 (all controller changes).

---

## 5. Async Timeout

Spring MVC adapts `Mono` return values through servlet async processing. The container's async timeout defaults are short and will fire on any slow operation.

- Set `spring.mvc.async.request-timeout: 300s` in `application.yml`.
- Handle `AsyncRequestTimeoutException` in `GlobalExceptionHandler` and return a `504 GATEWAY_TIMEOUT` with a clear error body.
- Register request-timing filters for both Servlet and Reactive runtimes so slow endpoints are visible in logs before the timeout fires.

**Origin:** Turn 17 (`fix-async-timeout-logging`).

---

## 6. Request-Timing Filters

Always register timing infrastructure for both runtimes (see rule 1).

- **Servlet:** `OncePerRequestFilter` subclass with an `AsyncListener` to capture completion/timeout timing.
- **Reactive:** `WebFilter` with `doFinally` on the downstream signal.
- Log at `DEBUG` for fast requests (< 1 s) and `INFO` for slow requests (>= 1 s). Timeout/error events log at `WARN`.

See `RequestTimingFilter.java` and `RequestTimingConfig.java`.

**Origin:** Turn 17.

---

## 7. Logging Configuration

- `spring.jpa.show-sql: false` in all profiles. Control SQL visibility through the `org.hibernate.SQL` logger level instead.
- Root logger: `INFO`. `com.picturemodel`: `DEBUG`. `org.hibernate.SQL`: `INFO` (not DEBUG — avoids parameter-binding noise).
- Console pattern must include timestamp, thread, level, and logger name:
  `%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n`

**Origin:** Turn 16 (`logging-config`).

---

## 8. OpenAPI and Actuator

Both must be present and configured.

- `pom.xml` must include `spring-boot-starter-actuator` and the appropriate springdoc OpenAPI starter (`springdoc-openapi-webflux-starter` for WebFlux or `springdoc-openapi-starter-webmvc` for MVC).
- `application.yml` must expose at least `health` and `info`:
  ```yaml
  management:
    endpoints:
      web:
        exposure:
          include: health,info
    endpoint:
      health:
        show-details: always
  ```

**Origin:** Turns 3, 4.

---

## 9. UI — Mutations and Navigation

Do not navigate programmatically inside a mutation's `onSuccess` callback. Navigation on state change is surprising and breaks the back-button contract.

- After a mutation succeeds, invalidate the relevant React Query keys so the UI re-renders from fresh data. That is sufficient.
- Navigation to a detail or sub-page must be triggered by an explicit user action: a `<Link>` component or a button with its own click handler.

Example (see `drive-card.tsx` — connect mutation):
```tsx
const connectMutation = useMutation({
  mutationFn: () => driveApi.connect(drive.id),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['drives'] });
    // Do NOT router.push(...) here.
  },
});
```

The "Browse Tree" action is a plain `<Link href={...}>`, not a side-effect.

**Origin:** Turn 18 (`ui-drive-connect-navigation`).

---

## 10. Repository Method Naming and Conventions

- Use Spring Data derived query names where possible (`findByDrive_IdAndDeletedFalse`, etc.).
- When a query needs both filtering logic and an entity graph, use `@Query` + `@EntityGraph` together (see `findByDrive_IdAndDirectoryAndDeletedFalse` in `ImageRepository`).
- Path normalization for directory queries: strip leading `/`, ensure trailing `/`, treat empty/`/` as root (empty string). Do this in the controller before passing to the repository.

**Origin:** Turn 15 (ImageRepository changes).

---

## 11. Connection URL Logging

Never log raw connection URLs — they may contain credentials embedded in the authority component (e.g., `smb://user:password@host`).

- Mask passwords in URLs before logging. Parse with `java.net.URI`, replace the password segment with `***`, reconstruct.
- If parsing fails, fall back to a regex replacement: `//([^/@:]+):([^@]+)@` → `//$1:***@`.

See `ConnectionManager.sanitizeConnectionUrl`.

**Origin:** Observed in `ConnectionManager` (Turn 19 session context).

---

## 12. Health-Check and Connection Lifecycle

- The `ConnectionManager` provider cache is a `ConcurrentHashMap<UUID, FileSystemProvider>`.
- On connect: validate with `testConnection()` **before** caching. Only cache after validation passes.
- On health-check failure: remove from cache, update drive status to `ERROR`, best-effort `disconnect()`.
- On shutdown (`@PreDestroy`): disconnect all cached providers and clear the map.
- Health checks run on a `@Scheduled(fixedRate = ...)` — do not block on the event loop.

**Origin:** Observed across ConnectionManager implementation (Turns 5, 19).

---

## 13. Dev Profile Overrides

`application-dev.yml` overrides must be self-contained for local development:

- Use H2 file-based datasource (`jdbc:h2:file:./data/picturemodel`) so state survives restarts.
- Set `ddl-auto: update` (not `validate`) so schema changes apply automatically during development.
- Disable `show-sql` here too — use logger levels.
- Provide a non-secret encryption key placeholder.

**Origin:** Turn 16.

---

## 14. Metadata Header Convention

Every source file in this project carries a block comment header:

```java
/**
 * App: Picture Model
 * Package: <package>
 * File: <filename>
 * Version: <semver>
 * Turns: <comma-separated turn IDs that touched this file>
 * Author: <name>
 * Date: <ISO timestamp of last change>
 * Exports: <public symbol(s)>
 * Description: <one-line description>. Methods: <method1> - <desc>; <method2> - <desc>.
 */
```

Update the `Turns` list and `Date` whenever the file is modified. This header is present on every `.java` file and `.yml` config file in the repo.

**Exception:** Do **not** add a metadata header to `globals.css`. CSS does not support `/* ... */` block comments in the same way, and the header breaks Tailwind's processing of that file. Leave it clean.

**Origin:** Observed across all turns.
