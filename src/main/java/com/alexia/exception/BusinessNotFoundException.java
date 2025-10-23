package com.alexia.exception;

/**
 * Excepción lanzada cuando no se encuentra un negocio.
 */
public class BusinessNotFoundException extends RuntimeException {
    
    public BusinessNotFoundException(Long id) {
        super("Negocio no encontrado con ID: " + id);
    }
    
    public BusinessNotFoundException(String message) {
        super(message);
    }
}
