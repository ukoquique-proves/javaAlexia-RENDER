package com.alexia.exception;

/**
 * Excepción lanzada cuando hay problemas con operaciones de Telegram.
 * 
 * @author Alexia Team
 * @version 1.0
 * @since 2025-10-15
 */
public class TelegramException extends RuntimeException {
    
    public TelegramException(String message) {
        super(message);
    }
    
    public TelegramException(String message, Throwable cause) {
        super(message, cause);
    }
}
