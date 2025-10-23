package com.alexia.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "whatsapp", layout = MainLayout.class)
@PageTitle("WhatsApp | Alexia")
public class WhatsAppView extends VerticalLayout {

    public WhatsAppView() {
        setSizeFull();
        setPadding(true);

        H2 title = new H2(VaadinIcon.PHONE.create(), new com.vaadin.flow.component.html.Span(" WhatsApp"));
        Paragraph description = new Paragraph("Configuración y monitoreo del bot de WhatsApp Business. Gestiona webhooks y conversaciones.");
        Paragraph status = new Paragraph("⏳ Funcionalidad en desarrollo");
        status.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, description, status);
    }
}
