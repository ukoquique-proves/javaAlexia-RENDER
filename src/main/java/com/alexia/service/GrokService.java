package com.alexia.service;

import com.alexia.dto.GrokMessage;
import com.alexia.dto.GrokIntent;
import com.alexia.dto.GrokRequest;
import com.alexia.dto.GrokResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Servicio para comunicación con Grok AI (Groq API).
 */
@Service
@Slf4j
public class GrokService {
    
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final int MAX_HISTORY_SIZE = 20;
    private static final String SYSTEM_PROMPT = 
            "Eres un asistente útil, amigable y conversacional. " +
            "Respondes en español de manera clara y concisa. " +
            "Eres servicial y proporcionas información precisa.";

    private static final String INTENT_PROMPT =
            "You are an expert in classifying user intent for a local business directory bot. " +
            "Analyze the user's message and classify it into one of these intents: PRODUCT_SEARCH, BUSINESS_SEARCH, LEAD_CAPTURE, COMPARE_PRICES, or GENERAL_QUERY. " +
            "Your response MUST be a JSON object. " +
            "\n\nIntent types:\n" +
            "- PRODUCT_SEARCH: User wants to find a specific product (e.g., 'tornillos', 'café', 'pan').\n" +
            "- BUSINESS_SEARCH: User wants to find a business, store, or service, especially if they mention location (e.g., 'cerca', 'cerca de mí', 'en mi barrio').\n" +
            "- LEAD_CAPTURE: User wants to register, sign up, become a client, or provide their contact information.\n" +
            "- COMPARE_PRICES: User wants to compare prices of products from different suppliers.\n" +
            "- GENERAL_QUERY: General conversation, greetings, or questions not related to the other categories.\n" +
            "\n\nFor LEAD_CAPTURE, also extract: hasConsent (boolean), firstName, lastName, phone, email, city if mentioned.\n" +
            "\n\nExamples:\n" +
            "'quiero encontrar cafe': {\"intent\": \"PRODUCT_SEARCH\", \"searchTerm\": \"cafe\", \"confidence\": 0.9}\n" +
            "'dónde hay una panaderia?': {\"intent\": \"BUSINESS_SEARCH\", \"searchTerm\": \"panaderia\", \"confidence\": 0.95}\n" +
            "'Busca ferreterías cerca': {\"intent\": \"BUSINESS_SEARCH\", \"searchTerm\": \"ferreterías\", \"confidence\": 0.98}\n" +
            "'Busca panaderías cerca': {\"intent\": \"BUSINESS_SEARCH\", \"searchTerm\": \"panaderías\", \"confidence\": 0.98}\n" +
            "'restaurantes cerca de mí': {\"intent\": \"BUSINESS_SEARCH\", \"searchTerm\": \"restaurantes\", \"confidence\": 0.98}\n" +
            "'quiero registrarme como cliente': {\"intent\": \"LEAD_CAPTURE\", \"searchTerm\": null, \"confidence\": 0.95, \"hasConsent\": false}\n" +
            "'sí acepto, mi nombre es Juan Pérez': {\"intent\": \"LEAD_CAPTURE\", \"searchTerm\": null, \"confidence\": 0.95, \"hasConsent\": true, \"firstName\": \"Juan\", \"lastName\": \"Pérez\"}\n" +
            "'quién vende vasos más barato?': {\"intent\": \"COMPARE_PRICES\", \"searchTerm\": \"vasos\", \"confidence\": 0.9}\n" +
            "'hola como estas?': {\"intent\": \"GENERAL_QUERY\", \"searchTerm\": null, \"confidence\": 1.0}\n" +
            "\n\nRespond ONLY with the JSON object.";
    
    @Value("${grok.api.key}")
    private String apiKey;
    
    @Value("${grok.api.url}")
    private String apiUrl;
    
    @Value("${grok.model}")
    private String model;
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    // Historial de conversaciones por chat ID
    private final Map<Long, List<GrokMessage>> conversationHistory;
    
