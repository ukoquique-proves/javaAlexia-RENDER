package com.alexia.telegram;

import com.alexia.constants.BotCommands;
import com.alexia.constants.Messages;
import com.alexia.dto.GrokIntent;
import com.alexia.dto.TelegramMessageDTO;
import com.alexia.entity.BotCommand;
import com.alexia.entity.Business;
import com.alexia.entity.Lead;
import com.alexia.entity.Product;
import com.alexia.repository.BotCommandRepository;
import com.alexia.repository.TelegramMessageRepository;
import com.alexia.service.BusinessService;
import com.alexia.service.GrokService;
import com.alexia.service.LeadService;
import com.alexia.service.ProductService;
import com.alexia.entity.Supplier;
import com.alexia.service.GeolocationService;
import com.alexia.service.SupplierService;
import com.alexia.service.TelegramService;
import com.alexia.service.RagSearchService;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Bot de Telegram para Alexia.
 * Recibe mensajes de usuarios y responde con comandos y funcionalidad de eco.
 */
@Slf4j
public class AlexiaTelegramBot extends TelegramLongPollingBot {
    private final TelegramService telegramService;
    private final BotCommandRepository botCommandRepository;
    private final TelegramMessageRepository telegramMessageRepository;
    private final GrokService grokService;
    private final BusinessService businessService;
    private final ProductService productService;
    private final LeadService leadService;
    private final GeolocationService geolocationService; // Added for geolocation features
    private final SupplierService supplierService; // Added for supplier features
    private final RagSearchService ragSearchService; // Added for RAG search with source citation
    private final String botUsername;
    
    /**
     * Estado del bot: true = activo (procesa mensajes), false = inactivo (ignora mensajes)
     */
    @Getter
    @Setter
    private volatile boolean active = false;

    public AlexiaTelegramBot(String botToken, String botUsername, 
                            TelegramService telegramService,
                            BotCommandRepository botCommandRepository,
                            TelegramMessageRepository telegramMessageRepository,
                            GrokService grokService,
                            BusinessService businessService,
                            ProductService productService,
                            LeadService leadService,
                            GeolocationService geolocationService,
                            SupplierService supplierService,
                            RagSearchService ragSearchService) {
        super(botToken);
        this.botUsername = botUsername;
        this.telegramService = telegramService;
        this.botCommandRepository = botCommandRepository;
        this.telegramMessageRepository = telegramMessageRepository;
        this.grokService = grokService;
        this.businessService = businessService;
        this.productService = productService;
        this.leadService = leadService;
        this.geolocationService = geolocationService;
        this.supplierService = supplierService;
        this.ragSearchService = ragSearchService;
        log.info("Bot de Telegram inicializado con Grok AI, búsqueda de negocios, productos, geolocalización y proveedores - username=@{}", botUsername);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Si el bot está inactivo, no procesar mensajes
        if (!active) {
            log.trace("Bot inactivo, ignorando actualización - chatId={}", 
                    update.hasMessage() ? update.getMessage().getChatId() : "N/A");
            return;
        }
        
        if (update.hasMessage() && update.getMessage().hasText()) {
            processTextMessage(update);
        }
    }

    /**
     * Procesa un mensaje de texto recibido.
     */
    private void processTextMessage(Update update) {
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();
        User user = update.getMessage().getFrom();

        log.info("Mensaje recibido - chatId={}, userName={}, firstName={}, messageLength={}", 
                chatId, user.getUserName(), user.getFirstName(), messageText.length());
        log.debug("Contenido del mensaje - chatId={}, text={}", chatId, messageText);

        String response;
        
        // Handle commands first
        if (messageText.startsWith("/")) {
            response = handleCommand(chatId, user, messageText);
        } else {
            // Use Grok AI to understand natural language intent
            GrokIntent intent = grokService.detectIntent(messageText);
            log.info("Detected Grok Intent: {}", intent);

            // Route based on detected intent with a confidence threshold
            if (intent != null && intent.getConfidence() > 0.75) {
                switch (intent.getIntent()) {
                    case PRODUCT_SEARCH:
                        response = handleProductSearch(intent.getSearchTerm());
                        break;
                    case BUSINESS_SEARCH:
                        response = handleRagBusinessSearch(intent.getSearchTerm());
                        break;
                    case COMPARE_PRICES:
                        response = handlePriceComparison(intent.getSearchTerm());
                        break;
                    case LEAD_CAPTURE:
                        response = handleLeadCapture(chatId, user, intent);
                        break;
                    case GENERAL_QUERY:
                    default:
                        response = generateGrokResponse(chatId, messageText);
                        break;
                }
            } else {
                // If intent is not clear, default to a general conversational response
                response = generateGrokResponse(chatId, messageText);
            }
        }

        // Guardar mensaje en base de datos
        saveMessageToDatabase(chatId, user, messageText, response);

        // Enviar respuesta al usuario
        sendTextMessage(chatId, response);
    }

