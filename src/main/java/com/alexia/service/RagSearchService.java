package com.alexia.service;

import com.alexia.dto.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagSearchService {
    
    private final SearchService searchService;
    
    /**
     * Perform a RAG search and format the results for Telegram with proper source citation
     */
    public String searchAndFormatForTelegram(String query, Double latitude, Double longitude, Integer radiusMeters) {
        log.info("Performing RAG search for query: '{}' at location: ({}, {}) with radius: {}m", 
                query, latitude, longitude, radiusMeters);
        
        try {
            SearchResult result = searchService.search(query, latitude, longitude, radiusMeters);
            
            if (!result.hasResults()) {
                return "❌ No encontré resultados para '" + query + "'.\n\n" +
                       "💡 Intenta con:\n" +
                       "• Otro término de búsqueda\n" +
                       "• Una categoría más general\n" +
                       "• Ampliar el radio de búsqueda";
            }
            
            return formatSearchResultForTelegram(result);
        } catch (Exception e) {
            log.error("Error performing RAG search for query: '" + query + "'", e);
            return "❌ Hubo un error al realizar la búsqueda. Por favor intenta nuevamente.";
        }
    }
    
    /**
     * Format search results for Telegram with proper source citation
     */
    private String formatSearchResultForTelegram(SearchResult result) {
        StringBuilder response = new StringBuilder();
        response.append(String.format("🔍 Resultados para \"%s\":\n\n", result.getQuery()));
        
        // Add internal results section if available
        if (result.hasInternalResults()) {
            response.append("📍 CERCA DE TI (base interna):\n");
            int count = 1;
            for (int i = 0; i < Math.min(5, result.getInternalResults().size()); i++) { // Limit to 5 results
                var business = result.getInternalResults().get(i);
                response.append(String.format("%d. %s", count++, business.getName()));
                
                // Add distance if available
                // TODO: Re-enable when location field is restored
                // if (business.getLocation() != null) {
                //     // In a real implementation, we would calculate the distance
                //     // For now, we'll just add a placeholder
                //     response.append(" (distancia)");
                // }
                
                response.append("\n");
                
                // Add category if available
                if (business.getCategory() != null) {
                    response.append(String.format("   📂 %s\n", business.getCategory()));
                }
                
                // Add contact info if available
                if (business.getPhone() != null) {
                    response.append(String.format("   📞 %s\n", business.getPhone()));
                }
                
                response.append("\n");
            }
            response.append("\n");
        }
        
        // Add external results section if available
        if (result.hasExternalResults()) {
            response.append("🌐 OTROS PROVEEDORES (web - Google Places):\n");
            int count = result.getInternalCount() + 1;
            for (int i = 0; i < Math.min(5, result.getExternalResults().size()); i++) { // Limit to 5 results
                var external = result.getExternalResults().get(i);
                response.append(String.format("%d. %s", count++, external.getBusinessName()));
                
                // Add distance if available
                if (external.getLatitude() != null && external.getLongitude() != null) {
                    // In a real implementation, we would calculate the distance
                    // For now, we'll just add a placeholder
                    response.append(" (distancia)");
                }
                
                response.append("\n");
                
                // Add category if available
                if (external.getCategory() != null) {
                    response.append(String.format("   📂 %s\n", external.getCategory()));
                }
                
                // Add rating if available
                if (external.getRating() != null) {
                    response.append(String.format("   ⭐ %.1f\n", external.getRating().doubleValue()));
                }
                
                // Add source citation
                response.append(String.format("   🌐 Fuente: %s\n", 
                    external.getSource() != null ? external.getSource() : "Web"));
                
                // Add last updated info
                if (external.getFetchedAt() != null) {
                    response.append(String.format("   🕐 Última actualización: %s\n", 
                        external.getFetchedAt().toString()));
                }
                
                response.append("\n");
            }
        }
        
        // Add source information
        response.append("\n");
        if ("internal".equals(result.getSource())) {
            response.append("✅ Todos los resultados provienen de nuestra base de datos interna.");
        } else if ("external".equals(result.getSource())) {
            response.append("🌐 Todos los resultados provienen de fuentes web externas.");
        } else if ("mixed".equals(result.getSource())) {
            response.append("🔄 Resultados combinados de nuestra base interna y fuentes web externas.");
        }
        
        return response.toString();
    }
}
