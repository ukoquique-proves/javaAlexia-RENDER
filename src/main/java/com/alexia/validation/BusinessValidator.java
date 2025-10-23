package com.alexia.validation;

import com.alexia.exception.InvalidProductDataException;
import com.alexia.entity.Business;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validador para entidad Business.
 * Contiene reglas de negocio complejas para negocios.
 */
@Component
public class BusinessValidator {

    private static final int MIN_NAME_LENGTH = 3;
    private static final int MAX_NAME_LENGTH = 255;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9\\s\\-()]{7,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern INSTAGRAM_PATTERN = Pattern.compile("^@?[A-Za-z0-9._]{1,30}$");
    private static final BigDecimal MIN_RATING = BigDecimal.ZERO;
    private static final BigDecimal MAX_RATING = new BigDecimal("5.00");

    /**
     * Valida un negocio antes de crearlo o actualizarlo.
     *
     * @param business Negocio a validar
     * @throws InvalidProductDataException si los datos son inválidos
     */
    public void validate(Business business) {
        if (business == null) {
            throw new InvalidProductDataException("El negocio no puede ser nulo");
        }

        validateName(business.getName());
        validateCategory(business.getCategory());
        validatePhone(business.getPhone());
        validateWhatsApp(business.getWhatsapp());
        validateInstagram(business.getInstagram());
        validateRating(business.getRating());
        validateBusinessHours(business.getBusinessHours());
        validateLocation(business.getLocation());
    }

    /**
     * Valida el nombre del negocio.
     */
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidProductDataException("El nombre del negocio es obligatorio");
        }

        String trimmedName = name.trim();
        if (trimmedName.length() < MIN_NAME_LENGTH) {
            throw new InvalidProductDataException(
                String.format("El nombre debe tener al menos %d caracteres", MIN_NAME_LENGTH)
            );
        }

        if (trimmedName.length() > MAX_NAME_LENGTH) {
            throw new InvalidProductDataException(
                String.format("El nombre no puede exceder %d caracteres", MAX_NAME_LENGTH)
            );
        }
    }

    /**
     * Valida la categoría del negocio.
     */
    private void validateCategory(String category) {
        if (category != null && !category.trim().isEmpty()) {
            if (category.length() > 100) {
                throw new InvalidProductDataException("La categoría no puede exceder 100 caracteres");
            }
        }
    }

    /**
     * Valida el teléfono del negocio.
     */
    private void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return; // Phone is optional
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new InvalidProductDataException(
                "Formato de teléfono inválido. Use formato internacional: +57 300 1234567"
            );
        }
    }

    /**
     * Valida el WhatsApp del negocio.
     */
    private void validateWhatsApp(String whatsapp) {
        if (whatsapp == null || whatsapp.trim().isEmpty()) {
            return; // WhatsApp is optional
        }

        if (!PHONE_PATTERN.matcher(whatsapp).matches()) {
            throw new InvalidProductDataException(
                "Formato de WhatsApp inválido. Use formato internacional: +57 300 1234567"
            );
        }
    }

    /**
     * Valida el Instagram del negocio.
     */
    private void validateInstagram(String instagram) {
        if (instagram == null || instagram.trim().isEmpty()) {
            return; // Instagram is optional
        }

        String handle = instagram.trim();
        if (!INSTAGRAM_PATTERN.matcher(handle).matches()) {
            throw new InvalidProductDataException(
                "Formato de Instagram inválido. Use formato: @usuario o usuario"
            );
        }
    }

    /**
     * Valida la calificación del negocio.
     */
    private void validateRating(BigDecimal rating) {
        if (rating == null) {
            return; // Rating is optional
        }

        if (rating.compareTo(MIN_RATING) < 0) {
            throw new InvalidProductDataException("La calificación no puede ser negativa");
        }

        if (rating.compareTo(MAX_RATING) > 0) {
            throw new InvalidProductDataException(
                String.format("La calificación no puede exceder %.2f", MAX_RATING)
            );
        }

        // Check decimal places (max 2)
        if (rating.scale() > 2) {
            throw new InvalidProductDataException("La calificación solo puede tener 2 decimales");
        }
    }

    /**
     * Valida los horarios de negocio.
     */
    private void validateBusinessHours(Map<String, Object> businessHours) {
        if (businessHours == null || businessHours.isEmpty()) {
            return; // Business hours are optional
        }

        String[] validDays = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
        
        for (String key : businessHours.keySet()) {
            boolean isValidDay = false;
            for (String validDay : validDays) {
                if (validDay.equalsIgnoreCase(key)) {
                    isValidDay = true;
                    break;
                }
            }
            
            if (!isValidDay) {
                throw new InvalidProductDataException(
                    String.format("Día inválido en horarios: '%s'. Use: monday, tuesday, etc.", key)
                );
            }
        }
    }

    /**
     * Valida la ubicación del negocio.
     */
    private void validateLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return; // Location is optional
        }

        // Validate WKT format: POINT(longitude latitude)
        if (!location.matches("^POINT\\([-+]?[0-9]*\\.?[0-9]+\\s+[-+]?[0-9]*\\.?[0-9]+\\)$")) {
            throw new InvalidProductDataException(
                "Formato de ubicación inválido. Use formato WKT: POINT(longitude latitude)"
            );
        }

        // Extract and validate coordinates
        String coords = location.substring(6, location.length() - 1); // Remove "POINT(" and ")"
        String[] parts = coords.split("\\s+");
        
        if (parts.length != 2) {
            throw new InvalidProductDataException("La ubicación debe tener longitud y latitud");
        }

        try {
            double longitude = Double.parseDouble(parts[0]);
            double latitude = Double.parseDouble(parts[1]);

            // Validate coordinate ranges
            if (longitude < -180 || longitude > 180) {
                throw new InvalidProductDataException("Longitud inválida. Debe estar entre -180 y 180");
            }

            if (latitude < -90 || latitude > 90) {
                throw new InvalidProductDataException("Latitud inválida. Debe estar entre -90 y 90");
            }
        } catch (NumberFormatException e) {
            throw new InvalidProductDataException("Coordenadas inválidas en la ubicación");
        }
    }

    /**
     * Valida que un negocio esté activo.
     */
    public void validateIsActive(Business business) {
        if (business == null) {
            throw new InvalidProductDataException("El negocio no puede ser nulo");
        }

        if (business.getIsActive() == null || !business.getIsActive()) {
            throw new InvalidProductDataException(
                String.format("El negocio '%s' no está activo", business.getName())
            );
        }
    }

    /**
     * Valida que un negocio esté verificado.
     */
    public void validateIsVerified(Business business) {
        if (business == null) {
            throw new InvalidProductDataException("El negocio no puede ser nulo");
        }

        if (business.getIsVerified() == null || !business.getIsVerified()) {
            throw new InvalidProductDataException(
                String.format("El negocio '%s' no está verificado", business.getName())
            );
        }
    }

    /**
     * Valida un email (para futuras funcionalidades).
     */
    public void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return; // Email is optional
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidProductDataException("Formato de email inválido");
        }
    }
}
