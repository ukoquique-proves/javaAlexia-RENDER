package com.alexia.views.components;

import com.alexia.constants.UIConstants;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Componente reutilizable para mostrar una métrica con icono, valor y etiqueta.
 * Diseño consistente con borde de color personalizable.
 * 
 * @author Alexia Team
 * @version 1.0
 * @since 2025-10-15
 */
public class MetricCard extends VerticalLayout {
    
    private final H1 valueText;
    private final Span labelText;
    private final Icon icon;
    
    /**
     * Constructor para crear una tarjeta de métrica.
     * 
     * @param label Etiqueta descriptiva de la métrica
     * @param value Valor actual de la métrica
     * @param iconType Icono de Vaadin a mostrar
     * @param borderColor Color del borde izquierdo (hex o CSS)
     */
    public MetricCard(String label, String value, VaadinIcon iconType, String borderColor) {
        setSpacing(false);
        setPadding(true);
        getStyle()
            .set("background", UIConstants.CARD_BACKGROUND)
            .set("border-radius", UIConstants.CARD_BORDER_RADIUS)
            .set("box-shadow", UIConstants.CARD_BOX_SHADOW)
            .set("border-left", "4px solid " + borderColor);

        // Icono
        icon = iconType.create();
        icon.setSize(UIConstants.ICON_SIZE_LARGE);
        icon.getStyle().set("color", borderColor);

        // Valor
        valueText = new H1(value);
        valueText.getStyle()
            .set("margin", "10px 0 5px 0")
            .set("font-size", "2em")
            .set("color", "#333");

        // Etiqueta
        labelText = new Span(label);
        labelText.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-size", "0.9em");

        add(icon, valueText, labelText);
        setAlignItems(Alignment.START);
    }
    
    /**
     * Actualiza el valor mostrado en la tarjeta.
     * 
     * @param newValue Nuevo valor a mostrar
     */
    public void updateValue(String newValue) {
        valueText.setText(newValue);
    }
    
    /**
     * Actualiza la etiqueta de la tarjeta.
     * 
     * @param newLabel Nueva etiqueta a mostrar
     */
    public void updateLabel(String newLabel) {
        labelText.setText(newLabel);
    }
}
