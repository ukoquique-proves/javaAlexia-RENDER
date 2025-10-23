package com.alexia.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Vista de configuración general del sistema.
 * Aquí se configurarán API keys, tokens, costos CPC/CPA, integraciones externas, etc.
 */
@Route(value = "configuration", layout = MainLayout.class)
@PageTitle("Configuración | Alexia")
public class ConfigurationView extends VerticalLayout {

    public ConfigurationView() {
        setSizeFull();
        setPadding(true);

        H2 title = new H2(VaadinIcon.COG.create(), new Span(" Configuración General"));
        
        Paragraph description = new Paragraph(
            "Configuración general del sistema: API keys, tokens, costos CPC/CPA, integraciones externas."
        );
        
        Paragraph note = new Paragraph(
            "💡 Nota: Los controles del bot de Telegram se encuentran en la sección 'Telegram' del menú."
        );
        note.getStyle().set("color", "var(--lumo-primary-color)");
        
        Paragraph status = new Paragraph("⏳ Funcionalidad en desarrollo");
        status.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, description, note, status);
    }
}
