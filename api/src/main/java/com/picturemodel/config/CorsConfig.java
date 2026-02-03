/**
 * App: Picture Model
 * Package: com.picturemodel.config
 * File: CorsConfig.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: CorsConfig
 * Description: class CorsConfig for CorsConfig responsibilities. Methods: addCorsMappings - add cors mappings.
 */

package com.picturemodel.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * CORS configuration for allowing web app access (WebFlux).
 *
 * @author Claude (AI Coding Agent)
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final CorsProperties corsProperties;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(corsProperties.getAllowedOrigins());
        config.setAllowedMethods(corsProperties.getAllowedMethods());
        config.setAllowedHeaders(java.util.List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        return new CorsWebFilter(source);
    }
}
