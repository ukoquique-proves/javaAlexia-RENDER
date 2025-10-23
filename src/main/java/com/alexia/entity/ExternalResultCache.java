package com.alexia.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "external_results_cache")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalResultCache {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "query_hash", nullable = false)
    private String queryHash;
    
    @Column(name = "source")
    private String source;
    
    @Column(name = "source_place_id")
    private String sourcePlaceId;
    
    @Column(name = "business_name")
    private String businessName;
    
    @Column(name = "category")
    private String category;
    
    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;
    
    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;
    
    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "confidence", precision = 3, scale = 2)
    private BigDecimal confidence;
    
    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;
    
    @Column(name = "ttl")
    private Integer ttl;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (fetchedAt == null) {
            fetchedAt = LocalDateTime.now();
        }
        if (ttl == null) {
            ttl = 86400; // 24 hours default
        }
    }
}
