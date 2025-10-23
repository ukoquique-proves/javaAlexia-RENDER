package com.alexia.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un comando ejecutado en el bot de Telegram.
 */
@Entity
@Table(name = "bot_commands")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El chat ID no puede ser nulo")
    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @NotNull(message = "El comando no puede ser nulo")
    @Size(max = 50, message = "El comando no puede exceder 50 caracteres")
    @Column(name = "command", nullable = false, length = 50)
    private String command;

    @Size(max = 255, message = "El username no puede exceder 255 caracteres")
    @Column(name = "user_name")
    private String userName;

    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    @Column(name = "first_name")
    private String firstName;

    @NotNull(message = "La fecha de creación no puede ser nula")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
