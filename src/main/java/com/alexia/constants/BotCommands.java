package com.alexia.constants;

/**
 * Constantes para comandos del bot de Telegram.
 */
public final class BotCommands {
    
    // Comandos disponibles
    public static final String START = "/start";
    public static final String HELP = "/help";
    public static final String STATUS = "/status";
    
    // Mensajes de respuesta
    public static final String START_MESSAGE = 
            "¡Bienvenido a Alexia! 🤖\n\n" +
            "Soy tu asistente automatizado para encontrar negocios, productos y servicios locales.\n\n" +
            "Usa /help para ver los comandos disponibles.";
    
    public static final String HELP_MESSAGE = 
            "📋 Comandos disponibles:\n\n" +
            "/start - Mensaje de bienvenida\n" +
            "/help - Muestra esta ayuda\n" +
            "/status - Estado del bot\n\n" +
            "También puedes enviarme cualquier mensaje y te responderé con eco.";
    
    public static final String STATUS_MESSAGE_TEMPLATE = 
            "✅ Bot activo y funcionando\n\n" +
            "📊 Estadísticas:\n" +
            "• Mensajes procesados: %d\n" +
            "• Comandos ejecutados: %d\n" +
            "• Última actualización: %s";
    
    public static final String UNKNOWN_COMMAND_MESSAGE = 
            "Comando no reconocido. Usa /help para ver los comandos disponibles.";
    
    private BotCommands() {
        throw new UnsupportedOperationException("Esta es una clase de constantes y no debe ser instanciada");
    }
}
