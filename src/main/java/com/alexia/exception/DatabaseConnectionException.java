package com.alexia.exception;

/**
 * Excepción lanzada cuando hay problemas de conexión con la base de datos.
 * 
 * @author Alexia Team
 * @version 1.0
 * @since 2025-10-15
 */
public class DatabaseConnectionException extends RuntimeException {
    
    public DatabaseConnectionException(String message) {
        super(message);
    }
    
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
