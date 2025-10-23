package com.alexia.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * Database configuration - delegates to Spring Boot's application-prod.properties
 * which uses standard DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD variables.
 * 
 * This class is now simplified since Spring Boot auto-configuration handles
 * the datasource creation from application-prod.properties.
 */
@Configuration
public class DatabaseConfig {

    /**
     * Production datasource - Spring Boot auto-configures from application-prod.properties
     * No manual bean creation needed - Spring Boot handles it automatically
     */
    // Removed manual @Bean - Spring Boot auto-configuration takes over
}
