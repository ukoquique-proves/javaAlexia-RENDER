package com.alexia.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "tracking", layout = MainLayout.class)
@PageTitle("Tracking | Alexia")
public class TrackingView extends VerticalLayout {

    public TrackingView() {
        setSizeFull();
        setPadding(true);

        H2 title = new H2(VaadinIcon.TRENDING_UP.create(), new com.vaadin.flow.component.html.Span(" Tracking"));
        Paragraph description = new Paragraph("Sistema de tracking de clics en enlaces enviados por el bot. Registra eventos y conversiones.");
        Paragraph status = new Paragraph("⏳ Funcionalidad en desarrollo");
        status.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, description, status);
    }
}
