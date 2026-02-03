/**
 * App: Picture Model
 * Package: com.picturemodel.config
 * File: CorsConfig.java
 * Version: 0.1.2
 * Turns: 5,12
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-02-03T02:35:17Z
 * Exports: CorsConfig
 * Description: CORS configuration for allowing UI access to API endpoints. Methods: corsConfigurer - servlet CORS; corsWebFilter - reactive CORS.
 */

package com.picturemodel.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * CORS configuration for allowing web app access.
 *
 * <p>This project may run in either Servlet (Spring MVC) or Reactive (WebFlux) mode depending on the
 * dependency graph (for example, some WebSocket setups pull in the Servlet stack). To avoid
 * environment-dependent CORS failures, this configuration registers CORS support for both web
 * application types.
 */
@Configuration
public class CorsConfig {

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public WebMvcConfigurer corsConfigurer(CorsProperties corsProperties) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(defaultOriginsIfEmpty(corsProperties.getAllowedOrigins()))
                        .allowedMethods(defaultMethodsIfEmpty(corsProperties.getAllowedMethods()))
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(corsProperties.getMaxAge() != null ? corsProperties.getMaxAge() : 3600L);
            }
        };
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public CorsWebFilter corsWebFilter(CorsProperties corsProperties) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(defaultListIfEmpty(corsProperties.getAllowedOrigins(), DEFAULT_ORIGINS));
        config.setAllowedMethods(defaultListIfEmpty(corsProperties.getAllowedMethods(), DEFAULT_METHODS));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return new CorsWebFilter(source);
    }

    private static final List<String> DEFAULT_ORIGINS =
            List.of("http://localhost:3000", "http://127.0.0.1:3000");
    private static final List<String> DEFAULT_METHODS =
            List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");

    private static String[] defaultOriginsIfEmpty(List<String> origins) {
        return defaultListIfEmpty(origins, DEFAULT_ORIGINS).toArray(new String[0]);
    }

    private static String[] defaultMethodsIfEmpty(List<String> methods) {
        return defaultListIfEmpty(methods, DEFAULT_METHODS).toArray(new String[0]);
    }

    private static List<String> defaultListIfEmpty(List<String> values, List<String> defaults) {
        if (values == null || values.isEmpty()) {
            return defaults;
        }
        return values;
    }
}
