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
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration for allowing web app access.
 *
 * @author Claude (AI Coding Agent)
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(corsProperties.getAllowedOrigins().toArray(new String[0]))
                .allowedMethods(corsProperties.getAllowedMethods().toArray(new String[0]))
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(corsProperties.getMaxAge());
    }
}
