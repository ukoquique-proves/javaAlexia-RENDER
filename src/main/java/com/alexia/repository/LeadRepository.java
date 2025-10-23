package com.alexia.repository;

import com.alexia.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de leads.
 * Incluye consultas para filtrado por estado, negocio, fuente y consentimiento.
 */
@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    /**
     * Encuentra todos los leads de un negocio específico.
     */
    List<Lead> findByBusinessId(Long businessId);

    /**
     * Encuentra todos los leads de un negocio ordenados por fecha de creación.
     */
    List<Lead> findByBusinessIdOrderByCreatedAtDesc(Long businessId);

    /**
     * Encuentra leads por estado.
     */
    List<Lead> findByStatus(String status);

    /**
     * Encuentra leads por estado y negocio.
     */
    List<Lead> findByBusinessIdAndStatus(Long businessId, String status);

    /**
     * Encuentra leads por fuente (telegram, whatsapp, web, etc).
     */
    List<Lead> findBySource(String source);

    /**
     * Encuentra leads por fuente y negocio.
     */
    List<Lead> findByBusinessIdAndSource(Long businessId, String source);

    /**
     * Encuentra un lead por user_wa_id (WhatsApp/Telegram ID).
     */
    Optional<Lead> findByUserWaId(String userWaId);

    /**
     * Encuentra leads por user_wa_id y negocio.
     */
    List<Lead> findByBusinessIdAndUserWaId(Long businessId, String userWaId);

    /**
     * Encuentra leads que dieron consentimiento.
     */
    List<Lead> findByConsentGiven(Boolean consentGiven);

    /**
     * Encuentra leads activos (no archivados ni perdidos).
     */
    @Query("SELECT l FROM Lead l WHERE l.status NOT IN ('archived', 'lost') ORDER BY l.createdAt DESC")
    List<Lead> findActiveLeads();

    /**
     * Encuentra leads activos de un negocio.
     */
    @Query("SELECT l FROM Lead l WHERE l.businessId = :businessId AND l.status NOT IN ('archived', 'lost') ORDER BY l.createdAt DESC")
    List<Lead> findActiveLeadsByBusiness(@Param("businessId") Long businessId);

    /**
     * Encuentra leads nuevos (status = 'new').
     */
    @Query("SELECT l FROM Lead l WHERE l.status = 'new' ORDER BY l.createdAt DESC")
    List<Lead> findNewLeads();

    /**
     * Encuentra leads nuevos de un negocio.
     */
    @Query("SELECT l FROM Lead l WHERE l.businessId = :businessId AND l.status = 'new' ORDER BY l.createdAt DESC")
    List<Lead> findNewLeadsByBusiness(@Param("businessId") Long businessId);

    /**
     * Encuentra leads convertidos (status = 'converted').
     */
    @Query("SELECT l FROM Lead l WHERE l.status = 'converted' ORDER BY l.createdAt DESC")
    List<Lead> findConvertedLeads();

    /**
     * Encuentra leads creados en un rango de fechas.
     */
    @Query("SELECT l FROM Lead l WHERE l.createdAt BETWEEN :startDate AND :endDate ORDER BY l.createdAt DESC")
    List<Lead> findLeadsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Encuentra leads de un negocio creados en un rango de fechas.
     */
    @Query("SELECT l FROM Lead l WHERE l.businessId = :businessId AND l.createdAt BETWEEN :startDate AND :endDate ORDER BY l.createdAt DESC")
    List<Lead> findLeadsByBusinessAndDateRange(@Param("businessId") Long businessId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Cuenta leads por estado.
     */
    long countByStatus(String status);

    /**
     * Cuenta leads por estado y negocio.
     */
    long countByBusinessIdAndStatus(Long businessId, String status);

    /**
     * Cuenta leads por fuente.
     */
    long countBySource(String source);

    /**
     * Cuenta leads que dieron consentimiento.
     */
    long countByConsentGiven(Boolean consentGiven);

    /**
     * Busca leads por nombre o apellido (búsqueda parcial).
     */
    @Query("SELECT l FROM Lead l WHERE LOWER(l.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(l.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY l.createdAt DESC")
    List<Lead> searchByName(@Param("searchTerm") String searchTerm);

    /**
     * Busca leads por email (búsqueda parcial).
     */
    @Query("SELECT l FROM Lead l WHERE LOWER(l.email) LIKE LOWER(CONCAT('%', :email, '%')) ORDER BY l.createdAt DESC")
    List<Lead> searchByEmail(@Param("email") String email);

    /**
     * Busca leads por teléfono (búsqueda parcial).
     */
    @Query("SELECT l FROM Lead l WHERE l.phone LIKE CONCAT('%', :phone, '%') ORDER BY l.createdAt DESC")
    List<Lead> searchByPhone(@Param("phone") String phone);

    /**
     * Encuentra leads pendientes de sincronización con CRM.
     */
    @Query("SELECT l FROM Lead l WHERE l.crmSyncStatus IS NULL OR l.crmSyncStatus = 'pending' OR l.crmSyncStatus = 'failed' ORDER BY l.createdAt DESC")
    List<Lead> findPendingCrmSync();

    /**
     * Encuentra leads sin consentimiento (para seguimiento).
     */
    @Query("SELECT l FROM Lead l WHERE l.consentGiven = false ORDER BY l.createdAt DESC")
    List<Lead> findLeadsWithoutConsent();
}
