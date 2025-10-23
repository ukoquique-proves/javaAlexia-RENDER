package com.alexia.validation;

import com.alexia.exception.InvalidProductDataException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Validador para entidad Lead (futuro Step 11).
 * Contiene reglas de negocio complejas para captura de leads.
 */
@Component
public class LeadValidator {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9\\s\\-()]{7,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 100;

    /**
     * Valida datos básicos de un lead.
     *
     * @param firstName Nombre
     * @param lastName Apellido
     * @param phone Teléfono
     * @param email Email
     * @throws InvalidProductDataException si los datos son inválidos
     */
    public void validateLeadData(String firstName, String lastName, String phone, String email) {
        validateName(firstName, "nombre");
        
        if (lastName != null && !lastName.trim().isEmpty()) {
            validateName(lastName, "apellido");
        }

        if (phone != null && !phone.trim().isEmpty()) {
            validatePhone(phone);
        }

        if (email != null && !email.trim().isEmpty()) {
            validateEmail(email);
        }

        // At least one contact method is required
        if ((phone == null || phone.trim().isEmpty()) && (email == null || email.trim().isEmpty())) {
            throw new InvalidProductDataException(
                "Se requiere al menos un método de contacto (teléfono o email)"
            );
        }
    }

    /**
     * Valida el nombre o apellido.
     */
    private void validateName(String name, String fieldName) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidProductDataException(
                String.format("El %s es obligatorio", fieldName)
            );
        }

        String trimmedName = name.trim();
        if (trimmedName.length() < MIN_NAME_LENGTH) {
            throw new InvalidProductDataException(
                String.format("El %s debe tener al menos %d caracteres", fieldName, MIN_NAME_LENGTH)
            );
        }

        if (trimmedName.length() > MAX_NAME_LENGTH) {
            throw new InvalidProductDataException(
                String.format("El %s no puede exceder %d caracteres", fieldName, MAX_NAME_LENGTH)
            );
        }

        // Only letters, spaces, and common name characters
        if (!trimmedName.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s'-]+$")) {
            throw new InvalidProductDataException(
                String.format("El %s contiene caracteres inválidos", fieldName)
            );
        }
    }

    /**
     * Valida el teléfono.
     */
    private void validatePhone(String phone) {
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new InvalidProductDataException(
                "Formato de teléfono inválido. Use formato internacional: +57 300 1234567"
            );
        }
    }

    /**
     * Valida el email.
     */
    private void validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidProductDataException("Formato de email inválido");
        }

        if (email.length() > 255) {
            throw new InvalidProductDataException("El email es demasiado largo (máx. 255 caracteres)");
        }
    }

    /**
     * Valida el consentimiento del lead.
     * Esta es una validación crítica para GDPR/LGPD compliance.
     *
     * @param consentGiven Si el usuario dio consentimiento
     * @throws InvalidProductDataException si no hay consentimiento
     */
    public void validateConsent(Boolean consentGiven) {
        if (consentGiven == null || !consentGiven) {
            throw new InvalidProductDataException(
                "Se requiere consentimiento explícito del usuario para capturar sus datos"
            );
        }
    }

    /**
     * Valida la fuente del lead.
     *
     * @param source Fuente del lead (telegram, whatsapp, web, organic)
     * @throws InvalidProductDataException si la fuente es inválida
     */
    public void validateSource(String source) {
        if (source == null || source.trim().isEmpty()) {
            throw new InvalidProductDataException("La fuente del lead es obligatoria");
        }

        String[] validSources = {"telegram", "whatsapp", "web", "organic", "data_alexia"};
        boolean isValid = false;
        
        for (String validSource : validSources) {
            if (validSource.equalsIgnoreCase(source)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            throw new InvalidProductDataException(
                "Fuente inválida. Use: telegram, whatsapp, web, organic, o data_alexia"
            );
        }
    }

    /**
     * Valida el ID de usuario de WhatsApp/Telegram.
     *
     * @param userWaId ID de usuario
     * @throws InvalidProductDataException si el ID es inválido
     */
    public void validateUserWaId(String userWaId) {
        if (userWaId == null || userWaId.trim().isEmpty()) {
            throw new InvalidProductDataException("El ID de usuario es obligatorio");
        }

        // Must be numeric for Telegram/WhatsApp IDs
        if (!userWaId.matches("^[0-9]+$")) {
            throw new InvalidProductDataException(
                "El ID de usuario debe ser numérico (Telegram/WhatsApp ID)"
            );
        }
    }

    /**
     * Valida la referencia al negocio.
     *
     * @param businessId ID del negocio
     * @throws InvalidProductDataException si el ID es inválido
     */
    public void validateBusinessReference(Long businessId) {
        if (businessId == null) {
            throw new InvalidProductDataException("El lead debe estar asociado a un negocio");
        }

        if (businessId <= 0) {
            throw new InvalidProductDataException("ID de negocio inválido");
        }
    }

    /**
     * Valida el valor estimado del lead.
     *
     * @param valueEstimated Valor estimado en COP
     * @throws InvalidProductDataException si el valor es inválido
     */
    public void validateValueEstimated(java.math.BigDecimal valueEstimated) {
        if (valueEstimated == null) {
            return; // Value is optional
        }

        if (valueEstimated.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new InvalidProductDataException("El valor estimado no puede ser negativo");
        }

        if (valueEstimated.compareTo(new java.math.BigDecimal("999999999.99")) > 0) {
            throw new InvalidProductDataException("El valor estimado es demasiado alto");
        }
    }

    /**
     * Valida el estado del lead.
     *
     * @param status Estado del lead
     * @throws InvalidProductDataException si el estado es inválido
     */
    public void validateStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return; // Status has default value
        }

        String[] validStatuses = {"new", "contacted", "qualified", "converted", "lost", "archived"};
        boolean isValid = false;
        
        for (String validStatus : validStatuses) {
            if (validStatus.equalsIgnoreCase(status)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            throw new InvalidProductDataException(
                "Estado inválido. Use: new, contacted, qualified, converted, lost, o archived"
            );
        }
    }
}
