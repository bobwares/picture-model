# Spring MVC to WebFlux Migration Plan
## Picture Model API - Reactive Migration Strategy

**Date:** 2026-02-02
**Target:** Spring Boot 3.2.1, Java 21, Project Reactor

---

## Executive Summary

**Current State:** Servlet-based Spring MVC REST API with JPA/JDBC blocking persistence
**Target State:** Reactive Spring WebFlux API with explicit blocking isolation
**Migration Approach:** Phased migration with blocking operations isolated on boundedElastic scheduler

---

## 1. Baseline Assessment

### 1.1 Blocking Components Inventory

#### **CRITICAL BLOCKERS:**

1. **JPA Repositories (5 repositories)**
   - `ImageRepository` (JpaRepository + Specifications)
   - `CrawlJobRepository`
   - `TagRepository`
   - `RemoteFileDriveRepository`
   - `ImageMetadataRepository`
   - **Risk:** HIGH - Core data access layer is entirely blocking
   - **Strategy:** Isolate with `Schedulers.boundedElastic()`

2. **File System I/O Operations**
   - SMB, SFTP, FTP, Local file system providers
   - Image streaming (`FileController.getImage()`)
   - Thumbnail generation with Thumbnailator
   - **Risk:** HIGH - All file operations are blocking
   - **Strategy:** Wrap in `Mono.fromCallable().subscribeOn(boundedElastic())`

3. **Async Crawl Jobs**
   - Current: `@Async` with ThreadPoolTaskExecutor
   - `CrawlerJobRunner` processes directories recursively
   - **Risk:** MEDIUM - Can remain async, but consider reactive refactor
   - **Strategy:** Keep current async pattern initially, migrate later if needed

4. **EXIF Extraction**
   - `ExifExtractorService.extract()` uses metadata-extractor library (blocking)
   - **Risk:** LOW - Called infrequently during crawls
   - **Strategy:** Wrap in `boundedElastic()` scheduler

5. **Credential Encryption**
   - Jasypt encryption/decryption (CPU-bound, blocking)
   - **Risk:** LOW - Called infrequently
   - **Strategy:** Wrap in `boundedElastic()` scheduler

#### **NON-BLOCKERS:**

- ✅ **No Spring Security** - No servlet filters to migrate
- ✅ **No HTTP Clients** - No RestTemplate to migrate
- ✅ **No ServletContext dependencies**

---

### 1.2 Controller Endpoint Inventory

| Controller | Endpoints | Return Types | Complexity |
|------------|-----------|--------------|------------|
| DriveController | 11 | ResponseEntity, DirectoryTreeNode | HIGH |
| ImageController | 2 | ResponseEntity, Page<Image> | MEDIUM |
| CrawlerController | 6 | ResponseEntity, Page<CrawlJob> | MEDIUM |
| TagController | 4 | ResponseEntity, List<Tag> | LOW |
| FileController | 2 | byte[] (streaming) | HIGH |
| SystemController | 2 | Map<String, Object> | LOW |
| **Total** | **27** | | |

---

### 1.3 Service Layer Inventory

| Service | Blocking Operations | Complexity |
|---------|---------------------|------------|
| DriveService | JPA, credential encryption | HIGH |
| CrawlerService | JPA | MEDIUM |
| ConnectionManager | None (in-memory cache) | LOW |
| CrawlerJobRunner | JPA, File I/O, EXIF | HIGH |
| ExifExtractorService | Image metadata extraction | MEDIUM |

---

## 2. Migration Strategy

### 2.1 Phased Approach

**Phase 1: Infrastructure & Dependencies** (Day 1)
- Switch to WebFlux runtime
- Update dependencies (pom.xml)
- Migrate CORS configuration
- Update exception handling

**Phase 2: Controller Layer** (Day 2-3)
- Migrate all 27 endpoints to reactive signatures
- Convert return types to Mono/Flux
- Preserve JSON contracts

**Phase 3: Service Layer Wrapping** (Day 3-4)
- Wrap JPA calls in `Schedulers.boundedElastic()`
- Wrap File I/O in `boundedElastic()`
- Update service method signatures

**Phase 4: Testing** (Day 4-5)
- Migrate to WebTestClient
- Integration testing
- Load testing to verify non-blocking behavior

**Phase 5: Documentation & Cleanup** (Day 5)
- Update API documentation
- Document blocking islands
- Performance benchmarking

