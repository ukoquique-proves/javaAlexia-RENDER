package com.alexia.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un lead capturado del sistema.
 * Incluye gestión de consentimiento GDPR/LGPD y preparación para CRM.
 */
@Entity
@Table(name = "leads", indexes = {
    @Index(name = "idx_leads_business_id", columnList = "business_id"),
    @Index(name = "idx_leads_status", columnList = "status"),
    @Index(name = "idx_leads_user_wa_id", columnList = "user_wa_id"),
    @Index(name = "idx_leads_source", columnList = "source"),
    @Index(name = "idx_leads_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "user_wa_id", length = 50)
    private String userWaId; // WhatsApp ID or Telegram ID

    @Column(name = "campaign_id")
    private Long campaignId; // For future campaigns feature

    @NotBlank(message = "La fuente del lead es obligatoria")
    @Size(max = 50, message = "La fuente no puede exceder 50 caracteres")
    @Column(name = "source", length = 50, nullable = false)
    private String source; // telegram, whatsapp, web, organic, data_alexia

    @Size(max = 50, message = "El estado no puede exceder 50 caracteres")
    @Column(name = "status", length = 50, nullable = false)
    @Builder.Default
    private String status = "new"; // new, contacted, qualified, converted, lost, archived

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(name = "first_name", length = 100, nullable = false)
    private String firstName;

    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    @Column(name = "last_name", length = 100)
    private String lastName;

    @Size(max = 50, message = "El teléfono no puede exceder 50 caracteres")
    @Column(name = "phone", length = 50)
    private String phone;

    @Email(message = "Formato de email inválido")
    @Size(max = 255, message = "El email no puede exceder 255 caracteres")
    @Column(name = "email", length = 255)
    private String email;

    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
    @Column(name = "city", length = 100)
    private String city;

    @Size(max = 50, message = "El país no puede exceder 50 caracteres")
    @Column(name = "country", length = 50)
    @Builder.Default
    private String country = "CO"; // Default Colombia

    @Column(name = "value_estimated", precision = 10, scale = 2)
    private BigDecimal valueEstimated;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // GDPR/LGPD Compliance fields
    @Column(name = "consent_given", nullable = false)
    @Builder.Default
    private Boolean consentGiven = false;

    @Column(name = "consent_date")
    private LocalDateTime consentDate;

    // Future CRM sync fields
    @Size(max = 50, message = "El estado de sincronización no puede exceder 50 caracteres")
    @Column(name = "crm_sync_status", length = 50)
    private String crmSyncStatus; // pending, synced, failed

    @Size(max = 100, message = "El ID de contacto CRM no puede exceder 100 caracteres")
    @Column(name = "crm_contact_id", length = 100)
    private String crmContactId;

    @Size(max = 100, message = "El ID de oportunidad CRM no puede exceder 100 caracteres")
    @Column(name = "crm_opportunity_id", length = 100)
    private String crmOpportunityId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Retorna el nombre completo del lead.
     */
    public String getFullName() {
        if (lastName != null && !lastName.trim().isEmpty()) {
            return firstName + " " + lastName;
        }
        return firstName;
    }

    /**
     * Verifica si el lead tiene al menos un método de contacto.
     */
    public boolean hasContactMethod() {
        return (phone != null && !phone.trim().isEmpty()) || 
               (email != null && !email.trim().isEmpty());
    }

    /**
     * Verifica si el lead está activo (no archivado ni perdido).
     */
    public boolean isActive() {
        return !"archived".equalsIgnoreCase(status) && 
               !"lost".equalsIgnoreCase(status);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
