package com.alexia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para la respuesta de la API de Grok AI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GrokResponse {
    
    /**
     * ID único de la respuesta
     */
    @JsonProperty("id")
    private String id;
    
    /**
     * Tipo de objeto (ej: "chat.completion")
     */
    @JsonProperty("object")
    private String object;
    
    /**
     * Timestamp de creación
     */
    @JsonProperty("created")
    private Long created;
    
    /**
     * Modelo utilizado
     */
    @JsonProperty("model")
    private String model;
    
    /**
     * Lista de opciones de respuesta
     */
    @JsonProperty("choices")
    private List<Choice> choices;
    
    /**
     * Información de uso de tokens
     */
    @JsonProperty("usage")
    private Usage usage;
    
    /**
     * Clase interna para las opciones de respuesta
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        
        @JsonProperty("index")
        private Integer index;
        
        @JsonProperty("message")
        private GrokMessage message;
        
        @JsonProperty("finish_reason")
        private String finishReason;
    }
    
    /**
     * Clase interna para información de uso
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;
        
        @JsonProperty("completion_tokens")
        private Integer completionTokens;
        
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}