---

### 2.2 Key Technical Decisions

#### **Decision 1: Keep JPA (Do NOT migrate to R2DBC)**

**Rationale:**
- R2DBC requires rewriting all repositories
- R2DBC lacks feature parity with JPA (no JpaSpecificationExecutor, no lazy loading)
- Current queries use advanced JPA features (Specifications, custom @Query)
- Migration cost too high for incremental benefit

**Approach:**
- Wrap all JPA calls with `Mono.fromCallable(() -> repository.method()).subscribeOn(Schedulers.boundedElastic())`
- Document all blocking boundaries
- Monitor thread pool usage

#### **Decision 2: Keep Current Async Crawl Pattern**

**Rationale:**
- Crawl jobs are long-running background tasks
- Current `@Async` pattern works well
- Refactoring to reactive would be complex with minimal benefit

**Approach:**
- Keep `@EnableAsync` configuration
- Wrap status queries in reactive layer
- Consider future migration to reactive streams for progress updates

#### **Decision 3: Stream File Content Reactively**

**Approach:**
- Use `DataBuffer` for streaming image bytes
- Replace `byte[]` with `Flux<DataBuffer>`
- Maintain backpressure for large files

---

## 3. Risk Assessment

### 3.1 High Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| **JPA blocking on event loop** | Application deadlock | Strict use of `boundedElastic()`, code review, tests |
| **File I/O blocking** | Thread starvation | Wrap all I/O in `boundedElastic()`, monitor thread pools |
| **Memory pressure from streaming** | OOM errors | Use `DataBuffer` with proper releasing, backpressure |
| **Pagination breaking** | Incorrect result sets | Extensive testing of `Page<T>` → `Mono<Page<T>>` |
| **Transaction boundaries** | Data inconsistency | Careful review of `@Transactional` with reactive |

### 3.2 Medium Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| **WebSocket compatibility** | Lost real-time updates | Test WebSocket over WebFlux (should work) |
| **OpenAPI/Swagger** | Broken docs | Update to springdoc-openapi-webflux-ui |
| **Actuator endpoints** | Missing metrics | Verify actuator works with WebFlux |
| **Test migration effort** | Delayed delivery | Allocate 2 days for test migration |

### 3.3 Low Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| **CORS breaking** | Frontend errors | Straightforward migration to `CorsWebFilter` |
| **Exception handling** | Wrong error responses | WebFlux error handling is similar to MVC |
| **Logging** | Lost context | Use Reactor Context for correlation IDs |

---

## 4. Implementation Sequence

### 4.1 Step-by-Step Execution Plan

#### **Step 1: Baseline Tests**
```bash
# Run existing tests and capture results
mvn clean test > baseline-test-results.txt
mvn spring-boot:run # Verify app starts
curl http://localhost:8080/actuator/health # Verify health
```

#### **Step 2: Update pom.xml**
```xml
<!-- REMOVE -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- ADD -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- UPDATE -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

#### **Step 3: Migrate Configuration Classes**

**3a. Remove Servlet-based CORS Config**
```java
// DELETE: CorsConfig.java (implements WebMvcConfigurer)

// CREATE: ReactiveCorsConfig.java
@Configuration
public class ReactiveCorsConfig {
    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://127.0.0.1:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        return new CorsWebFilter(source);
    }
}
```

**3b. Update Async Config (no changes needed)**

**3c. Update Exception Handler**
```java
// UPDATE: GlobalExceptionHandler.java
// Change @RestControllerAdvice to support reactive
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class) // Was MethodArgumentNotValidException
    public Mono<ResponseEntity<ErrorDto>> handleValidationException(WebExchangeBindException ex) {
        // ...
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorDto>> handleIllegalArgument(IllegalArgumentException ex) {
        // ...
    }

    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<ErrorDto>> handleRuntimeException(RuntimeException ex) {
        // ...
    }
}
```

#### **Step 4: Create Reactive Repository Wrappers**

**Create:** `ReactiveRepositoryWrapper.java`
```java
@Component
public class ReactiveRepositoryWrapper {

    private final ImageRepository imageRepository;
    private final TagRepository tagRepository;
    // ... other repositories

