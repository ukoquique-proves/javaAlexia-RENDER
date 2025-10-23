package com.alexia.exception;

/**
 * Excepción lanzada cuando los datos de un producto son inválidos.
 */
public class InvalidProductDataException extends RuntimeException {
    
    public InvalidProductDataException(String message) {
        super(message);
    }
}
