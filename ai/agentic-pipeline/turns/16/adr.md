# Architecture Decision Record

No architectural decisions required for logging configuration updates.

**Turn**: 16

**Date**: 2026-02-03 - 05:11

**Context**  
Disable verbose Hibernate SQL logging while improving application logs for debugging.

**Options Considered**
- Keep Hibernate SQL logs enabled and filter in the log viewer.
- Disable SQL logs and improve application log formatting and package-level log levels.

**Decision**  
Disable Hibernate SQL logs (and dev show-sql/binder logs) and update console log pattern to include timestamp, thread, level, and logger name.

**Result**
- SQL logs suppressed.
- com.picturemodel logs remain DEBUG with richer formatting.
