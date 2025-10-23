package com.alexia.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO para la solicitud a la API de Grok AI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrokRequest {
    
    /**
     * Modelo de IA a utilizar (ej: "llama-3.1-8b-instant")
     */
    @JsonProperty("model")
    private String model;
    
    /**
     * Lista de mensajes de la conversación
     */
    @JsonProperty("messages")
    private List<GrokMessage> messages;
    
    /**
     * Temperatura para la generación (0.0 - 2.0)
     * Valores más altos = más creatividad
     */
    @JsonProperty("temperature")
    @Builder.Default
    private Double temperature = 0.7;
    
    /**
     * Máximo número de tokens en la respuesta
     */
    @JsonProperty("max_tokens")
    @Builder.Default
    private Integer maxTokens = 1024;
    
    /**
     * Top P para nucleus sampling (0.0 - 1.0)
     */
    @JsonProperty("top_p")
    @Builder.Default
    private Double topP = 1.0;
    
    /**
     * Si se debe hacer streaming de la respuesta
     */
    @JsonProperty("stream")
    @Builder.Default
    private Boolean stream = false;
    
    /**
     * Formato de la respuesta
     */
    @JsonProperty("response_format")
    private Map<String, String> responseFormat;
}
