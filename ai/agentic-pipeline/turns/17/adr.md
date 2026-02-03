# Architecture Decision Record

Add servlet async request timeout config and request timing logging to diagnose slow endpoints.

**Turn**: 17

**Date**: 2026-02-03 - 05:20

**Context**  
The backend throws AsyncRequestTimeoutException under Tomcat (`http-nio-*`) when requests exceed the servlet async timeout (often due to reactive return values adapted by Spring MVC). We need to prevent premature timeouts and add visibility into which endpoints are slow.

**Options Considered**
- Force reactive runtime only (Netty) to avoid servlet async adaptation.
- Increase Spring MVC async timeout and add request timing logs.

**Decision**  
Increase `spring.mvc.async.request-timeout` and add request timing logging filters for both servlet and reactive runtimes.

**Result**
- Timeouts reduced for longer operations.
- Logs now show per-request durations and highlight async timeouts.

**Consequences**  
- Long-running requests may still be slow; this change makes them diagnosable and avoids default timeout failures.
