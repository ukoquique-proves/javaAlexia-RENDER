package com.alexia.factory;

import com.alexia.entity.ConnectionTest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Factory para crear instancias de ConnectionTest.
 * Separa la lógica de creación de la entidad.
 * 
 * @author Alexia Team
 * @version 1.0
 * @since 2025-10-15
 */
@Component
public class ConnectionTestFactory {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Crea un registro de prueba de conexión con timestamp actual.
     * 
     * @return Nueva instancia de ConnectionTest
     */
    public ConnectionTest createTestRecord() {
        String testMessage = "Prueba de conexión - " + LocalDateTime.now().format(FORMATTER);
        return new ConnectionTest(testMessage);
    }
    
    /**
     * Crea un registro de prueba con un mensaje personalizado.
     * 
     * @param customMessage Mensaje personalizado
     * @return Nueva instancia de ConnectionTest
     */
    public ConnectionTest createTestRecord(String customMessage) {
        String message = customMessage + " - " + LocalDateTime.now().format(FORMATTER);
        return new ConnectionTest(message);
    }
    
    /**
     * Crea un registro de prueba con mensaje y timestamp específicos.
     * Útil para testing.
     * 
     * @param message Mensaje del registro
     * @param timestamp Timestamp específico
     * @return Nueva instancia de ConnectionTest
     */
    public ConnectionTest createTestRecord(String message, LocalDateTime timestamp) {
        ConnectionTest test = new ConnectionTest(message);
        // Nota: Requeriría setter o builder para establecer timestamp personalizado
        return test;
    }
}
