package com.alexia.repository;

import com.alexia.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Product.
 * Proporciona métodos de búsqueda para el catálogo de productos.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Busca productos por negocio.
     */
    List<Product> findByBusinessId(Long businessId);

    /**
     * Busca productos activos por negocio.
     */
    @EntityGraph(attributePaths = {"business"})
    List<Product> findByBusinessIdAndIsActiveTrue(Long businessId);

    /**
     * Busca productos por categoría.
     */
    List<Product> findByCategory(String category);

    /**
     * Busca productos activos por categoría.
     */
    List<Product> findByCategoryAndIsActiveTrue(String category);

    /**
     * Busca productos por nombre (búsqueda parcial, case-insensitive).
     */
    @EntityGraph(attributePaths = {"business"})
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.isActive = true")
    List<Product> searchByName(@Param("name") String name);

    /**
     * Busca productos por nombre o descripción.
     */
    @EntityGraph(attributePaths = {"business"})
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Product> searchByNameOrDescription(@Param("searchTerm") String searchTerm);

    /**
     * Busca productos con stock disponible.
     */
    @Query("SELECT p FROM Product p WHERE p.stock > 0 AND p.isActive = true")
    List<Product> findAvailableProducts();

    /**
     * Busca productos con stock disponible por negocio.
     */
    @Query("SELECT p FROM Product p WHERE p.businessId = :businessId AND p.stock > 0 AND p.isActive = true")
    List<Product> findAvailableProductsByBusiness(@Param("businessId") Long businessId);

    /**
     * Cuenta productos por negocio.
     */
    long countByBusinessId(Long businessId);

    /**
     * Cuenta productos activos por negocio.
     */
    long countByBusinessIdAndIsActiveTrue(Long businessId);

    /**
     * Busca productos por categoría y negocio.
     */
    List<Product> findByCategoryAndBusinessId(String category, Long businessId);

    /**
     * Busca todos los productos activos.
     */
    @EntityGraph(attributePaths = {"business"})
    List<Product> findByIsActiveTrue();
}
