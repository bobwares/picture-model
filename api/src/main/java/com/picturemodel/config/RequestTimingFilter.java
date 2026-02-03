/**
 * App: Picture Model
 * Package: com.picturemodel.config
 * File: RequestTimingFilter.java
 * Version: 0.1.0
 * Turns: 17
 * Author: codex
 * Date: 2026-02-03T05:19:16Z
 * Exports: RequestTimingFilter
 * Description: Servlet filter that logs request completion time (and async completion/timeout) for debugging.
 */

package com.picturemodel.config;

import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Logs request timing and status for servlet-based requests, including async completions.
 * <p>
 * Spring MVC adapts reactive return types (e.g., Mono) to servlet async processing. If a handler
 * takes too long, the container can time out the async request. This filter makes it visible which
 * endpoint is slow and whether the timeout triggered.
 */
public class RequestTimingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestTimingFilter.class);

    private static final long SLOW_REQUEST_MS = 1000L;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startNs = System.nanoTime();

        try {
            filterChain.doFilter(request, response);
        } finally {
            if (request.isAsyncStarted()) {
                request.getAsyncContext().addListener(new AsyncListener() {
                    @Override
                    public void onComplete(AsyncEvent event) {
                        logCompleted(request, response, startNs, null);
                    }

                    @Override
                    public void onTimeout(AsyncEvent event) {
                        logCompleted(request, response, startNs, new RuntimeException("ASYNC_TIMEOUT"));
                    }

                    @Override
                    public void onError(AsyncEvent event) {
                        Throwable t = event.getThrowable();
                        logCompleted(request, response, startNs, t != null ? t : new RuntimeException("ASYNC_ERROR"));
                    }

                    @Override
                    public void onStartAsync(AsyncEvent event) {
                        // no-op
                    }
                });
            } else {
                logCompleted(request, response, startNs, null);
            }
        }
    }

    private static void logCompleted(
            HttpServletRequest request,
            HttpServletResponse response,
            long startNs,
            Throwable error
    ) {
        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000L;
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String path = query == null ? uri : uri + "?" + query;
        int status = response.getStatus();

        if (error != null) {
            log.warn("HTTP {} {} -> {} in {}ms (async)", method, path, status, elapsedMs);
            return;
        }

        if (elapsedMs >= SLOW_REQUEST_MS) {
            log.info("HTTP {} {} -> {} in {}ms", method, path, status, elapsedMs);
        } else {
            log.debug("HTTP {} {} -> {} in {}ms", method, path, status, elapsedMs);
        }
    }
}

