package com.alexia.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "conversations", layout = MainLayout.class)
@PageTitle("Conversaciones | Alexia")
public class ConversationsView extends VerticalLayout {

    public ConversationsView() {
        setSizeFull();
        setPadding(true);

        H2 title = new H2(VaadinIcon.COMMENTS.create(), new com.vaadin.flow.component.html.Span(" Conversaciones"));
        Paragraph description = new Paragraph("Historial completo de conversaciones con usuarios. Filtra por canal, fecha y estado.");
        Paragraph status = new Paragraph("⏳ Funcionalidad en desarrollo - Paso 4");
        status.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, description, status);
    }
}
