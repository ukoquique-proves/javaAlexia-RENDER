package com.alexia.service;

import com.alexia.telegram.AlexiaTelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Servicio para gestionar el ciclo de vida del bot de Telegram.
 * Permite iniciar, detener y consultar el estado del bot de forma controlada.
 */
@Service
@Slf4j
public class BotManagerService {

    private final AlexiaTelegramBot alexiaTelegramBot;
    private TelegramBotsApi botsApi;
    private boolean isBotRegistered = false;

    public BotManagerService(AlexiaTelegramBot alexiaTelegramBot) {
        this.alexiaTelegramBot = alexiaTelegramBot;
        log.info("BotManagerService inicializado - El bot está listo para ser iniciado manualmente");
    }

    /**
     * Inicia el bot de Telegram.
     * Registra el bot en la API de Telegram y lo activa para procesar mensajes.
     * 
     * @throws IllegalStateException si el bot ya está en ejecución
     */
    public synchronized void startBot() {
        if (isBotRunning()) {
            log.warn("Intento de iniciar el bot cuando ya está en ejecución");
            throw new IllegalStateException("El bot ya está en ejecución");
        }

        try {
            log.info("🚀 Iniciando el bot de Telegram...");
            
            // Registrar el bot si no está registrado
            if (!isBotRegistered) {
                botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(alexiaTelegramBot);
                isBotRegistered = true;
                log.info("✅ Bot registrado en la API de Telegram");
            }
            
            // Eliminar webhook si existe (necesario para usar long polling)
            log.info("🔧 Verificando y eliminando webhook si existe...");
            boolean webhookDeleted = alexiaTelegramBot.deleteWebhook();
            if (!webhookDeleted) {
                log.warn("⚠️ No se pudo eliminar el webhook, pero continuando con el inicio");
            }
            
            // Activar el bot para que procese mensajes
            alexiaTelegramBot.setActive(true);
            isBotRegistered = true; // Marcar como registrado
            
            log.info("✅ Bot de Telegram iniciado correctamente - username=@{}", 
                    alexiaTelegramBot.getBotUsername());
            
        } catch (TelegramApiException e) {
            log.error("❌ Error al iniciar el bot de Telegram: {}", e.getMessage(), e);
            isBotRegistered = false;
            alexiaTelegramBot.setActive(false);
            throw new RuntimeException("Error al iniciar el bot: " + e.getMessage(), e);
        }
    }

    /**
     * Detiene el bot de Telegram.
     * El bot deja de procesar mensajes pero mantiene su registro en la API.
     * 
     * @throws IllegalStateException si el bot no está en ejecución
     */
    public synchronized void stopBot() {
        if (!isBotRunning()) {
            log.warn("Intento de detener el bot cuando no está en ejecución");
            throw new IllegalStateException("El bot no está en ejecución");
        }

        log.info("🛑 Deteniendo el bot de Telegram...");
        
        // Desactivar el bot para que no procese más mensajes
        alexiaTelegramBot.setActive(false);
        
        log.info("✅ Bot de Telegram detenido - No procesará nuevos mensajes");
        log.info("ℹ️  El bot permanece registrado y puede ser reiniciado en cualquier momento");
    }

    /**
     * Verifica si el bot está en ejecución.
     * 
     * @return true si el bot está registrado y activo, false en caso contrario
     */
    public boolean isBotRunning() {
        // El bot puede estar activo aunque no haya sido registrado por este servicio
        // (por ejemplo, si fue auto-registrado en TelegramBotConfig)
        return alexiaTelegramBot.isActive();
    }

    /**
     * Obtiene el estado detallado del bot.
     * 
     * @return String con información del estado del bot
     */
    public String getBotStatus() {
        if (isBotRunning()) {
            return String.format("✅ Bot ACTIVO - @%s procesando mensajes", 
                    alexiaTelegramBot.getBotUsername());
        } else {
            return String.format("⏹️ Bot DETENIDO - @%s no está procesando mensajes", 
                    alexiaTelegramBot.getBotUsername());
        }
    }

    /**
     * Reinicia el bot (detiene y vuelve a iniciar).
     */
    public synchronized void restartBot() {
        log.info("🔄 Reiniciando el bot de Telegram...");
        
        if (isBotRunning()) {
            stopBot();
        }
        
        startBot();
        
        log.info("✅ Bot reiniciado correctamente");
    }
}
