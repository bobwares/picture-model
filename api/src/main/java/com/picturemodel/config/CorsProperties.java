/**
 * App: Picture Model
 * Package: com.picturemodel.config
 * File: CorsProperties.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: CorsProperties
 * Description: class CorsProperties for CorsProperties responsibilities. Methods: none declared.
 */

package com.picturemodel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration properties for CORS settings.
 */
@Configuration
@ConfigurationProperties(prefix = "picture-model.cors")
@Data
public class CorsProperties {

    private List<String> allowedOrigins;
    private List<String> allowedMethods;
    private Long maxAge;
}
