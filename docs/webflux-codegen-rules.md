# WebFlux Code-Generation Rules

Derived from every error encountered across turns 1-17 of the Picture Model agentic pipeline.
Each rule is tagged with the turn that produced it.

---

## 1. Repository Method Names Must Be Verified Against the Interface

**Turns: 12, 14, 15**

Before calling any repository method—especially inside a wrapper or service—read the repository
interface and copy the exact method name and signature. Do not guess or extrapolate.

Errors that were caused by this:

| Guessed name | Actual name | Repository |
|---|---|---|
| `findByImage_Id(UUID)` | `findByImageId(UUID)` | `ImageMetadataRepository` |
| `findByDrive_IdOrderByStartedAtDesc` | `findByDrive_IdOrderByStartTimeDesc` | `CrawlJobRepository` |
| `findAllByOrderByStartedAtDesc` | (does not exist; use `findAll(Pageable)`) | `CrawlJobRepository` |

**Rule:** Open the repository interface. Read every method signature. Only then write the wrapper call.

---

## 2. Return Types Must Match the Repository Declaration

**Turns: 12, 15**

`ImageMetadataRepository.findByImageId` returns `List<ImageMetadata>`, not
`Optional<ImageMetadata>`. The wrapper must match: use `Flux` (via `flatMapMany`) for List returns,
`Mono` (via `flatMap(Mono::justOrEmpty)`) for Optional returns.

**Rule:** After reading the method signature, check the declared return type and choose the
correct reactive wrapper:

```
Optional<T>  →  Mono.fromCallable(...)
               .flatMap(Mono::justOrEmpty)
               .subscribeOn(Schedulers.boundedElastic())

List<T>      →  Mono.fromCallable(...)
               .flatMapMany(Flux::fromIterable)
               .subscribeOn(Schedulers.boundedElastic())

Page<T>      →  Mono.fromCallable(...)
               .subscribeOn(Schedulers.boundedElastic())

void         →  Mono.fromRunnable(...)
               .subscribeOn(Schedulers.boundedElastic())
               .then()
```

---

## 3. Void-Returning Controller Endpoints: Use Typed `fromCallable`, Not `fromRunnable().then()`

**Turn: 12 (three identical compilation errors)**

`Mono.fromRunnable(() -> ...).thenReturn(ResponseEntity.noContent().build())` and
`Mono.fromRunnable(() -> ...).then(Mono.just(ResponseEntity.<Void>noContent().build()))`
both lose the generic type through the chain. The compiler infers `Mono<ResponseEntity<Object>>`
instead of `Mono<ResponseEntity<Void>>`.

**Correct pattern:**

```java
return Mono.<ResponseEntity<Void>>fromCallable(() -> {
    service.doSomething(id);
    return ResponseEntity.noContent().build();
}).subscribeOn(Schedulers.boundedElastic());
```

Put the explicit type parameter on `fromCallable`, not on `ResponseEntity.noContent()`.

---

## 4. Place Type Witnesses on `Mono.just()`, Not on `ResponseEntity` Factory Methods

**Turn: 12**

`ResponseEntity.noContent()` and `ResponseEntity.notFound()` are static factory methods that
do not accept type parameters. Writing `ResponseEntity.<Void>noContent()` is syntactically
accepted but does nothing useful, and `Mono.just(...)` infers `Object` downstream.

**Correct:**

```java
Mono.<ResponseEntity<Void>>just(ResponseEntity.noContent().build())
Mono.<ResponseEntity<Void>>just(ResponseEntity.notFound().build())
```

**Incorrect:**

```java
Mono.just(ResponseEntity.<Void>noContent().build())   // infers Object
Mono.just(ResponseEntity.<Void>notFound().build())    // infers Object
```

---

## 5. Never Return JPA Entities Directly from Controllers

**Turns: 14, 15**

Jackson serializes the response body after the Hibernate session is closed (especially under
async/reactive dispatch). Any LAZY-loaded association accessed during serialization throws
`LazyInitializationException`.

**Rule:** Every controller endpoint that returns entity data must map through a DTO first.
The project's established pattern is:

- **List/grid endpoints** → `dtoMapper.toImageSummaryDto(entity)` — does not touch lazy collections.
- **Detail endpoints** → `dtoMapper.toImageDetailDto(entity)` — requires associations to be
  eagerly loaded via `@EntityGraph` on the repository method.

---

## 6. Add `@EntityGraph` to Every Repository Method Whose Results Are Serialized

**Turns: 14, 15**

Even after introducing DTOs, the mapper still reads `.getDrive()`, `.getMetadata()`,
`.getTags()` etc. Those reads will throw if the association was not fetched.

**Per-use-case `@EntityGraph` guidance:**

| Use case | Required `attributePaths` |
|---|---|
| File streaming (needs drive root path) | `"drive"` |
| Image list / summary DTO | `"drive"` |
| Image detail DTO | `"drive", "metadata", "tags"` |
| CrawlJob serialization (has driveName) | `"drive"` |

You may `@Override` inherited `JpaRepository` methods to attach `@EntityGraph`:

```java
@Override
@EntityGraph(attributePaths = "drive")
Page<CrawlJob> findAll(Pageable pageable);

@Override
@EntityGraph(attributePaths = "drive")
Optional<CrawlJob> findById(UUID id);
```

---

## 7. CORS Must Cover Both Servlet and Reactive Runtimes

**Turn: 13**

