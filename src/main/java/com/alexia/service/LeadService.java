package com.alexia.service;

import com.alexia.entity.Lead;
import com.alexia.repository.LeadRepository;
import com.alexia.validation.LeadValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de leads.
 * Incluye validación de consentimiento GDPR/LGPD y lógica de negocio.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeadService {

    private final LeadRepository leadRepository;
    private final LeadValidator leadValidator;

    /**
     * Crea un nuevo lead con validación completa.
     */
    @Transactional
    public Lead createLead(Lead lead) {
        log.info("Creando nuevo lead: {} {} - Source: {}", lead.getFirstName(), lead.getLastName(), lead.getSource());
        
        // Validate lead data
        leadValidator.validateLeadData(lead.getFirstName(), lead.getLastName(), lead.getPhone(), lead.getEmail());
        leadValidator.validateSource(lead.getSource());
        leadValidator.validateBusinessReference(lead.getBusinessId());
        
        if (lead.getUserWaId() != null && !lead.getUserWaId().trim().isEmpty()) {
            leadValidator.validateUserWaId(lead.getUserWaId());
        }
        
        // Validate consent (critical for GDPR/LGPD)
        leadValidator.validateConsent(lead.getConsentGiven());
        
        // Set consent date if consent is given
        if (lead.getConsentGiven() && lead.getConsentDate() == null) {
            lead.setConsentDate(LocalDateTime.now());
        }
        
        // Set default status if not provided
        if (lead.getStatus() == null || lead.getStatus().trim().isEmpty()) {
            lead.setStatus("new");
        }
        
        lead.setCreatedAt(LocalDateTime.now());
        lead.setUpdatedAt(LocalDateTime.now());
        
        Lead savedLead = leadRepository.save(lead);
        log.info("Lead creado exitosamente - ID: {}, Nombre: {}, Negocio: {}", 
                savedLead.getId(), savedLead.getFullName(), savedLead.getBusinessId());
        
        return savedLead;
    }

    /**
     * Actualiza un lead existente.
     */
    @Transactional
    public Lead updateLead(Long id, Lead leadDetails) {
        log.info("Actualizando lead con ID: {}", id);
        
        Lead lead = leadRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lead no encontrado con ID: " + id));
        
        // Validate updated data
        leadValidator.validateLeadData(leadDetails.getFirstName(), leadDetails.getLastName(), 
                                       leadDetails.getPhone(), leadDetails.getEmail());
        
        if (leadDetails.getStatus() != null) {
            leadValidator.validateStatus(leadDetails.getStatus());
        }
        
        // Update fields
        lead.setFirstName(leadDetails.getFirstName());
        lead.setLastName(leadDetails.getLastName());
        lead.setPhone(leadDetails.getPhone());
        lead.setEmail(leadDetails.getEmail());
        lead.setCity(leadDetails.getCity());
        lead.setCountry(leadDetails.getCountry());
        lead.setValueEstimated(leadDetails.getValueEstimated());
        lead.setNotes(leadDetails.getNotes());
        lead.setStatus(leadDetails.getStatus());
        lead.setUpdatedAt(LocalDateTime.now());
        
        return leadRepository.save(lead);
    }

    /**
     * Actualiza el estado de un lead.
     */
    @Transactional
    public Lead updateLeadStatus(Long id, String newStatus) {
        log.info("Actualizando estado del lead {} a: {}", id, newStatus);
        
        leadValidator.validateStatus(newStatus);
        
        Lead lead = leadRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lead no encontrado con ID: " + id));
        
        lead.setStatus(newStatus);
        lead.setUpdatedAt(LocalDateTime.now());
        
        return leadRepository.save(lead);
    }

    /**
     * Registra el consentimiento de un lead.
     */
    @Transactional
    public Lead giveConsent(Long id) {
        log.info("Registrando consentimiento para lead ID: {}", id);
        
        Lead lead = leadRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lead no encontrado con ID: " + id));
        
        lead.setConsentGiven(true);
        lead.setConsentDate(LocalDateTime.now());
        lead.setUpdatedAt(LocalDateTime.now());
        
        log.info("Consentimiento registrado para lead: {}", lead.getFullName());
        return leadRepository.save(lead);
    }

    /**
     * Obtiene todos los leads.
     */
    @Transactional(readOnly = true)
    public List<Lead> getAllLeads() {
        log.debug("Obteniendo todos los leads");
        return leadRepository.findAll();
    }

    /**
     * Obtiene un lead por ID.
     */
    @Transactional(readOnly = true)
    public Optional<Lead> getLeadById(Long id) {
        log.debug("Obteniendo lead con ID: {}", id);
        return leadRepository.findById(id);
    }

    /**
     * Obtiene leads de un negocio específico.
     */
    @Transactional(readOnly = true)
    public List<Lead> getLeadsByBusiness(Long businessId) {
        log.debug("Obteniendo leads del negocio: {}", businessId);
        return leadRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);
    }

    /**
     * Obtiene leads por estado.
     */
    @Transactional(readOnly = true)
    public List<Lead> getLeadsByStatus(String status) {
        log.debug("Obteniendo leads con estado: {}", status);
        return leadRepository.findByStatus(status);
    }

    /**
     * Obtiene leads activos (no archivados ni perdidos).
     */
    @Transactional(readOnly = true)
    public List<Lead> getActiveLeads() {
        log.debug("Obteniendo leads activos");
        return leadRepository.findActiveLeads();
    }

    /**
     * Obtiene leads activos de un negocio.
     */
    @Transactional(readOnly = true)
    public List<Lead> getActiveLeadsByBusiness(Long businessId) {
        log.debug("Obteniendo leads activos del negocio: {}", businessId);
        return leadRepository.findActiveLeadsByBusiness(businessId);
    }

    /**
     * Obtiene leads nuevos (status = 'new').
     */
    @Transactional(readOnly = true)
    public List<Lead> getNewLeads() {
        log.debug("Obteniendo leads nuevos");
        return leadRepository.findNewLeads();
    }

    /**
     * Obtiene leads por fuente.
     */
    @Transactional(readOnly = true)
    public List<Lead> getLeadsBySource(String source) {
        log.debug("Obteniendo leads de fuente: {}", source);
        return leadRepository.findBySource(source);
    }

    /**
     * Busca un lead por user_wa_id (WhatsApp/Telegram ID).
     */
    @Transactional(readOnly = true)
    public Optional<Lead> getLeadByUserWaId(String userWaId) {
        log.debug("Buscando lead con user_wa_id: {}", userWaId);
        return leadRepository.findByUserWaId(userWaId);
    }

    /**
     * Busca leads por nombre.
     */
    @Transactional(readOnly = true)
    public List<Lead> searchLeadsByName(String searchTerm) {
        log.debug("Buscando leads por nombre: {}", searchTerm);
        return leadRepository.searchByName(searchTerm);
    }

    /**
     * Obtiene leads en un rango de fechas.
     */
    @Transactional(readOnly = true)
    public List<Lead> getLeadsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Obteniendo leads entre {} y {}", startDate, endDate);
        return leadRepository.findLeadsByDateRange(startDate, endDate);
    }

    /**
     * Cuenta leads por estado.
     */
    @Transactional(readOnly = true)
    public long countLeadsByStatus(String status) {
        return leadRepository.countByStatus(status);
    }

    /**
     * Cuenta leads por fuente.
     */
    @Transactional(readOnly = true)
    public long countLeadsBySource(String source) {
        return leadRepository.countBySource(source);
    }

    /**
     * Obtiene leads sin consentimiento.
     */
    @Transactional(readOnly = true)
    public List<Lead> getLeadsWithoutConsent() {
        log.debug("Obteniendo leads sin consentimiento");
        return leadRepository.findLeadsWithoutConsent();
    }

    /**
     * Elimina un lead (soft delete - marca como archived).
     */
    @Transactional
    public void deleteLead(Long id) {
        log.info("Archivando lead con ID: {}", id);
        
        Lead lead = leadRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lead no encontrado con ID: " + id));
        
        lead.setStatus("archived");
        lead.setUpdatedAt(LocalDateTime.now());
        leadRepository.save(lead);
        
        log.info("Lead archivado: {}", lead.getFullName());
    }

    /**
     * Elimina permanentemente un lead (hard delete).
     */
    @Transactional
    public void hardDeleteLead(Long id) {
        log.warn("Eliminación permanente de lead con ID: {}", id);
        leadRepository.deleteById(id);
    }
}
