package com.alexia.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un registro de prueba de conexión a la base de datos.
 */
@Entity
@Table(name = "connection_test")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El mensaje no puede estar vacío")
    @Size(max = 255, message = "El mensaje no puede exceder 255 caracteres")
    @Column(nullable = false)
    private String message;

    @PastOrPresent(message = "La fecha no puede ser futura")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ConnectionTest(String message) {
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
