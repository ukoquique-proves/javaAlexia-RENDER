package com.alexia.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "metrics", layout = MainLayout.class)
@PageTitle("Métricas | Alexia")
public class MetricsView extends VerticalLayout {

    public MetricsView() {
        setSizeFull();
        setPadding(true);

        H2 title = new H2(VaadinIcon.CHART.create(), new com.vaadin.flow.component.html.Span(" Métricas"));
        Paragraph description = new Paragraph("Análisis detallado de métricas: conversiones, tasa de respuesta, engagement y más.");
        Paragraph status = new Paragraph("⏳ Funcionalidad en desarrollo - Paso 10");
        status.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, description, status);
    }
}
