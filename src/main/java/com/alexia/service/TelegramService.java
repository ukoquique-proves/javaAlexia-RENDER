package com.alexia.service;

import com.alexia.dto.TelegramMessageDTO;
import com.alexia.entity.TelegramMessage;
import com.alexia.exception.TelegramException;
import com.alexia.repository.TelegramMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar mensajes de Telegram.
 * Maneja la lógica de negocio relacionada con la persistencia de mensajes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramService {

    private final TelegramMessageRepository telegramMessageRepository;

    /**
     * Guarda un mensaje de Telegram en la base de datos.
     *
     * @param dto DTO con la información del mensaje
     * @return El mensaje guardado
     * @throws TelegramException si hay un error al guardar
     */
    @Transactional
    public TelegramMessage saveMessage(TelegramMessageDTO dto) {
        log.debug("Guardando mensaje de Telegram - chatId={}, userName={}", 
                dto.getChatId(), dto.getUserName());
        
        try {
            // Crear entidad directamente desde el DTO
            TelegramMessage message = TelegramMessage.builder()
                    .chatId(dto.getChatId())
                    .userName(dto.getUserName())
                    .firstName(dto.getFirstName())
                    .lastName(dto.getLastName())
                    .messageText(dto.getMessageText())
                    .botResponse(dto.getBotResponse())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            TelegramMessage saved = telegramMessageRepository.save(message);
            
            log.info("Mensaje de Telegram guardado - messageId={}, chatId={}, userName={}, fullName={}", 
                    saved.getId(), saved.getChatId(), dto.getUserName(), dto.getFullName());
            
            return saved;
            
        } catch (DataAccessException e) {
            log.error("Error de acceso a datos al guardar mensaje - chatId={}, exception={}, message={}", 
                    dto.getChatId(), e.getClass().getSimpleName(), e.getMessage());
            throw new TelegramException("Error al guardar mensaje de Telegram", e);
        } catch (Exception e) {
            log.error("Error inesperado al guardar mensaje - chatId={}, exception={}, message={}", 
                    dto.getChatId(), e.getClass().getSimpleName(), e.getMessage(), e);
            throw new TelegramException("Error inesperado al procesar mensaje de Telegram", e);
        }
    }

    /**
     * Obtiene todos los mensajes de un chat específico.
     *
     * @param chatId ID del chat
     * @return Lista de mensajes
     */
    public List<TelegramMessageDTO> getMessagesByChatId(Long chatId) {
        return telegramMessageRepository.findByChatIdOrderByCreatedAtDesc(chatId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los mensajes ordenados por fecha.
     *
     * @return Lista de todos los mensajes
     */
    public List<TelegramMessageDTO> getAllMessages() {
        return telegramMessageRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Cuenta el total de mensajes recibidos.
     *
     * @return Número total de mensajes
     */
    public long getTotalMessageCount() {
        return telegramMessageRepository.count();
    }
    
    /**
     * Convierte una entidad TelegramMessage a DTO.
     *
     * @param message Entidad a convertir
     * @return DTO con los datos del mensaje
     */
    private TelegramMessageDTO convertToDTO(TelegramMessage message) {
        return TelegramMessageDTO.builder()
                .chatId(message.getChatId())
                .userName(message.getUserName())
                .firstName(message.getFirstName())
                .lastName(message.getLastName())
                .messageText(message.getMessageText())
                .botResponse(message.getBotResponse())
                .build();
    }
}
