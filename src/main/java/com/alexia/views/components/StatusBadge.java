package com.alexia.views.components;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * Componente reutilizable para mostrar el estado de un servicio.
 * Badge con icono y texto que indica si el servicio está activo o inactivo.
 * 
 * @author Alexia Team
 * @version 1.0
 * @since 2025-10-15
 */
public class StatusBadge extends HorizontalLayout {
    
    private final Icon icon;
    private final Span text;
    private boolean isActive;
    
    /**
     * Constructor para crear un badge de estado.
     * 
     * @param serviceName Nombre del servicio
     * @param active true si el servicio está activo, false si está inactivo
     */
    public StatusBadge(String serviceName, boolean active) {
        this.isActive = active;
        
        setSpacing(true);
        setPadding(true);
        setAlignItems(Alignment.CENTER);
        
        updateStyles();
        
        // Icono
        icon = active ? VaadinIcon.CHECK_CIRCLE.create() : VaadinIcon.CLOSE_CIRCLE.create();
        icon.setSize("16px");
        icon.getStyle().set("color", active ? "#4CAF50" : "#F44336");
        
        // Texto
        text = new Span(serviceName);
        text.getStyle()
            .set("font-size", "0.9em")
            .set("font-weight", "500")
            .set("color", active ? "#2E7D32" : "#C62828");
        
        add(icon, text);
    }
    
    /**
     * Actualiza el estado del badge.
     * 
     * @param active true para activo, false para inactivo
     */
    public void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            
            // Actualizar icono
            remove(icon);
            Icon newIcon = active ? VaadinIcon.CHECK_CIRCLE.create() : VaadinIcon.CLOSE_CIRCLE.create();
            newIcon.setSize("16px");
            newIcon.getStyle().set("color", active ? "#4CAF50" : "#F44336");
            addComponentAsFirst(newIcon);
            
            // Actualizar texto
            text.getStyle().set("color", active ? "#2E7D32" : "#C62828");
            
            // Actualizar estilos del badge
            updateStyles();
        }
    }
    
    /**
     * Actualiza los estilos del badge según el estado.
     */
    private void updateStyles() {
        getStyle()
            .set("border-radius", "20px")
            .set("background", isActive ? "#E8F5E9" : "#FFEBEE")
            .set("padding", "5px 15px");
    }
    
    /**
     * Obtiene el estado actual del badge.
     * 
     * @return true si está activo, false si está inactivo
     */
    public boolean isActive() {
        return isActive;
    }
}
