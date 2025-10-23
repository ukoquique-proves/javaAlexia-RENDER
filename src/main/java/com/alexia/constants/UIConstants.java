package com.alexia.constants;

/**
 * Constantes de UI (colores, tamaños, estilos).
 * Centraliza valores de diseño para consistencia visual.
 */
public final class UIConstants {
    
    private UIConstants() {
        throw new UnsupportedOperationException("Esta es una clase de constantes");
    }
    
    // Colores de métricas
    public static final String COLOR_PRIMARY_BLUE = "#2196F3";
    public static final String COLOR_SUCCESS_GREEN = "#4CAF50";
    public static final String COLOR_WARNING_ORANGE = "#FF9800";
    public static final String COLOR_PURPLE = "#9C27B0";
    public static final String COLOR_CYAN = "#00BCD4";
    public static final String COLOR_RED = "#FF5722";
    public static final String COLOR_DEEP_PURPLE = "#673AB7";
    
    // Colores de estado
    public static final String COLOR_SUCCESS_BG = "#E8F5E9";
    public static final String COLOR_SUCCESS_TEXT = "#2E7D32";
    public static final String COLOR_ERROR_BG = "#FFEBEE";
    public static final String COLOR_ERROR_TEXT = "#C62828";
    
    // Tamaños
    public static final String ICON_SIZE_SMALL = "16px";
    public static final String ICON_SIZE_MEDIUM = "24px";
    public static final String ICON_SIZE_LARGE = "32px";
    
    // Estilos de card
    public static final String CARD_BACKGROUND = "white";
    public static final String CARD_BORDER_RADIUS = "8px";
    public static final String CARD_BOX_SHADOW = "0 2px 4px rgba(0,0,0,0.1)";
    
    // Estilos de badge
    public static final String BADGE_BORDER_RADIUS = "20px";
    public static final String BADGE_PADDING = "5px 15px";
}
