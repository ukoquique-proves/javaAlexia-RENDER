package com.alexia.service;

import com.alexia.entity.Business;
import com.alexia.exception.BusinessNotFoundException;
import com.alexia.repository.BusinessRepository;
import com.alexia.validation.BusinessValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de negocios.
 * Proporciona búsqueda por categoría y operaciones CRUD básicas.
 */
@Service
@Transactional
public class BusinessService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessService.class);

    private final BusinessRepository businessRepository;
    private final BusinessValidator businessValidator;

    public BusinessService(BusinessRepository businessRepository, BusinessValidator businessValidator) {
        this.businessRepository = businessRepository;
        this.businessValidator = businessValidator;
    }

    /**
     * Busca negocios por categoría.
     * Primero intenta búsqueda exacta, si no encuentra resultados, busca por coincidencia parcial.
     *
     * @param category Categoría a buscar
     * @return Lista de negocios encontrados
     */
    public List<Business> searchByCategory(String category) {
        logger.info("Buscando negocios por categoría: {}", category);

        if (category == null || category.trim().isEmpty()) {
            logger.warn("Categoría vacía o nula");
            return List.of();
        }

        String cleanCategory = category.trim();

        // Primero intenta búsqueda exacta
        List<Business> businesses = businessRepository.findByCategoryIgnoreCase(cleanCategory);

        // Si no encuentra resultados, intenta búsqueda parcial
        if (businesses.isEmpty()) {
            logger.info("No se encontraron resultados exactos, buscando coincidencias parciales");
            businesses = businessRepository.findByCategoryContainingIgnoreCase(cleanCategory);
        }

        logger.info("Se encontraron {} negocios para la categoría '{}'", businesses.size(), cleanCategory);
        return businesses;
    }

    /**
     * Busca negocios por nombre.
     *
     * @param name Nombre a buscar
     * @return Lista de negocios encontrados
     */
    public List<Business> searchByName(String name) {
        logger.info("Buscando negocios por nombre: {}", name);

        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }

        List<Business> businesses = businessRepository.findByNameContainingIgnoreCase(name.trim());
        logger.info("Se encontraron {} negocios con el nombre '{}'", businesses.size(), name);
        return businesses;
    }

    /**
     * Obtiene todos los negocios activos.
     *
     * @return Lista de todos los negocios activos
     */
    public List<Business> getAllActiveBusinesses() {
        logger.info("Obteniendo todos los negocios activos");
        return businessRepository.findByIsActiveTrueOrderByName();
    }

    /**
     * Obtiene un negocio por ID.
     *
     * @param id ID del negocio
     * @return Optional con el negocio si existe
     */
    public Optional<Business> getBusinessById(Long id) {
        logger.info("Buscando negocio con ID: {}", id);
        return businessRepository.findById(id);
    }

    /**
     * Guarda un nuevo negocio o actualiza uno existente.
     *
     * @param business Negocio a guardar
     * @return Negocio guardado
     */
    public Business saveBusiness(Business business) {
        logger.info("Guardando negocio: {}", business.getName());
        
        // Validate business data
        businessValidator.validate(business);
        
        return businessRepository.save(business);
    }

    /**
     * Elimina un negocio (soft delete - marca como inactivo).
     *
     * @param id ID del negocio a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean deleteBusiness(Long id) {
        logger.info("Eliminando negocio con ID: {}", id);
        Optional<Business> business = businessRepository.findById(id);

        if (business.isPresent()) {
            Business b = business.get();
            b.setIsActive(false);
            businessRepository.save(b);
            logger.info("Negocio {} marcado como inactivo", b.getName());
            return true;
        }

        logger.warn("No se encontró negocio con ID: {}", id);
        return false;
    }

    /**
     * Cuenta negocios activos por categoría.
     *
     * @param category Categoría a contar
     * @return Número de negocios en la categoría
     */
    public long countByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return 0;
        }
        return businessRepository.countByCategoryIgnoreCase(category.trim());
    }

    /**
     * Cuenta todos los negocios activos.
     *
     * @return Número total de negocios activos
     */
    public long countActiveBusinesses() {
        return businessRepository.countByIsActiveTrue();
    }

    /**
     * Obtiene todas las categorías disponibles.
     *
     * @return Lista de categorías únicas
     */
    public List<String> getAllCategories() {
        logger.info("Obteniendo todas las categorías");
        return businessRepository.findDistinctCategories();
    }

    /**
     * Formatea la lista de negocios para mostrar en Telegram.
     *
     * @param businesses Lista de negocios
     * @param category Categoría buscada
     * @return Mensaje formateado
     */
    public String formatBusinessListForTelegram(List<Business> businesses, String category) {
        if (businesses.isEmpty()) {
            return "❌ No encontré negocios en la categoría '" + category + "'.\n\n" +
                   "Categorías disponibles: " + String.join(", ", getAllCategories());
        }

        StringBuilder message = new StringBuilder();
        message.append("🔍 Encontré ").append(businesses.size())
               .append(" negocio(s) en la categoría '").append(category).append("':\n\n");

        for (int i = 0; i < businesses.size(); i++) {
            Business business = businesses.get(i);
            message.append(i + 1).append(". ")
                   .append("📍 ").append(business.getName()).append("\n");

            if (business.getAddress() != null && !business.getAddress().isEmpty()) {
                message.append("   📌 ").append(business.getAddress()).append("\n");
            }

            if (business.getPhone() != null && !business.getPhone().isEmpty()) {
                message.append("   📞 ").append(business.getPhone()).append("\n");
            }

            message.append("\n");
        }

        return message.toString();
    }
    
    // TODO: Re-enable when categories field is restored
    // /**
    //  * Establece las categorías para un negocio.
    //  *
    //  * @param business Negocio
    //  * @param categories Array de categorías
    //  */
    // public void setBusinessCategories(Business business, String[] categories) {
    //     business.setCategories(categories);
    // }
    // 
    // /**
    //  * Obtiene las categorías de un negocio.
    //  *
    //  * @param business Negocio
    //  * @return Array de categorías
    //  */
    // public String[] getBusinessCategories(Business business) {
    //     return business.getCategories();
    // }
}
