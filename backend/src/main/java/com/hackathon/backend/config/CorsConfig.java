package com.hackathon.backend.config;

/**
 * CORS configuration is intentionally handled in SecurityConfig.java
 * via the corsConfigurationSource() bean and cors(cors -> ...) DSL.
 *
 * Do NOT add a WebMvcConfigurer-based CORS here — when Spring Security is
 * present, its filter chain runs first and adding a second CORS layer via
 * WebMvcConfigurer causes duplicate Access-Control headers, breaking the
 * frontend with "Multiple values in Access-Control-Allow-Origin" errors.
 *
 * To change allowed origins, edit SecurityConfig#corsConfigurationSource().
 */
// CorsConfig intentionally left empty — see SecurityConfig.java
