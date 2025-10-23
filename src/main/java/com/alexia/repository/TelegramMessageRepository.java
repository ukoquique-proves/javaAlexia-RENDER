package com.alexia.repository;

import com.alexia.entity.TelegramMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para operaciones de base de datos con mensajes de Telegram.
 */
@Repository
public interface TelegramMessageRepository extends JpaRepository<TelegramMessage, Long> {
    
    /**
     * Encuentra mensajes por chat ID ordenados por fecha descendente.
     */
    List<TelegramMessage> findByChatIdOrderByCreatedAtDesc(Long chatId);
    
    /**
     * Cuenta mensajes por chat ID.
     */
    long countByChatId(Long chatId);
    
    /**
     * Encuentra mensajes entre dos fechas ordenados por fecha descendente.
     */
    List<TelegramMessage> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
    
    /**
     * Encuentra todos los mensajes ordenados por fecha descendente.
     */
    List<TelegramMessage> findAllByOrderByCreatedAtDesc();
}
