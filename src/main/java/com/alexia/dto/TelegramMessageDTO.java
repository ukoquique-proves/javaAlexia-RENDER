package com.alexia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para transferir información de mensajes de Telegram entre capas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramMessageDTO {
    
    private Long chatId;
    private String userName;
    private String firstName;
    private String lastName;
    private String messageText;
    private String botResponse;
    private LocalDateTime timestamp;
    
    /**
     * Obtiene el nombre completo del usuario.
     */
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        if (firstName != null) {
            fullName.append(firstName);
        }
        if (lastName != null) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(lastName);
        }
        return fullName.length() > 0 ? fullName.toString() : userName;
    }
}
