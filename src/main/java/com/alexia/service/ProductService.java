package com.alexia.service;

import com.alexia.entity.Product;
import com.alexia.exception.ProductNotFoundException;
import com.alexia.repository.ProductRepository;
import com.alexia.validation.ProductValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de productos.
 * Proporciona operaciones CRUD y búsqueda de productos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductValidator productValidator;

    /**
     * Obtiene todos los productos.
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        log.debug("Obteniendo todos los productos");
        return productRepository.findAll();
    }

    /**
     * Obtiene todos los productos activos.
     */
    @Transactional(readOnly = true)
    public List<Product> getActiveProducts() {
        log.debug("Obteniendo productos activos");
        return productRepository.findByIsActiveTrue();
    }

    /**
     * Obtiene un producto por ID.
     */
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        log.debug("Obteniendo producto con ID: {}", id);
        return productRepository.findById(id);
    }

    /**
     * Obtiene productos por negocio.
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByBusiness(Long businessId) {
        log.debug("Obteniendo productos del negocio: {}", businessId);
        return productRepository.findByBusinessId(businessId);
    }

    /**
     * Obtiene productos activos por negocio.
     */
    @Transactional(readOnly = true)
    public List<Product> getActiveProductsByBusiness(Long businessId) {
        log.debug("Obteniendo productos activos del negocio: {}", businessId);
        return productRepository.findByBusinessIdAndIsActiveTrue(businessId);
    }

    /**
     * Obtiene productos por categoría.
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String category) {
        log.debug("Obteniendo productos de categoría: {}", category);
        return productRepository.findByCategoryAndIsActiveTrue(category);
    }

    /**
     * Busca productos por nombre.
     */
    @Transactional(readOnly = true)
    public List<Product> searchProductsByName(String name) {
        log.debug("Buscando productos con nombre: {}", name);
        return productRepository.searchByName(name);
    }

    /**
     * Busca productos por nombre o descripción.
     */
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String query) {
        log.debug("Buscando productos con query: {}", query);
        return productRepository.searchByNameOrDescription(query);
    }

    /**
     * Obtiene productos disponibles (con stock).
     */
    @Transactional(readOnly = true)
    public List<Product> getAvailableProducts() {
        log.debug("Obteniendo productos disponibles");
        return productRepository.findAvailableProducts();
    }

    /**
     * Obtiene productos disponibles por negocio.
     */
    @Transactional(readOnly = true)
    public List<Product> getAvailableProductsByBusiness(Long businessId) {
        log.debug("Obteniendo productos disponibles del negocio: {}", businessId);
        return productRepository.findAvailableProductsByBusiness(businessId);
    }

    /**
     * Crea un nuevo producto.
     */
    @Transactional
    public Product createProduct(Product product) {
        log.info("Creando nuevo producto: {}", product.getName());
        
        // Validate product data
        productValidator.validate(product);
        
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    /**
     * Actualiza un producto existente.
     */
    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        log.info("Actualizando producto con ID: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
        
        // Validate updated product data
        productValidator.validate(productDetails);
        
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setCategory(productDetails.getCategory());
        product.setImages(productDetails.getImages());
        product.setVariants(productDetails.getVariants());
        product.setStock(productDetails.getStock());
        product.setIsActive(productDetails.getIsActive());
        product.setMetadata(productDetails.getMetadata());
        product.setUpdatedAt(LocalDateTime.now());
        
        return productRepository.save(product);
    }

    /**
     * Elimina un producto (soft delete - marca como inactivo).
     */
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Eliminando producto con ID: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
        
        product.setIsActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    /**
     * Elimina un producto permanentemente.
     */
    @Transactional
    public void hardDeleteProduct(Long id) {
        log.warn("Eliminación permanente de producto con ID: {}", id);
        productRepository.deleteById(id);
    }

    /**
     * Actualiza el stock de un producto.
     */
    @Transactional
    public Product updateStock(Long id, Integer newStock) {
        log.info("Actualizando stock del producto {}: {}", id, newStock);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
        
        // Validate stock update
        productValidator.validateStockUpdate(product.getStock(), newStock);
        
        product.setStock(newStock);
        product.setUpdatedAt(LocalDateTime.now());
        
        return productRepository.save(product);
    }

    /**
     * Cuenta productos por negocio.
     */
    @Transactional(readOnly = true)
    public long countProductsByBusiness(Long businessId) {
        return productRepository.countByBusinessId(businessId);
    }

    /**
     * Cuenta productos activos por negocio.
     */
    @Transactional(readOnly = true)
    public long countActiveProductsByBusiness(Long businessId) {
        return productRepository.countByBusinessIdAndIsActiveTrue(businessId);
    }

    /**
     * Verifica si un producto existe.
     */
    @Transactional(readOnly = true)
    public boolean productExists(Long id) {
        return productRepository.existsById(id);
    }
}
