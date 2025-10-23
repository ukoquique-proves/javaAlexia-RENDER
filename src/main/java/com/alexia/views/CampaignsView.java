package com.alexia.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "campaigns", layout = MainLayout.class)
@PageTitle("Campañas | Alexia")
public class CampaignsView extends VerticalLayout {

    public CampaignsView() {
        setSizeFull();
        setPadding(true);

        H2 title = new H2(VaadinIcon.MEGAPHONE.create(), new com.vaadin.flow.component.html.Span(" Campañas"));
        Paragraph description = new Paragraph("Gestión de campañas publicitarias CPC (Costo Por Clic) y CPA (Costo Por Adquisición).");
        Paragraph status = new Paragraph("⏳ Funcionalidad en desarrollo");
        status.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, description, status);
    }
}
