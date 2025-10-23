package com.alexia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respuestas de error estandarizadas.
 * 
 * @author Alexia Team
 * @version 1.0
 * @since 2025-10-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private String message;
    private String error;
    private int status;
    private LocalDateTime timestamp;
    private String path;
    
    /**
     * Crea una respuesta de error con timestamp actual.
     * 
     * @param message Mensaje de error
     * @param error Tipo de error
     * @param status Código de estado HTTP
     * @param path Ruta donde ocurrió el error
     * @return ErrorResponse con timestamp actual
     */
    public static ErrorResponse create(String message, String error, int status, String path) {
        return ErrorResponse.builder()
                .message(message)
                .error(error)
                .status(status)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
