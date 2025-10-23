package com.alexia.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa un mensaje en la conversación con Grok AI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrokMessage {
    
    /**
     * Rol del mensaje: "system", "user", o "assistant"
     */
    @JsonProperty("role")
    private String role;
    
    /**
     * Contenido del mensaje
     */
    @JsonProperty("content")
    private String content;
}
