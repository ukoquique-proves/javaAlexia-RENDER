package com.alexia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para el resultado de la prueba de conexión a la base de datos.
 * Proporciona una estructura clara para transferir datos entre capas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionResultDTO {
    
    private boolean success;
    private Long recordId;
    private Long totalRecords;
    private String message;
    private LocalDateTime timestamp;
    private String errorMessage;
    
    /**
     * Crea un resultado exitoso
     */
    public static ConnectionResultDTO success(Long recordId, Long totalRecords, String message) {
        return ConnectionResultDTO.builder()
                .success(true)
                .recordId(recordId)
                .totalRecords(totalRecords)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Crea un resultado de error
     */
    public static ConnectionResultDTO error(String errorMessage) {
        return ConnectionResultDTO.builder()
                .success(false)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Formatea el resultado como string para mostrar en UI
     */
    public String toDisplayString() {
        if (success) {
            return String.format("✓ Conexión exitosa a Supabase!\n" +
                                "Registro guardado con ID: %d\n" +
                                "Total de registros: %d\n" +
                                "Mensaje: %s", 
                                recordId, totalRecords, message);
        } else {
            return "✗ Error de conexión: " + errorMessage;
        }
    }
}