    /**
     * Maneja los comandos del bot.
     */
    private String handleCommand(Long chatId, User user, String commandText) {
        String command = commandText.split(" ")[0].toLowerCase();
        
        log.info("Comando recibido - chatId={}, command={}, userName={}", 
                chatId, command, user.getUserName());
        
        // Guardar comando en base de datos
        saveCommandToDatabase(chatId, user, command);
        
        // Procesar comando
        return switch (command) {
            case BotCommands.START -> BotCommands.START_MESSAGE;
            case BotCommands.HELP -> generateHelpMessage();
            case BotCommands.STATUS -> generateStatusMessage();
            case "/cerca" -> generateNearbyBusinessesMessage();
            case "/categorias" -> generateCategoriesMessage();
            default -> BotCommands.UNKNOWN_COMMAND_MESSAGE;
        };
    }

    /**
     * Genera el mensaje de estado del bot.
     */
    private String generateStatusMessage() {
        long totalMessages = telegramMessageRepository.count();
        long totalCommands = botCommandRepository.count();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        
        return String.format(BotCommands.STATUS_MESSAGE_TEMPLATE, 
                totalMessages, totalCommands, timestamp);
    }
    
    /**
     * Genera el mensaje de ayuda con todos los comandos disponibles.
     */
    private String generateHelpMessage() {
        StringBuilder helpMessage = new StringBuilder();
        helpMessage.append("🤖 Comandos disponibles:\n\n");
        helpMessage.append("/start - Inicia el bot\n");
        helpMessage.append("/help - Muestra esta ayuda\n");
        helpMessage.append("/status - Muestra el estado del bot\n");
        helpMessage.append("/cerca - Busca negocios cercanos que necesiten productos\n");
        helpMessage.append("/categorias - Muestra categorías de negocios\n\n");
        helpMessage.append("También puedes hacer preguntas naturales como:\n");
        helpMessage.append("• \"¿Quién necesita platos desechables cerca?\"\n");
        helpMessage.append("• \"Busca restaurantes cerca de mi tienda\"\n");
        helpMessage.append("• \"¿Qué cafeterías hay en 1km a la redonda?\"");
        
        return helpMessage.toString();
    }
    
    /**
     * Genera un mensaje con negocios cercanos.
     */
    private String generateNearbyBusinessesMessage() {
        // En una implementación real, aquí se usaría la ubicación del usuario
        // Por ahora usamos coordenadas de ejemplo (Bogotá)
        double defaultLongitude = -74.0721;
        double defaultLatitude = 4.7110;
        int defaultRadius = 3000; // 3km
        
        try {
            List<Business> nearbyBusinesses = geolocationService.findPlasticProductConsumersNearby(
                defaultLongitude, defaultLatitude, defaultRadius);
            
            return geolocationService.formatNearbyBusinessesForTelegram(
                nearbyBusinesses, "productos plásticos", defaultRadius);
        } catch (Exception e) {
            log.error("Error al buscar negocios cercanos", e);
            return "❌ Error al buscar negocios cercanos. Por favor intenta más tarde.";
        }
    }
    
    /**
     * Genera un mensaje con las categorías disponibles.
     */
    private String generateCategoriesMessage() {
        String[] categories = geolocationService.getPlasticProductConsumerCategories();
        
        StringBuilder message = new StringBuilder();
        message.append("🏷️ Categorías de negocios que pueden necesitar productos:\n\n");
        
        for (int i = 0; i < categories.length; i++) {
            message.append(i + 1).append(". ").append(categories[i]).append("\n");
        }
        
        message.append("\n💡 Usa comandos como /cerca o preguntas naturales para buscar por estas categorías.");
        
        return message.toString();
    }

    private String handlePriceComparison(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return "❌ Por favor, dime qué producto quieres comparar.";
        }

        List<Supplier> suppliers = supplierService.findAndCompareSuppliers(productName);

        if (suppliers.isEmpty()) {
            return "❌ No encontré proveedores para '" + productName + "'.";
        }

