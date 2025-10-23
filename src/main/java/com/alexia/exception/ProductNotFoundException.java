package com.alexia.exception;

/**
 * Excepción lanzada cuando no se encuentra un producto.
 */
public class ProductNotFoundException extends RuntimeException {
    
    public ProductNotFoundException(Long id) {
        super("Producto no encontrado con ID: " + id);
    }
    
    public ProductNotFoundException(String message) {
        super(message);
    }
}
