package com.alexia.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "billing", layout = MainLayout.class)
@PageTitle("Facturación | Alexia")
public class BillingView extends VerticalLayout {

    public BillingView() {
        setSizeFull();
        setPadding(true);

        H2 title = new H2(VaadinIcon.MONEY.create(), new com.vaadin.flow.component.html.Span(" Facturación"));
        Paragraph description = new Paragraph("Gestión de facturación automática basada en CPC/CPA. Integración con Stripe y Mercado Pago.");
        Paragraph status = new Paragraph("⏳ Funcionalidad en desarrollo");
        status.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, description, status);
    }
}
