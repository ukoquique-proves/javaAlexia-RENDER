package com.alexia.validation;

import com.alexia.entity.Product;
import com.alexia.exception.InvalidProductDataException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Validador para entidad Product.
 * Contiene reglas de negocio complejas que van más allá de las validaciones JPA.
 */
@Component
public class ProductValidator {

    private static final int MAX_VARIANTS = 50;
    private static final int MAX_IMAGES = 10;
    private static final BigDecimal MAX_PRICE = new BigDecimal("999999999.99");
    private static final int MAX_STOCK = 1000000;
    private static final int MIN_NAME_LENGTH = 3;
    private static final int MAX_NAME_LENGTH = 255;

    /**
     * Valida un producto antes de crearlo o actualizarlo.
     *
     * @param product Producto a validar
     * @throws InvalidProductDataException si los datos son inválidos
     */
    public void validate(Product product) {
        if (product == null) {
            throw new InvalidProductDataException("El producto no puede ser nulo");
        }

        validateName(product.getName());
        validatePrice(product.getPrice());
        validateStock(product.getStock());
        validateImages(product.getImages());
        validateVariants(product.getVariants());
        validateBusinessReference(product.getBusinessId());
    }

    /**
     * Valida el nombre del producto.
     */
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidProductDataException("El nombre del producto es obligatorio");
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
     * Valida el precio del producto.
     */
    private void validatePrice(BigDecimal price) {
        if (price == null) {
            return; // Price is optional
        }

        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidProductDataException("El precio no puede ser negativo");
        }

        if (price.compareTo(MAX_PRICE) > 0) {
            throw new InvalidProductDataException(
                String.format("El precio no puede exceder $%s COP", MAX_PRICE)
            );
        }
    }

    /**
     * Valida el stock del producto.
     */
    private void validateStock(Integer stock) {
        if (stock == null) {
            return; // Stock is optional
        }

        if (stock < 0) {
            throw new InvalidProductDataException("El stock no puede ser negativo");
        }

        if (stock > MAX_STOCK) {
            throw new InvalidProductDataException(
                String.format("El stock no puede exceder %d unidades", MAX_STOCK)
            );
        }
    }

    /**
     * Valida las imágenes del producto.
     */
    private void validateImages(String[] images) {
        if (images == null || images.length == 0) {
            return; // Images are optional
        }

        if (images.length > MAX_IMAGES) {
            throw new InvalidProductDataException(
                String.format("Un producto no puede tener más de %d imágenes", MAX_IMAGES)
            );
        }

        // Validate each image URL
        for (String imageUrl : images) {
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                validateImageUrl(imageUrl);
            }
        }
    }

    /**
     * Valida una URL de imagen.
     */
    private void validateImageUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new InvalidProductDataException(
                "Las URLs de imágenes deben comenzar con http:// o https://"
            );
        }

        if (url.length() > 500) {
            throw new InvalidProductDataException("La URL de la imagen es demasiado larga (máx. 500 caracteres)");
        }
    }

    /**
     * Valida las variantes del producto.
     */
    private void validateVariants(Object variants) {
        if (variants == null) {
            return; // Variants are optional
        }

        // If variants is a Map, check size
        if (variants instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> variantsMap = (java.util.Map<String, Object>) variants;
            
            if (variantsMap.size() > MAX_VARIANTS) {
                throw new InvalidProductDataException(
                    String.format("Un producto no puede tener más de %d tipos de variantes", MAX_VARIANTS)
                );
            }
        }
    }

    /**
     * Valida que el producto tenga una referencia a un negocio.
     */
    private void validateBusinessReference(Long businessId) {
        if (businessId == null) {
            throw new InvalidProductDataException("El producto debe estar asociado a un negocio");
        }

        if (businessId <= 0) {
            throw new InvalidProductDataException("ID de negocio inválido");
        }
    }

    /**
     * Valida una actualización de stock.
     */
    public void validateStockUpdate(Integer currentStock, Integer newStock) {
        if (newStock == null) {
            throw new InvalidProductDataException("El nuevo stock no puede ser nulo");
        }

        validateStock(newStock);

        // Business rule: Can't reduce stock below 0
        if (newStock < 0) {
            throw new InvalidProductDataException("No se puede establecer un stock negativo");
        }
    }

    /**
     * Valida que un producto esté activo antes de ciertas operaciones.
     */
    public void validateIsActive(Product product) {
        if (product == null) {
            throw new InvalidProductDataException("El producto no puede ser nulo");
        }

        if (product.getIsActive() == null || !product.getIsActive()) {
            throw new InvalidProductDataException(
                String.format("El producto '%s' no está activo", product.getName())
            );
        }
    }
}
