package com.alexia.config;

import com.alexia.repository.BotCommandRepository;
import com.alexia.repository.TelegramMessageRepository;
import com.alexia.service.BusinessService;
import com.alexia.service.GrokService;
import com.alexia.service.LeadService;
import com.alexia.service.ProductService;
import com.alexia.service.SupplierService;
import com.alexia.service.TelegramService;
import com.alexia.service.GeolocationService;
import com.alexia.service.RagSearchService;
import com.alexia.telegram.AlexiaTelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Configuración del bot de Telegram.
 * Inicializa y configura el bot con sus dependencias.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class TelegramBotConfig implements ApplicationListener<ContextRefreshedEvent> {
    
    private final TelegramService telegramService;
    private final BotCommandRepository botCommandRepository;
    private final TelegramMessageRepository telegramMessageRepository;
    private final GrokService grokService;
    private final BusinessService businessService;
    private final ProductService productService;
    private final LeadService leadService;
    private final GeolocationService geolocationService;
    private final SupplierService supplierService;
    private final RagSearchService ragSearchService;
    
    @Value("${telegram.bot.token}")
    private String botToken;
    
    @Value("${telegram.bot.username}")
    private String botUsername;
    
    @Bean
    public AlexiaTelegramBot alexiaTelegramBot() {
        AlexiaTelegramBot bot = new AlexiaTelegramBot(
                botToken, 
                botUsername,
                telegramService,
                botCommandRepository,
                telegramMessageRepository,
                grokService,
                businessService,
                productService,
                leadService,
                geolocationService,
                supplierService,
                ragSearchService
        );
        
        // Eliminar webhook si existe (para usar long polling)
        bot.deleteWebhook();
        
        return bot;
    }
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            AlexiaTelegramBot bot = event.getApplicationContext().getBean(AlexiaTelegramBot.class);
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            bot.setActive(true);
            log.info("✅ Bot de Telegram registrado y activado - username=@{}", bot.getBotUsername());

            // Add shutdown hook to properly disconnect bot
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("🛑 Shutting down Telegram bot...");
                bot.setActive(false);
                try {
                    Thread.sleep(1000); // Give time for graceful shutdown
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                log.info("✅ Telegram bot disconnected successfully");
            }));

        } catch (TelegramApiException e) {
            log.error("Error al registrar el bot de Telegram", e);
        }
    }
}
