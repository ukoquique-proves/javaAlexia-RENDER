package com.alexia.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "logs", layout = MainLayout.class)
@PageTitle("Logs | Alexia")
public class LogsView extends VerticalLayout {

    public LogsView() {
        setSizeFull();
        setPadding(true);

        H2 title = new H2(VaadinIcon.BOOK.create(), new com.vaadin.flow.component.html.Span(" Logs"));
        Paragraph description = new Paragraph("Registro de actividad del sistema: errores, eventos, sincronizaciones y operaciones importantes.");
        Paragraph status = new Paragraph("⏳ Funcionalidad en desarrollo");
        status.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, description, status);
    }
}
