Title: Upgrade Spring Boot REST API from Spring MVC to WebFlux (Reactive)

Role
You are a senior Java/Spring engineer. Upgrade an existing Spring Boot REST API that currently uses Spring MVC (Servlet stack) to Spring WebFlux (reactive stack) with Project Reactor. Keep behavior compatible and produce a working build with tests.

Context

* Current app: Spring Boot REST API using Spring MVC
* Target app: Spring Boot WebFlux (reactive)
* Goal: Non-blocking request handling end-to-end where feasible
* Java: 17+
* Spring Boot: 3.x

Hard constraints

* Do not change public API semantics (URLs, HTTP methods, status codes, request/response JSON) unless strictly required; document any changes.
* Do not introduce blocking calls on reactive event-loop threads. Any unavoidable blocking must be explicitly isolated using boundedElastic and documented.
* Do not mix MVC and WebFlux runtimes; ensure the application runs on the WebFlux stack.
* Preserve existing security, validation, and CORS behavior.
* Keep logging behavior consistent and production-safe.
* The application must compile and all tests must pass.

Assumptions to validate

1. Identify whether the persistence layer is blocking (JPA/JDBC) or reactive.
2. Identify outbound HTTP clients (RestTemplate, etc.) and migrate to non-blocking where appropriate.
3. Identify Spring Security usage and whether it is servlet-based.

Deliverables

1. A short migration plan with risks and sequencing.
2. Code changes that:

    * switch dependencies and runtime to WebFlux
    * migrate controllers to reactive return types
    * define reactive service boundaries
    * update exception handling to WebFlux-compatible patterns
    * migrate security configuration to reactive security if present
    * migrate outbound HTTP calls to WebClient
3. Updated or new tests using WebTestClient.
4. A final summary listing endpoints touched, remaining blocking islands, and follow-up recommendations.

Implementation steps

1. Baseline

    * Run existing tests and record current behavior.
    * Inventory controllers, exception handlers, filters/interceptors, and security config.

2. Dependencies and runtime

    * Replace spring-boot-starter-web with spring-boot-starter-webflux.
    * Ensure no servlet stack forces MVC at runtime.

3. Controller migration

    * Convert return types:

        * T → Mono<T>
        * List<T> → Flux<T> or Mono<List<T>> (preserve semantics)
        * ResponseEntity<T> → Mono<ResponseEntity<T>> where needed

4. Service and repository layer

    * Expose reactive service APIs used by controllers.
    * If repositories are blocking (JPA/JDBC), isolate calls using
      Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic()).
    * Clearly mark and minimize blocking sections.

5. Exception handling

    * Migrate @ControllerAdvice and error handling to WebFlux-compatible mechanisms.
    * Preserve error response formats and status codes.

6. Security

    * Migrate servlet-based Spring Security configuration to reactive security
      using SecurityWebFilterChain.
    * Replace servlet filters with reactive WebFilter equivalents.

7. Observability

    * Ensure logging and correlation IDs work correctly with reactive execution.
    * Avoid unsafe MDC usage unless explicitly bridged.

8. Tests

    * Replace MockMvc tests with WebTestClient.
    * Ensure integration tests validate the same request/response contracts.

9. Cleanup

    * Remove unused MVC artifacts or migrate them appropriately.
    * Confirm the application runs fully on the WebFlux stack.

Quality gates

* No blocking calls on event-loop threads outside explicitly isolated sections.
* WebFlux runtime confirmed.
* Clean build with all tests passing.
* Remaining blocking areas documented with a clear migration path.

Output format

* Migration plan checklist
* Patch-style summary of file changes with key code snippets
* Final risk and follow-up notes

Begin by inspecting the project structure and identifying all MVC and blocking components, then execute the migration step-by-step with logically grouped changes.
