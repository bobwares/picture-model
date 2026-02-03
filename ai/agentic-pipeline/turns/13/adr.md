# Architecture Decision Record

Support CORS in both Servlet and Reactive runtimes.

**Turn**: 13

**Date**: 2026-02-03 - 02:35

**Context**  
The UI (http://localhost:3000) cannot call the API due to missing CORS headers. The backend can run in different web application modes depending on the dependency graph, so a WebFlux-only CORS filter may not be applied.

**Options Considered**
- Force reactive mode only.
- Add Servlet (Spring MVC) CORS configuration in addition to the existing reactive filter.

**Decision**  
Register CORS for both Servlet and Reactive web application types using conditional beans, ensuring the API always returns the required CORS headers.

**Result**
- Updated CORS configuration to apply in both runtimes.

**Consequences**  
- Eliminates environment-dependent CORS failures when the app boots as Servlet vs Reactive.
