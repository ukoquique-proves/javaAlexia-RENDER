package com.alexia.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("🤖 Alexia");
        logo.getStyle()
            .set("font-size", "var(--lumo-font-size-l)")
            .set("margin", "0");

        Span subtitle = new Span("Panel Administrativo");
        subtitle.getStyle()
            .set("font-size", "var(--lumo-font-size-s)")
            .set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout titleLayout = new VerticalLayout(logo, subtitle);
        titleLayout.setSpacing(false);
        titleLayout.setPadding(false);

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), titleLayout);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private void createDrawer() {
        Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.getStyle().set("margin", "0");
        tabs.getStyle().set("width", "100%");

        // Dashboard
        tabs.add(createTab(VaadinIcon.DASHBOARD, "Dashboard", DashboardView.class));
        
        // Sección de Gestión
        tabs.add(createSectionHeader("GESTIÓN"));
        tabs.add(createTab(VaadinIcon.SHOP, "Negocios", BusinessesView.class));
        tabs.add(createTab(VaadinIcon.PACKAGE, "Productos", ProductsView.class));
        tabs.add(createTab(VaadinIcon.TRUCK, "Proveedores", SuppliersView.class));
        tabs.add(createTab(VaadinIcon.MEGAPHONE, "Campañas", CampaignsView.class));
        tabs.add(createTab(VaadinIcon.USERS, "Leads", LeadsView.class));
        
        // Sección de Mensajería
        tabs.add(createSectionHeader("MENSAJERÍA"));
        tabs.add(createTab(VaadinIcon.CHAT, "Telegram", TelegramView.class));
        tabs.add(createTab(VaadinIcon.LIST, "Logs Telegram", TelegramLogsView.class));
        tabs.add(createTab(VaadinIcon.PHONE, "WhatsApp", WhatsAppView.class));
        tabs.add(createTab(VaadinIcon.COMMENTS, "Conversaciones", ConversationsView.class));
        
        // Sección de Análisis
        tabs.add(createSectionHeader("ANÁLISIS"));
        tabs.add(createTab(VaadinIcon.CHART, "Métricas", MetricsView.class));
        tabs.add(createTab(VaadinIcon.MONEY, "Facturación", BillingView.class));
        tabs.add(createTab(VaadinIcon.TRENDING_UP, "Tracking", TrackingView.class));
        
        // Sección de Configuración
        tabs.add(createSectionHeader("SISTEMA"));
        tabs.add(createTab(VaadinIcon.COG, "Configuración", ConfigurationView.class));
        tabs.add(createTab(VaadinIcon.DATABASE, "Base de Datos", DatabaseView.class));
        tabs.add(createTab(VaadinIcon.BOOK, "Logs", LogsView.class));

        addToDrawer(tabs);
    }

    private Tab createTab(VaadinIcon viewIcon, String viewName, Class<? extends com.vaadin.flow.component.Component> navigationTarget) {
        Icon icon = viewIcon.create();
        icon.getStyle()
            .set("box-sizing", "border-box")
            .set("margin-inline-end", "var(--lumo-space-m)")
            .set("padding", "var(--lumo-space-xs)");

        RouterLink link = new RouterLink();
        link.add(icon, new Span(viewName));
        link.setRoute(navigationTarget);
        link.setTabIndex(-1);
        link.getStyle().set("text-decoration", "none");

        Tab tab = new Tab(link);
        tab.getStyle().set("cursor", "pointer");
        
        return tab;
    }

    private Tab createSectionHeader(String title) {
        Span header = new Span(title);
        header.getStyle()
            .set("font-size", "var(--lumo-font-size-xs)")
            .set("font-weight", "bold")
            .set("color", "var(--lumo-secondary-text-color)")
            .set("padding", "var(--lumo-space-m) var(--lumo-space-m) var(--lumo-space-xs)")
            .set("margin-top", "var(--lumo-space-m)");
        
        Tab tab = new Tab(header);
        tab.setEnabled(false);
        
        return tab;
    }
}
