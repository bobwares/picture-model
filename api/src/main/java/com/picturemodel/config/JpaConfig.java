/**
 * App: Picture Model
 * Package: com.picturemodel.config
 * File: JpaConfig.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: JpaConfig
 * Description: class JpaConfig for JpaConfig responsibilities. Methods: none declared.
 */

package com.picturemodel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA configuration.
 *
 * @author Claude (AI Coding Agent)
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.picturemodel.domain.repository")
@EnableTransactionManagement
public class JpaConfig {
    // Spring Boot auto-configuration handles most JPA setup
}
