package com.alexia.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un mensaje de Telegram almacenado en la base de datos.
 */
@Entity
@Table(name = "telegram_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El chat ID no puede ser nulo")
    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Size(max = 255, message = "El username no puede exceder 255 caracteres")
    @Column(name = "user_name")
    private String userName;

    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    @Column(name = "first_name")
    private String firstName;

    @Size(max = 255, message = "El apellido no puede exceder 255 caracteres")
    @Column(name = "last_name")
    private String lastName;

    @Column(name = "message_text", columnDefinition = "TEXT")
    private String messageText;

    @Column(name = "bot_response", columnDefinition = "TEXT")
    private String botResponse;

    @NotNull(message = "La fecha de creación no puede ser nula")
    @PastOrPresent(message = "La fecha no puede ser futura")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