`spring-boot-starter-webflux` does not guarantee the app boots in reactive mode. WebSocket
or other starters can pull in the servlet stack. A `CorsWebFilter` bean is silently ignored
when the context is servlet-based, and vice versa.

**Rule:** Register both beans, gated by conditional annotations:

```java
@Bean
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public WebMvcConfigurer corsConfigurer(CorsProperties p) { ... }

@Bean
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public CorsWebFilter corsWebFilter(CorsProperties p) { ... }
```

Always supply defaults for origins and methods in case the properties are null at startup.

---

## 8. springdoc Artifact Must Match the Web Stack

**Turn: 3 / 12**

| Stack | Artifact |
|---|---|
| Spring MVC | `springdoc-openapi-starter-webmvc-ui` |
| Spring WebFlux | `springdoc-openapi-starter-webflux-ui` |

Using the wrong one produces either a 404 on `/swagger-ui.html` or a silent failure to
register the OpenAPI endpoint.

---

## 9. WebFlux Validation Exception Is `WebExchangeBindException`, Not `MethodArgumentNotValidException`

**Turn: 12**

`MethodArgumentNotValidException` is a Spring MVC class. The WebFlux equivalent thrown by
`@Valid @RequestBody` is `WebExchangeBindException`. The `GlobalExceptionHandler` must catch
the correct one.

---

## 10. All Blocking I/O Must Run on `Schedulers.boundedElastic()`

**Turn: 12**

JPA repository calls, file-system reads (SMB, SFTP, FTP, local), and thumbnail generation
are all blocking. Every `Mono.fromCallable` or `Mono.fromRunnable` that touches any of these
must chain `.subscribeOn(Schedulers.boundedElastic())`.

Do not call blocking operations inside a `.map()` or `.flatMap()` on the main event loop.

---

## 11. Do Not Enable Open Session In View (OSIV)

**Turn: 15**

OSIV (`spring.jpa.open-in-view: true`) keeps the Hibernate session alive until the HTTP
response is fully written. It does not work reliably under async/reactive dispatch and masks
lazy-loading bugs that surface later under load. The project explicitly avoids it.

Use `@EntityGraph` and DTOs instead.

---

## 12. Hibernate SQL Logging Should Be Off by Default

**Turn: 16**

`spring.jpa.show-sql: true` and `org.hibernate.SQL: DEBUG` flood the console in every
environment. The project convention is:

```yaml
spring:
  jpa:
    show-sql: false
logging:
  level:
    org.hibernate.SQL: INFO       # or OFF
    com.picturemodel: DEBUG       # application-level stays verbose
```

---

## 13. Actuator Endpoints Must Be Explicitly Exposed

**Turn: 4**

Spring Boot 3.x defaults to exposing only `/actuator` itself. Health and info must be opted in:

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

---

## 14. DTO Design: Summary vs Detail Split

**Turn: 15**

All response DTOs follow a two-tier pattern:

1. **Summary DTO** — used in paginated list responses. Contains only scalar fields and pre-computed
   URLs. Never reads lazy collections. Annotated with `@JsonInclude(JsonInclude.Include.NON_NULL)`
   so the absent `metadata`/`tags` fields are omitted from the wire payload.

2. **Detail DTO** — used in single-resource (`/{id}`) responses. Includes nested collections
   (`metadata`, `tags`). Requires the repository to have loaded those via `@EntityGraph`.

Both summary and detail map through a single `DtoMapper` component with explicit method names
(`toImageSummaryDto` / `toImageDetailDto`).

---

## 15. Paginated Responses Must Map Entities to DTOs Before Returning

**Turn: 15**

`Page<Image>.getContent()` returns a live list of JPA proxies. Serializing that list directly
triggers lazy-load failures on any uninitialized collection. Always stream-map to DTOs inside
the reactive `.map()`:

```java
.map(page -> {
    Map<String, Object> response = new HashMap<>();
    response.put("content", page.getContent().stream()
            .map(dtoMapper::toImageSummaryDto)
            .toList());
    response.put("totalElements", page.getTotalElements());
    ...
    return ResponseEntity.ok(response);
});
```

---

## 16. `spring.mvc.async.request-timeout` Is Required for Reactive Returns from Servlet Adapters

**Turn: 13, 17**

When the app boots in servlet mode but controllers return `Mono<…>`, Spring MVC adapts them
via async dispatch. Without an explicit timeout the request may hang indefinitely on slow
blocking calls.

```yaml
spring:
  mvc:
    async:
      request-timeout: 300s
```

---

## Quick Reference: Compilation-Error Checklist

Before committing, verify these common failure points:

1. Every repository method called in `ReactiveRepositoryWrapper` exists in the interface with
   the exact same name and parameter types.
2. `Optional` returns use `.flatMap(Mono::justOrEmpty)`; `List` returns use `.flatMapMany(Flux::fromIterable)`.
3. Void endpoints use `Mono.<ResponseEntity<Void>>fromCallable(…)`, not `fromRunnable().then()`.
4. Type witnesses sit on `Mono.just()` / `Mono.fromCallable()`, never on `ResponseEntity` factories.
5. `GlobalExceptionHandler` catches `WebExchangeBindException`, not `MethodArgumentNotValidException`.
6. `pom.xml` has `springdoc-openapi-starter-webflux-ui` (not `webmvc`).
7. `CorsConfig` has both `@ConditionalOnWebApplication(SERVLET)` and `(REACTIVE)` beans.
8. Every paginated controller endpoint maps entities → DTOs inside `.map()`.
9. Every repository method used by a serialized endpoint has `@EntityGraph` with the correct `attributePaths`.