        StringBuilder response = new StringBuilder("⚖️ Comparativa de precios para '" + productName + "':\n\n");
        for (int i = 0; i < suppliers.size(); i++) {
            Supplier supplier = suppliers.get(i);
            BigDecimal price = getPriceForProduct(supplier, productName);
            response.append(String.format("%d. %s - %s\n", i + 1, supplier.getName(), price));
        }

        return response.toString();
    }

    private BigDecimal getPriceForProduct(Supplier supplier, String productName) {
        Object price = supplier.getProducts().get(productName);
        if (price instanceof Number) {
            return new BigDecimal(price.toString());
        }
        return BigDecimal.ZERO;
    }

    /**
     * Guarda el comando ejecutado en la base de datos.
     */
    private void saveCommandToDatabase(Long chatId, User user, String command) {
        try {
            BotCommand botCommand = BotCommand.builder()
                    .chatId(chatId)
                    .command(command)
                    .userName(user.getUserName())
                    .firstName(user.getFirstName())
                    .createdAt(LocalDateTime.now())
                    .build();

            botCommandRepository.save(botCommand);
            log.debug("Comando guardado en BD - chatId={}, command={}", chatId, command);
        } catch (Exception e) {
            log.error("Error al guardar comando en BD - chatId={}, command={}, exception={}, message={}", 
                    chatId, command, e.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    /**
     * Genera una respuesta usando Grok AI o eco como fallback.
     */
    private String generateGrokResponse(Long chatId, String messageText) {
        try {
            // Intentar obtener respuesta de Grok AI
            String grokResponse = grokService.getResponse(chatId, messageText);
            
            // Si Grok AI responde correctamente, retornar
            if (grokResponse != null && !grokResponse.isEmpty()) {
                log.info("Respuesta de Grok AI generada - chatId={}, responseLength={}", 
                        chatId, grokResponse.length());
                return grokResponse;
            }
        } catch (Exception e) {
            log.error("Error al obtener respuesta de Grok AI - chatId={}, error={}", 
                    chatId, e.getMessage());
        }
        
        // Fallback: usar respuesta eco
        log.warn("Grok AI no disponible, usando respuesta eco - chatId={}", chatId);
        return Messages.TELEGRAM_ECHO_PREFIX + messageText;
    }

    /**
     * Guarda el mensaje en la base de datos.
     */
    private void saveMessageToDatabase(Long chatId, User user, String messageText, String response) {
        try {
            TelegramMessageDTO dto = TelegramMessageDTO.builder()
                    .chatId(chatId)
                    .userName(user.getUserName())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .messageText(messageText)
                    .botResponse(response)
                    .build();

            telegramService.saveMessage(dto);
            log.debug("Mensaje guardado en BD - chatId={}, userName={}", chatId, user.getUserName());
        } catch (Exception e) {
            log.error("Error al guardar mensaje en BD - chatId={}, exception={}, message={}", 
                    chatId, e.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    /**
     * Maneja la búsqueda de negocios usando RAG (Retrieval Augmented Generation) con citación de fuentes.
     */
    private String handleRagBusinessSearch(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return "❌ Por favor especifica qué negocio o categoría buscas.\n\nEjemplo: panadería o Ferretería La Unión";
        }
        
        log.info("Búsqueda RAG de negocios - searchTerm={}", searchTerm);
        
        // For now, we'll use default location (Bogotá) and radius (3km)
        // In a real implementation, we would get the user's location
        Double defaultLongitude = -74.0721;
        Double defaultLatitude = 4.7110;
        Integer defaultRadiusMeters = 3000;
        
        try {
            return ragSearchService.searchAndFormatForTelegram(searchTerm, defaultLatitude, defaultLongitude, defaultRadiusMeters);
        } catch (Exception e) {
            log.error("Error al buscar negocios con RAG - searchTerm={}, error={}", searchTerm, e.getMessage());
            return "❌ Error al buscar negocios. Por favor intenta nuevamente.";
        }
    }

    /**
     * Maneja la captura de leads con consentimiento GDPR/LGPD.
     */
    private String handleLeadCapture(Long chatId, User user, GrokIntent intent) {
        try {
            // Check if user already has a lead
            Optional<Lead> existingLead = leadService.getLeadByUserWaId(String.valueOf(chatId));

            if (existingLead.isPresent()) {
                Lead lead = existingLead.get();
                return String.format(
                    "👋 ¡Hola de nuevo %s! Ya tienes un lead registrado con nosotros.\n\n" +
                    "📊 Estado actual: %s\n" +
                    "📞 Teléfono: %s\n" +
                    "📧 Email: %s\n" +
                    "✅ Consentimiento: %s\n\n" +
                    "💡 Si necesitas actualizar tu información, puedes decir:\n" +
                    "• 'Actualizar mi teléfono a +57 300 1234567'\n" +
                    "• 'Cambiar mi email a ejemplo@correo.com'\n" +
                    "• 'Agregar mi ciudad: Bogotá'",
                    lead.getFirstName(),
                    translateStatus(lead.getStatus()),
                    lead.getPhone() != null ? lead.getPhone() : "No registrado",
                    lead.getEmail() != null ? lead.getEmail() : "No registrado",
                    lead.getConsentGiven() ? "Otorgado ✓" : "Pendiente ⚠️"
                );
            }

            // New lead - ask for consent first
            if (intent.getHasConsent() == null || !intent.getHasConsent()) {
                return String.format(
                    "👋 ¡Hola %s! Para brindarte el mejor servicio y mantenerte informado sobre productos y negocios que te interesan, necesitamos tu consentimiento.\n\n" +
                    "✅ **¿Estás de acuerdo con que almacenemos tu información de contacto para:\n" +
                    "• Enviarte recomendaciones personalizadas\n" +
                    "• Informarte sobre nuevos productos y negocios\n" +
                    "• Contactarte para ofertas especiales\n\n" +
                    "Responde 'Sí, acepto' para continuar o 'No, gracias' para cancelar.\n\n" +
                    "🔒 Cumplimos con las normas GDPR/LGPD para proteger tu privacidad.",
                    user.getFirstName()
                );
            }

            // User gave consent - create lead
            if (Boolean.TRUE.equals(intent.getHasConsent()) && intent.getFirstName() != null) {
                Lead lead = Lead.builder()
                    .businessId(1L) // Default business for now, can be enhanced later
                    .userWaId(String.valueOf(chatId))
                    .source("telegram")
                    .firstName(intent.getFirstName())
                    .lastName(intent.getLastName())
                    .phone(intent.getPhone())
                    .email(intent.getEmail())
                    .city(intent.getCity())
                    .consentGiven(true)
                    .consentDate(LocalDateTime.now())
                    .build();

                Lead savedLead = leadService.createLead(lead);

                return String.format(
                    "🎉 ¡Perfecto %s! Hemos registrado tu información.\n\n" +
                    "📋 **Resumen de tu lead:**\n" +
                    "👤 Nombre: %s %s\n" +
                    "📞 Teléfono: %s\n" +
                    "📧 Email: %s\n" +
                    "🏙️ Ciudad: %s\n" +
                    "✅ Consentimiento: Otorgado\n" +
                    "📅 Fecha: %s\n\n" +
                    "💡 Ahora recibirás recomendaciones personalizadas y ofertas especiales. ¡Gracias por confiar en nosotros!",
                    savedLead.getFirstName(),
                    savedLead.getFirstName(),
                    savedLead.getLastName() != null ? savedLead.getLastName() : "",
                    savedLead.getPhone() != null ? savedLead.getPhone() : "No proporcionado",
                    savedLead.getEmail() != null ? savedLead.getEmail() : "No proporcionado",
                    savedLead.getCity() != null ? savedLead.getCity() : "No proporcionada",
                    savedLead.getConsentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            }

            // Fallback for unclear consent
            return "❓ No pude entender tu respuesta sobre el consentimiento. Por favor responde 'Sí, acepto' para continuar o 'No, gracias' para cancelar.";

        } catch (Exception e) {
            log.error("Error al procesar captura de lead - chatId={}, error={}", chatId, e.getMessage(), e);
            return "❌ Hubo un error al procesar tu solicitud de lead. Por favor intenta nuevamente más tarde.";
        }
    }

    /**
     * Maneja la búsqueda de productos.
     */
    private String handleProductSearch(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return "❌ Por favor especifica qué producto buscas.\n\nEjemplo: vasos de plástico o café";
        }
        
        log.info("Búsqueda de productos - searchTerm={}", searchTerm);
        
        try {
            List<Product> products = productService.searchProducts(searchTerm);
            return formatProductListForTelegram(products, searchTerm);
        } catch (Exception e) {
            log.error("Error al buscar productos - searchTerm={}, error={}", searchTerm, e.getMessage());
            return "❌ Error al buscar productos. Por favor intenta nuevamente.";
        }
    }

    /**
     * Formatea una lista de productos para mostrar en Telegram.
     */
    private String formatProductListForTelegram(List<Product> products, String searchTerm) {
        if (products.isEmpty()) {
            return String.format("❌ No encontré productos con \"%s\".\n\n" +
                    "💡 Intenta con:\n" +
                    "• Otro término de búsqueda\n" +
                    "• Una categoría más general", searchTerm);
        }

        StringBuilder response = new StringBuilder();
        response.append(String.format("🔍 Encontré %d producto%s con \"%s\":\n\n",
                products.size(),
                products.size() == 1 ? "" : "s",
                searchTerm));

        int count = 0;
        for (Product product : products) {
            if (count >= 10) { // Limitar a 10 resultados
                response.append(String.format("\n... y %d más. Refina tu búsqueda para ver otros resultados.",
                        products.size() - 10));
                break;
            }

            count++;
            response.append(String.format("%d. %s %s\n", count, getProductEmoji(product.getCategory()), product.getName()));
            
            if (product.getDescription() != null && !product.getDescription().isEmpty()) {
                String shortDesc = product.getDescription().length() > 80 
                    ? product.getDescription().substring(0, 77) + "..." 
                    : product.getDescription();
                response.append(String.format("   📝 %s\n", shortDesc));
            }
            
            if (product.getPrice() != null) {
                response.append(String.format("   💰 %s", product.getFormattedPrice()));
                if (product.getCategory() != null) {
                    response.append(String.format(" | 📂 %s", product.getCategory()));
                }
                response.append("\n");
            }
            
            if (product.getStock() != null) {
                if (product.getStock() > 0) {
                    response.append(String.format("   📦 Stock: %d unidades\n", product.getStock()));
                } else {
                    response.append("   ⚠️ Sin stock disponible\n");
                }
            }
            
            if (product.getBusiness() != null) {
                response.append(String.format("   🏪 %s\n", product.getBusiness().getName()));
                if (product.getBusiness().getPhone() != null) {
                    response.append(String.format("   📞 %s\n", product.getBusiness().getPhone()));
                }
            }
            
            response.append("\n");
        }

        response.append("💡 Tip: Contacta directamente al negocio para más información.");

        return response.toString();
    }

    /**
     * Obtiene un emoji apropiado según la categoría del producto.
     */
    private String getProductEmoji(String category) {
        if (category == null) return "📦";
        
        String cat = category.toLowerCase();
        if (cat.contains("vaso")) return "🥤";
        if (cat.contains("plato")) return "🍽️";
        if (cat.contains("cubiert")) return "🍴";
        if (cat.contains("bolsa")) return "🛍️";
        if (cat.contains("contenedor")) return "🍱";
        if (cat.contains("ropa") || cat.contains("camisa") || cat.contains("vestido")) return "👕";
        if (cat.contains("zapato")) return "👟";
        if (cat.contains("comida") || cat.contains("alimento")) return "🍔";
        if (cat.contains("herramienta")) return "🔧";
        
        return "📦";
    }

    /**
     * Envía un mensaje de texto al usuario.
     */
    private void sendTextMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            execute(message);
            log.info("Respuesta enviada - chatId={}, responseLength={}", chatId, text.length());
        } catch (TelegramApiException e) {
            log.error("Error al enviar mensaje - chatId={}, exception={}, message={}", 
                    chatId, e.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    /**
     * Traduce el estado del lead al español para mostrar en Telegram.
     */
    private String translateStatus(String status) {
        return switch (status) {
            case "new" -> "Nuevo";
            case "contacted" -> "Contactado";
            case "qualified" -> "Calificado";
            case "converted" -> "Convertido";
            case "lost" -> "Perdido";
            case "archived" -> "Archivado";
            default -> status;
        };
    }

    /**
     * Elimina el webhook de Telegram si existe.
     * Esto es necesario para usar long polling en lugar de webhooks.
     * 
     * @return true si se eliminó correctamente o no existía, false si hubo error
     */
    public boolean deleteWebhook() {
        try {
            log.info("Eliminando webhook de Telegram...");
            DeleteWebhook deleteWebhook = new DeleteWebhook();
            Boolean result = execute(deleteWebhook);
            
            if (Boolean.TRUE.equals(result)) {
                log.info("✓ Webhook eliminado correctamente");
                return true;
            } else {
                log.warn("⚠ No se pudo eliminar el webhook (puede que no exista)");
                return true; // No es un error crítico
            }
        } catch (TelegramApiException e) {
            log.error("Error al eliminar webhook - exception={}, message={}", 
                    e.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }
}
