package com.alexia.repository;

import com.alexia.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para operaciones de base de datos con negocios.
 */
@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {

    /**
     * Encuentra negocios por categoría (case-insensitive).
     * Solo retorna negocios activos.
     */
    @Query("SELECT b FROM Business b WHERE LOWER(b.category) = LOWER(:category) AND b.isActive = true ORDER BY b.name")
    List<Business> findByCategoryIgnoreCase(@Param("category") String category);

    /**
     * Encuentra negocios cuya categoría contenga el texto especificado (case-insensitive).
     * Solo retorna negocios activos.
     */
    @Query("SELECT b FROM Business b WHERE LOWER(b.category) LIKE LOWER(CONCAT('%', :category, '%')) AND b.isActive = true ORDER BY b.name")
    List<Business> findByCategoryContainingIgnoreCase(@Param("category") String category);

    /**
     * Encuentra todos los negocios activos ordenados por nombre.
     */
    List<Business> findByIsActiveTrueOrderByName();

    /**
     * Encuentra negocios por nombre (case-insensitive).
     */
    @Query("SELECT b FROM Business b WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%')) AND b.isActive = true ORDER BY b.name")
    List<Business> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Cuenta negocios activos por categoría.
     */
    @Query("SELECT COUNT(b) FROM Business b WHERE LOWER(b.category) = LOWER(:category) AND b.isActive = true")
    long countByCategoryIgnoreCase(@Param("category") String category);

    /**
     * Cuenta todos los negocios activos.
     */
    long countByIsActiveTrue();

    /**
     * Obtiene todas las categorías únicas de negocios activos.
     */
    @Query("SELECT DISTINCT b.category FROM Business b WHERE b.isActive = true AND b.category IS NOT NULL ORDER BY b.category")
    List<String> findDistinctCategories();

    // TODO: Re-enable when PostGIS extension is available
    // /**
    //  * Encuentra negocios dentro de un radio específico (en metros) de una ubicación.
    //  * Usa PostGIS ST_DWithin para búsqueda eficiente.
    //  * 
    //  * @param longitude Longitud del punto central
    //  * @param latitude Latitud del punto central
    //  * @param radiusMeters Radio de búsqueda en metros
    //  * @return Lista de negocios ordenados por distancia
    //  */
    // @Query(value = "SELECT b.*, " +
    //         "ST_Distance(b.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography) as distance " +
    //         "FROM businesses b " +
    //         "WHERE b.is_active = true " +
    //         "AND b.location IS NOT NULL " +
    //         "AND ST_DWithin(b.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, :radiusMeters) " +
    //         "ORDER BY distance", 
    //         nativeQuery = true)
    // List<Business> findNearby(@Param("longitude") double longitude, 
    //                           @Param("latitude") double latitude, 
    //                           @Param("radiusMeters") int radiusMeters);

    // TODO: Re-enable when PostGIS extension is available
    // /**
    //  * Encuentra negocios verificados dentro de un radio específico.
    //  */
    // @Query(value = "SELECT b.*, " +
    //         "ST_Distance(b.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography) as distance " +
    //         "FROM businesses b " +
    //         "WHERE b.is_active = true " +
    //         "AND b.is_verified = true " +
    //         "AND b.location IS NOT NULL " +
    //         "AND ST_DWithin(b.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, :radiusMeters) " +
    //         "ORDER BY distance", 
    //         nativeQuery = true)
    // List<Business> findVerifiedNearby(@Param("longitude") double longitude, 
    //                                   @Param("latitude") double latitude, 
    //                                   @Param("radiusMeters") int radiusMeters);

    // TODO: Re-enable when PostGIS extension is available
    // /**
    //  * Encuentra negocios por categoría dentro de un radio específico.
    //  */
    // @Query(value = "SELECT b.*, " +
    //         "ST_Distance(b.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography) as distance " +
    //         "FROM businesses b " +
    //         "WHERE b.is_active = true " +
    //         "AND LOWER(b.category) LIKE LOWER(CONCAT('%', :category, '%')) " +
    //         "AND b.location IS NOT NULL " +
    //         "AND ST_DWithin(b.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, :radiusMeters) " +
    //         "ORDER BY distance", 
    //         nativeQuery = true)
    // List<Business> findByCategoryNearby(@Param("category") String category,
    //                                     @Param("longitude") double longitude, 
    //                                     @Param("latitude") double latitude, 
    //                                     @Param("radiusMeters") int radiusMeters);

    // TODO: Re-enable when PostGIS extension is available
    // /**
    //  * Encuentra negocios cercanos que coincidan con una consulta de texto (nombre o categoría).
    //  */
    // @Query(value = "SELECT b.* " +
    //         "FROM businesses b " +
    //         "WHERE b.is_active = true " +
    //         "AND (LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
    //         "     LOWER(b.category) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
    //         "     LOWER(:query) LIKE LOWER(CONCAT('%', b.category, '%'))) " +
    //         "AND b.location IS NOT NULL " +
    //         "AND ST_DWithin(b.location, CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography), :radiusMeters) " +
    //         "ORDER BY ST_Distance(b.location, CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography))",
    //         nativeQuery = true)
    // List<Business> findNearbyWithCategory(@Param("query") String query,
    //                                       @Param("longitude") double longitude, 
    //                                       @Param("latitude") double latitude, 
    //                                       @Param("radiusMeters") int radiusMeters);

    /**
     * Encuentra todos los negocios verificados.
     */
    List<Business> findByIsActiveTrueAndIsVerifiedTrueOrderByName();
}
