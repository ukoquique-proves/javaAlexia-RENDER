package com.alexia;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

/**
 * Clase principal de la aplicación Alexia.
 * Punto de entrada para Spring Boot.
 * 
 * @author Alexia Team
 * @version 1.0
 * @since 2025-10-14
 */
@SpringBootApplication
public class AlexiaApplication {

    public static void main(String[] args) {
        loadEnvironmentVariables();
        SpringApplication.run(AlexiaApplication.class, args);
        System.out.println("✓ Alexia Application Started Successfully!");
        System.out.println("✓ Dashboard available at: http://localhost:8080");
    }

    private static void loadEnvironmentVariables() {
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
            );
            System.out.println("✓ Variables de entorno cargadas desde .env");
        } catch (Exception e) {
            System.err.println("⚠ No se pudo cargar archivo .env: " + e.getMessage());
        }
    }
}
