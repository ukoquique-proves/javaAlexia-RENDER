package com.alexia.service;

import com.alexia.entity.Business;
import com.alexia.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Servicio para funcionalidades de geolocalización.
 * Incluye búsqueda de negocios cercanos y categorías relacionadas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeolocationService {
    
    private final BusinessRepository businessRepository;
    
    // Categorías de negocios que necesitan productos plásticos
    private static final String[] PLASTIC_PRODUCT_CONSUMERS = {
        "restaurant", "restaurante", "cafeteria", "café", "bar", "pub", 
        "catering", "banquete", "evento", "eventos", "catering service",
        "hotel", "hostal", "motel", "lodging", "alojamiento",
        "school", "escuela", "colegio", "universidad", "educación",
        "hospital", "hospitality", "salud", "clínica", "healthcare",
        "party", "fiesta", "celebración", "cumpleaños", "boda",
        "office", "oficina", "corporativo", "business", "negocio",
        "store", "tienda", "retail", "venta al por menor"
    };
    
    /**
     * Busca negocios cercanos que necesiten productos plásticos.
     * 
     * @param longitude Longitud del punto central
     * @param latitude Latitud del punto central
     * @param radiusMeters Radio de búsqueda en metros
     * @return Lista de negocios cercanos
     */
    public List<Business> findPlasticProductConsumersNearby(double longitude, double latitude, int radiusMeters) {
        log.info("Buscando consumidores de productos plásticos cercanos - longitud={}, latitud={}, radio={}m", 
                longitude, latitude, radiusMeters);
        
        // Primero buscar por categorías específicas
        List<Business> businesses = businessRepository.findByCategoryNearby(
            String.join(",", PLASTIC_PRODUCT_CONSUMERS), longitude, latitude, radiusMeters);
        
        // Si no hay suficientes resultados, buscar por proximidad
        if (businesses.size() < 5) {
            log.info("No se encontraron suficientes negocios por categoría, buscando por proximidad");
            businesses = businessRepository.findNearby(longitude, latitude, radiusMeters);
        }
        
        log.info("Se encontraron {} negocios cercanos", businesses.size());
        return businesses;
    }
    
    /**
     * Busca negocios cercanos por categoría específica.
     * 
     * @param category Categoría a buscar
     * @param longitude Longitud del punto central
     * @param latitude Latitud del punto central
     * @param radiusMeters Radio de búsqueda en metros
     * @return Lista de negocios cercanos
     */
    public List<Business> findNearbyByCategory(String category, double longitude, double latitude, int radiusMeters) {
        log.info("Buscando negocios cercanos por categoría - categoría={}, longitud={}, latitud={}, radio={}m", 
                category, longitude, latitude, radiusMeters);
        
        List<Business> businesses = businessRepository.findByCategoryNearby(
            category, longitude, latitude, radiusMeters);
        
        log.info("Se encontraron {} negocios de la categoría '{}' cercanos", businesses.size(), category);
        return businesses;
    }
    
    /**
     * Obtiene todas las categorías de consumidores de productos plásticos.
     * 
     * @return Array de categorías
     */
    public String[] getPlasticProductConsumerCategories() {
        return PLASTIC_PRODUCT_CONSUMERS;
    }
    
    /**
     * Formatea la lista de negocios cercanos para mostrar en Telegram.
     * 
     * @param businesses Lista de negocios
     * @param category Categoría buscada
     * @param radiusMeters Radio de búsqueda en metros
     * @return Mensaje formateado
     */
    public String formatNearbyBusinessesForTelegram(List<Business> businesses, String category, int radiusMeters) {
        if (businesses.isEmpty()) {
            return "❌ No encontré negocios cercanos que necesiten '" + category + "'.\n\n" +
                   "💡 Intenta ampliar el radio de búsqueda o usar otra categoría.";
        }

        StringBuilder message = new StringBuilder();
        message.append("🎯 Encontré ").append(businesses.size())
               .append(" negocio(s) cercanos que pueden necesitar '").append(category).append("'\n\n");

        // Convertir metros a kilómetros si es mayor a 1000m
        String radiusText = radiusMeters >= 1000 ? 
            (radiusMeters / 1000) + "km" : 
            radiusMeters + "m";

        message.append("📍 Radio de búsqueda: ").append(radiusText).append("\n\n");

        for (int i = 0; i < Math.min(businesses.size(), 15); i++) { // Limitar a 15 resultados
            Business business = businesses.get(i);
            message.append(i + 1).append(". ")
                   .append("🏪 ").append(business.getName()).append("\n");

            // Extraer distancia del resultado (añadida por la consulta)
            try {
                // La distancia se añade como propiedad adicional en la consulta
                // En una implementación real, se usaría un DTO o se calcularía aquí
                // Por ahora simulamos la distancia
                double simulatedDistance = calculateSimulatedDistance(business, i);
                String distanceText = simulatedDistance >= 1000 ? 
                    String.format("%.1fkm", simulatedDistance/1000) : 
                    String.format("%.0fm", simulatedDistance);
                
                message.append("   📏 ").append(distanceText).append("\n");
            } catch (Exception e) {
                // Si no se puede obtener la distancia, continuar sin ella
                log.debug("No se pudo obtener la distancia para el negocio: {}", business.getName());
            }

            if (business.getAddress() != null && !business.getAddress().isEmpty()) {
                message.append("   📍 ").append(business.getAddress()).append("\n");
            }

            if (business.getPhone() != null && !business.getPhone().isEmpty()) {
                message.append("   📞 ").append(business.getPhone()).append("\n");
            }

            if (business.getWhatsapp() != null && !business.getWhatsapp().isEmpty()) {
                message.append("   💬 WhatsApp: ").append(business.getWhatsapp()).append("\n");
            }

            // Información adicional útil
            if (business.getCategory() != null && !business.getCategory().isEmpty()) {
                message.append("   🏷️ Categoría: ").append(business.getCategory()).append("\n");
            }

            message.append("\n");
        }

        if (businesses.size() > 15) {
            message.append("... y ").append(businesses.size() - 15).append(" más.\n");
        }

        message.append("💡 Consejo: Usa /contactar [número] para enviar un mensaje de presentación.");

        return message.toString();
    }
    
    /**
     * Simula el cálculo de distancia para demostración.
     * En una implementación real, esta información vendría de la base de datos.
     * 
     * @param business Negocio
     * @param index Índice en la lista
     * @return Distancia simulada en metros
     */
    private double calculateSimulatedDistance(Business business, int index) {
        // Simulación de distancia creciente para demostración
        return 100 + (index * 75);
    }
}
