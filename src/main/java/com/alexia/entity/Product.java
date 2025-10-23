package com.alexia.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entidad que representa un producto en el catálogo.
 * Diseño universal para cualquier tipo de negocio (plásticos, moda, comida, ferretería).
 * Usa JSONB para variants y metadata para máxima flexibilidad.
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_products_business_id", columnList = "business_id"),
    @Index(name = "idx_products_category", columnList = "category"),
    @Index(name = "idx_products_is_active", columnList = "is_active"),
    @Index(name = "idx_products_name", columnList = "name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El negocio es obligatorio")
    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    private Business business;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @PositiveOrZero(message = "El precio debe ser mayor o igual a cero")
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Size(max = 100, message = "La categoría no puede exceder 100 caracteres")
    @Column(length = 100)
    private String category;

    /**
     * Array de URLs de imágenes del producto.
     * Permite múltiples fotos por producto.
     * Ejemplo: ["https://storage.com/img1.jpg", "https://storage.com/img2.jpg"]
     */
    @Column(columnDefinition = "TEXT[]")
    private String[] images;

    /**
     * Variantes del producto en formato JSONB.
     * Flexible para cualquier tipo de negocio:
     * - Plásticos: {"materials": ["PP", "PET"], "capacities": ["500ml", "1L"]}
     * - Moda: {"sizes": ["S", "M", "L"], "colors": ["Rojo", "Azul"]}
     * - Ferretería: {"dimensions": ["10cm", "20cm"], "materials": ["Acero", "Aluminio"]}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> variants = new HashMap<>();

    @PositiveOrZero(message = "El stock debe ser mayor o igual a cero")
    @Column(nullable = false)
    private Integer stock = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Metadata extensible para features futuras.
     * Permite agregar campos sin cambiar el schema.
     * Ejemplo: {"seo_keywords": ["vasos", "plástico"], "supplier_id": 123, "barcode": "7501234567890"}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata = new HashMap<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (stock == null) {
            stock = 0;
        }
        if (variants == null) {
            variants = new HashMap<>();
        }
        if (metadata == null) {
            metadata = new HashMap<>();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Obtiene la primera imagen del producto o null si no hay imágenes.
     */
    public String getFirstImage() {
        return (images != null && images.length > 0) ? images[0] : null;
    }

    /**
     * Obtiene todas las imágenes como lista.
     */
    public List<String> getImagesList() {
        List<String> list = new ArrayList<>();
        if (images != null) {
            for (String img : images) {
                list.add(img);
            }
        }
        return list;
    }

    /**
     * Verifica si el producto tiene stock disponible.
     */
    public boolean hasStock() {
        return stock != null && stock > 0;
    }

    /**
     * Verifica si el producto está activo y disponible.
     */
    public boolean isAvailable() {
        return isActive != null && isActive && hasStock();
    }

    /**
     * Formatea el precio con símbolo de moneda.
     */
    public String getFormattedPrice() {
        if (price == null) {
            return "N/A";
        }
        return String.format("$%,.0f COP", price);
    }

    /**
     * Retorna una representación en texto del producto.
     */
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        
        if (price != null) {
            sb.append(" - ").append(getFormattedPrice());
        }
        
        if (stock != null) {
            sb.append(" - Stock: ").append(stock);
        }
        
        return sb.toString();
    }

    /**
     * Obtiene una variante específica del producto.
     */
    public Object getVariant(String key) {
        return variants != null ? variants.get(key) : null;
    }

    /**
     * Establece una variante del producto.
     */
    public void setVariant(String key, Object value) {
        if (variants == null) {
            variants = new HashMap<>();
        }
        variants.put(key, value);
    }

    /**
     * Obtiene un metadato específico.
     */
    public Object getMetadataValue(String key) {
        return metadata != null ? metadata.get(key) : null;
    }

    /**
     * Establece un metadato.
     */
    public void setMetadataValue(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }
}