    public Mono<Image> findImageById(UUID id) {
        return Mono.fromCallable(() -> imageRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(Mono::justOrEmpty);
    }

    public Flux<Tag> findAllTags() {
        return Mono.fromCallable(() -> tagRepository.findAll())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }

    // ... more wrapper methods
}
```

#### **Step 5: Migrate Controllers (Example: TagController)**

**Before (MVC):**
```java
@GetMapping
public ResponseEntity<List<Tag>> getAll() {
    List<Tag> tags = tagRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    return ResponseEntity.ok(tags);
}
```

**After (WebFlux):**
```java
@GetMapping
public Mono<ResponseEntity<List<Tag>>> getAll() {
    return Mono.fromCallable(() ->
            tagRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
        )
        .subscribeOn(Schedulers.boundedElastic())
        .map(ResponseEntity::ok);
}
```

#### **Step 6: Migrate File Streaming (FileController)**

**Before (MVC):**
```java
@GetMapping("/{imageId}")
public ResponseEntity<byte[]> getImage(@PathVariable UUID imageId) {
    byte[] bytes = loadImageBytes(imageId);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(mimeType))
        .body(bytes);
}
```

**After (WebFlux):**
```java
@GetMapping("/{imageId}")
public Mono<ResponseEntity<Flux<DataBuffer>>> getImage(@PathVariable UUID imageId) {
    return Mono.fromCallable(() -> loadImage(imageId))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(image -> {
            Flux<DataBuffer> dataBufferFlux = Mono.fromCallable(() ->
                    readImageInputStream(image)
                )
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(inputStream ->
                    DataBufferUtils.readInputStream(
                        () -> inputStream,
                        new DefaultDataBufferFactory(),
                        4096
                    )
                );

            return Mono.just(ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getMimeType()))
                .body(dataBufferFlux));
        });
}
```

#### **Step 7: Migrate Service Layer**

**Before (MVC):**
```java
@Service
public class DriveService {
    public RemoteFileDrive createDrive(RemoteFileDrive drive, Map<String, String> credentials) {
        // ... encrypt credentials
        drive.setStatus(ConnectionStatus.DISCONNECTED);
        return driveRepository.save(drive);
    }
}
```

**After (WebFlux):**
```java
@Service
public class DriveService {
    public Mono<RemoteFileDrive> createDrive(RemoteFileDrive drive, Map<String, String> credentials) {
        return Mono.fromCallable(() -> {
            // ... encrypt credentials (blocking)
            drive.setStatus(ConnectionStatus.DISCONNECTED);
            return driveRepository.save(drive);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
```

#### **Step 8: Migrate Tests**

**Before (MVC):**
```java
@AutoConfigureMockMvc
class DriveControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGetAllDrives() throws Exception {
        mockMvc.perform(get("/api/drives"))
            .andExpect(status().isOk());
    }
}
```

**After (WebFlux):**
```java
@WebFluxTest(DriveController.class)
class DriveControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldGetAllDrives() {
        webTestClient.get().uri("/api/drives")
            .exchange()
            .expectStatus().isOk();
    }
}
```

---

## 5. Testing Strategy

### 5.1 Unit Tests
- Migrate all controller tests to WebTestClient
- Test reactive chains with StepVerifier
- Verify backpressure behavior

### 5.2 Integration Tests
- Test end-to-end request/response contracts
- Verify blocking operations execute on boundedElastic
- Load test with concurrent requests

### 5.3 Performance Tests
- Measure thread usage (should use fewer threads than MVC)
- Test large file streaming (memory usage)
- Verify no deadlocks under load

---

## 6. Quality Gates

### 6.1 Mandatory Checks
- [ ] No blocking calls on event-loop threads (use BlockHound in tests)
- [ ] All tests passing
- [ ] WebFlux runtime confirmed (`EmbeddedNetty` in logs, not `Tomcat`)
- [ ] All 27 endpoints return correct responses
- [ ] File streaming works without memory leaks
- [ ] Pagination works correctly
- [ ] Transaction boundaries verified

### 6.2 Performance Baselines
- [ ] Response times ≤ MVC baseline
- [ ] Memory usage ≤ MVC + 20% (due to Reactor overhead)
- [ ] Thread count < 50 under load (vs 200+ in MVC)
- [ ] Throughput ≥ MVC baseline

---

## 7. Rollback Plan

### 7.1 Git Strategy
```bash
# Create migration branch
git checkout -b feature/webflux-migration

# Commit each phase separately
git commit -m "Phase 1: Update dependencies"
git commit -m "Phase 2: Migrate controllers"
# etc.

# If rollback needed
git checkout main
git branch -D feature/webflux-migration
```

### 7.2 Rollback Triggers
- Test failures > 10%
- Performance degradation > 30%
- Deadlock/blocking detected
- Memory leaks in file streaming
- Data corruption in transactions

---

## 8. Post-Migration Follow-up

### 8.1 Short-term (1-2 weeks)
- [ ] Monitor production metrics (if deployed)
- [ ] Profile blocking calls with BlockHound
- [ ] Optimize hot paths
- [ ] Add reactive logging with Reactor Context

### 8.2 Medium-term (1-3 months)
- [ ] Consider R2DBC migration (if blocking becomes bottleneck)
- [ ] Migrate crawl jobs to reactive streams
- [ ] Add reactive WebSocket for real-time crawl updates
- [ ] Implement reactive caching (Redis Reactive)

### 8.3 Long-term (3-6 months)
- [ ] Full reactive stack (R2DBC, reactive drivers)
- [ ] Reactive file streaming from file system providers
- [ ] Event-driven architecture with reactive messaging

---

## 9. Documentation Requirements

### 9.1 Update After Migration
- [ ] API documentation (OpenAPI/Swagger)
- [ ] Architecture diagrams (add reactive flow)
- [ ] Developer onboarding docs
- [ ] Deployment guides (Netty vs Tomcat)

### 9.2 New Documentation
- [ ] Blocking islands map (where JPA calls happen)
- [ ] Thread pool configuration guide
- [ ] Reactive patterns used
- [ ] Performance tuning guide

---

## 10. Success Metrics

| Metric | Baseline (MVC) | Target (WebFlux) |
|--------|----------------|------------------|
| Avg response time | TBD | ≤ baseline |
| P99 response time | TBD | ≤ baseline |
| Thread count (200 RPS) | ~200 threads | < 50 threads |
| Memory usage | TBD | ≤ baseline + 20% |
| Throughput (RPS) | TBD | ≥ baseline |
| Build time | TBD | ≤ baseline + 10% |
| Test execution time | TBD | ≤ baseline |

---

## 11. Team Readiness

### 11.1 Required Skills
- [ ] Understanding of reactive programming (Reactor)
- [ ] Mono/Flux operators
- [ ] Schedulers (boundedElastic, parallel)
- [ ] Backpressure concepts
- [ ] Debugging reactive code

### 11.2 Training Materials
- Project Reactor documentation
- Baeldung reactive guides
- Spring WebFlux reference docs
- BlockHound setup guide

---

## 12. Timeline Estimate

| Phase | Duration | Dependencies |
|-------|----------|-------------|
| Planning & baseline | 1 day | None |
| Infrastructure | 1 day | Planning complete |
| Controller migration | 2 days | Infrastructure done |
| Service migration | 2 days | Controllers done |
| Testing | 2 days | All code migrated |
| Documentation | 1 day | Tests passing |
| **Total** | **9 days** | |

---

## 13. Appendix: Key Code Patterns

### Pattern 1: Wrap Blocking JPA Call
```java
public Mono<Entity> findById(UUID id) {
    return Mono.fromCallable(() -> repository.findById(id))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(Mono::justOrEmpty);
}
```

### Pattern 2: Wrap Blocking File I/O
```java
public Flux<DataBuffer> readFile(String path) {
    return Mono.fromCallable(() -> openInputStream(path))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(inputStream ->
            DataBufferUtils.readInputStream(
                () -> inputStream,
                bufferFactory,
                4096
            )
        );
}
```

### Pattern 3: Transactional Reactive Method
```java
@Transactional
public Mono<Entity> save(Entity entity) {
    return Mono.fromCallable(() -> repository.save(entity))
        .subscribeOn(Schedulers.boundedElastic());
}
```

### Pattern 4: Pagination
```java
public Mono<Page<Entity>> findAll(Pageable pageable) {
    return Mono.fromCallable(() -> repository.findAll(pageable))
        .subscribeOn(Schedulers.boundedElastic());
}
```

---

## Approval & Sign-off

**Prepared by:** Claude Sonnet 4.5
**Date:** 2026-02-02
**Status:** READY FOR REVIEW

**Approvals Required:**
- [ ] Tech Lead
- [ ] QA Lead
- [ ] DevOps Lead

---

**Next Steps:**
1. Review and approve this plan
2. Execute Phase 1: Update dependencies
3. Iterative implementation with continuous testing
