/**
 * App: Picture Model
 * Package: com.picturemodel.config
 * File: RequestTimingConfig.java
 * Version: 0.1.0
 * Turns: 17
 * Author: codex
 * Date: 2026-02-03T05:19:16Z
 * Exports: RequestTimingConfig
 * Description: Registers request timing logging for servlet and reactive runtimes.
 */

package com.picturemodel.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.server.WebFilter;

/**
 * Registers request timing logging for both Servlet and Reactive runtimes so logs remain usable even
 * if the application boots with a different web stack.
 */
@Configuration
public class RequestTimingConfig {

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public FilterRegistrationBean<RequestTimingFilter> requestTimingFilter() {
        FilterRegistrationBean<RequestTimingFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new RequestTimingFilter());
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        return bean;
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public WebFilter requestTimingWebFilter() {
        return (exchange, chain) -> {
            long startNs = System.nanoTime();
            return chain.filter(exchange)
                    .doFinally(signalType -> {
                        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000L;
                        int status = exchange.getResponse().getStatusCode() != null
                                ? exchange.getResponse().getStatusCode().value()
                                : 0;
                        String path = exchange.getRequest().getURI().getRawPath();
                        String query = exchange.getRequest().getURI().getRawQuery();
                        String fullPath = query == null ? path : path + "?" + query;
                        String method = exchange.getRequest().getMethod() != null
                                ? exchange.getRequest().getMethod().name()
                                : "UNKNOWN";

                        // Keep reactive filter noise low; only log slow requests at INFO.
                        if (elapsedMs >= 1000L) {
                            org.slf4j.LoggerFactory.getLogger(RequestTimingConfig.class)
                                    .info("HTTP {} {} -> {} in {}ms", method, fullPath, status, elapsedMs);
                        } else {
                            org.slf4j.LoggerFactory.getLogger(RequestTimingConfig.class)
                                    .debug("HTTP {} {} -> {} in {}ms", method, fullPath, status, elapsedMs);
                        }
                    });
        };
    }
}