    public GrokService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
        this.conversationHistory = new ConcurrentHashMap<>();
    }
    
    /**
     * Obtiene una respuesta de Grok AI para un mensaje de usuario.
     *
     * @param chatId ID del chat
     * @param userMessage Mensaje del usuario
     * @return Respuesta de Grok AI
     */
    public String getResponse(Long chatId, String userMessage) {
        try {
            log.info("Obteniendo respuesta de Grok AI - chatId={}, messageLength={}", 
                    chatId, userMessage.length());
            
            // Obtener o crear historial de conversación
            List<GrokMessage> history = conversationHistory.computeIfAbsent(
                    chatId, k -> new ArrayList<>());
            
            // Agregar mensaje del usuario al historial
            history.add(GrokMessage.builder()
                    .role("user")
                    .content(userMessage)
                    .build());
            
            // Mantener solo los últimos MAX_HISTORY_SIZE mensajes
            if (history.size() > MAX_HISTORY_SIZE) {
                history = new ArrayList<>(history.subList(history.size() - MAX_HISTORY_SIZE, history.size()));
                conversationHistory.put(chatId, history);
            }
            
            // Preparar mensajes para la API
            List<GrokMessage> messages = new ArrayList<>();
            messages.add(GrokMessage.builder()
                    .role("system")
                    .content(SYSTEM_PROMPT)
                    .build());
            messages.addAll(history);
            
            // Crear request
            GrokRequest grokRequest = GrokRequest.builder()
                    .model(model)
                    .messages(messages)
                    .temperature(0.7)
                    .maxTokens(1024)
                    .topP(1.0)
                    .stream(false)
                    .build();
            
            // Llamar a la API
            String response = callGrokApi(grokRequest);
            
            // Agregar respuesta al historial
            if (response != null) {
                history.add(GrokMessage.builder()
                        .role("assistant")
                        .content(response)
                        .build());
                
                log.info("Respuesta de Grok AI recibida - chatId={}, responseLength={}", 
                        chatId, response.length());
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Error al obtener respuesta de Grok AI - chatId={}, error={}", 
                    chatId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Detects the user's intent using Grok AI.
     *
     * @param userMessage The user's message.
     * @return A GrokIntent object with the detected intent and search term.
     */
    public GrokIntent detectIntent(String userMessage) {
        try {
            log.info("Detecting intent for message: '{}'", userMessage);

            List<GrokMessage> messages = new ArrayList<>();
            messages.add(GrokMessage.builder().role("system").content(INTENT_PROMPT).build());
            messages.add(GrokMessage.builder().role("user").content(userMessage).build());

            GrokRequest intentRequest = GrokRequest.builder()
                    .model(model)
                    .messages(messages)
                    .temperature(0.1)
                    .maxTokens(150)
                    .responseFormat(Map.of("type", "json_object"))
                    .build();

            String jsonResponse = callGrokApi(intentRequest);

            if (jsonResponse != null) {
                GrokIntent intent = objectMapper.readValue(jsonResponse, GrokIntent.class);
                log.info("Intent detected: {} with searchTerm '{}' and confidence {}", intent.getIntent(), intent.getSearchTerm(), intent.getConfidence());
                return intent;
            }
        } catch (Exception e) {
            log.error("Error detecting intent: {}", e.getMessage(), e);
        }
        // Default to general query if intent detection fails
        GrokIntent defaultIntent = new GrokIntent();
        defaultIntent.setIntent(GrokIntent.IntentType.GENERAL_QUERY);
        return defaultIntent;
    }
    
    /**
     * Limpia el historial de conversación de un chat.
     *
     * @param chatId ID del chat
     */
    public void clearHistory(Long chatId) {
        conversationHistory.remove(chatId);
        log.info("Historial de conversación limpiado - chatId={}", chatId);
    }
    
    /**
     * Obtiene el tamaño del historial de un chat.
     *
     * @param chatId ID del chat
     * @return Número de mensajes en el historial
     */
    public int getHistorySize(Long chatId) {
        List<GrokMessage> history = conversationHistory.get(chatId);
        return history != null ? history.size() : 0;
    }
    
    /**
     * Obtiene el número total de conversaciones activas.
     *
     * @return Número de conversaciones
     */
    public int getActiveConversationsCount() {
        return conversationHistory.size();
    }
    
    /**
     * Realiza la llamada HTTP a la API de Grok.
     *
     * @param grokRequest Request a enviar
     * @return Respuesta de texto de Grok AI
     * @throws IOException Si hay error en la comunicación
     */
    private String callGrokApi(GrokRequest grokRequest) throws IOException {
        String requestBody = objectMapper.writeValueAsString(grokRequest);
        
        log.debug("Llamando a Grok API - url={}, model={}, messages={}", 
                apiUrl, model, grokRequest.getMessages().size());
        
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, JSON))
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Error en respuesta de Grok API - code={}, message={}", 
                        response.code(), response.message());
                return null;
            }
            
            String responseBody = response.body().string();
            GrokResponse grokResponse = objectMapper.readValue(responseBody, GrokResponse.class);
            
            if (grokResponse.getChoices() != null && !grokResponse.getChoices().isEmpty()) {
                String content = grokResponse.getChoices().get(0).getMessage().getContent();
                
                // Log de uso de tokens
                if (grokResponse.getUsage() != null) {
                    log.debug("Uso de tokens - prompt={}, completion={}, total={}", 
                            grokResponse.getUsage().getPromptTokens(),
                            grokResponse.getUsage().getCompletionTokens(),
                            grokResponse.getUsage().getTotalTokens());
                }
                
                return content;
            }
            
            return null;
        }
    }
}
