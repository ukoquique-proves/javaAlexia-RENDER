package com.alexia.views;

import com.alexia.constants.Messages;
import com.alexia.constants.UIConstants;
import com.alexia.repository.TelegramMessageRepository;
import com.alexia.service.BotManagerService;
import com.alexia.usecase.TestConnectionUseCase;
import com.alexia.views.components.MetricCard;
import com.alexia.views.components.SystemStatusPanel;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | Alexia")
public class DashboardView extends VerticalLayout {

    private final TestConnectionUseCase testConnectionUseCase;
    private final BotManagerService botManagerService;
    private final TelegramMessageRepository telegramMessageRepository;

    public DashboardView(TestConnectionUseCase testConnectionUseCase,
                        BotManagerService botManagerService,
                        TelegramMessageRepository telegramMessageRepository) {
        this.testConnectionUseCase = testConnectionUseCase;
        this.botManagerService = botManagerService;
        this.telegramMessageRepository = telegramMessageRepository;
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Header
        H2 title = new H2("📊 Dashboard");
        title.getStyle().set("margin", "0");
        
        Paragraph subtitle = new Paragraph("Vista general del sistema Alexia");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        add(title, subtitle);

        // Obtener métricas reales
        long totalMessages = telegramMessageRepository.count();

        // Métricas principales - Primera fila
        HorizontalLayout metricsRow1 = new HorizontalLayout();
        metricsRow1.setWidthFull();
        metricsRow1.setSpacing(true);
        
        metricsRow1.add(
            new MetricCard("Mensajes Telegram", String.valueOf(totalMessages), VaadinIcon.CHAT, UIConstants.COLOR_PRIMARY_BLUE),
            new MetricCard("Leads Generados", "0", VaadinIcon.USERS, UIConstants.COLOR_SUCCESS_GREEN),
            new MetricCard("Negocios Activos", "0", VaadinIcon.SHOP, UIConstants.COLOR_WARNING_ORANGE),
            new MetricCard("Conversiones", "0", VaadinIcon.TRENDING_UP, UIConstants.COLOR_PURPLE)
        );
        
        add(metricsRow1);

        // Métricas principales - Segunda fila
        HorizontalLayout metricsRow2 = new HorizontalLayout();
        metricsRow2.setWidthFull();
        metricsRow2.setSpacing(true);
        
        metricsRow2.add(
            new MetricCard("Campañas Activas", "0", VaadinIcon.MEGAPHONE, UIConstants.COLOR_CYAN),
            new MetricCard("Ingresos del Mes", "$0", VaadinIcon.MONEY, UIConstants.COLOR_SUCCESS_GREEN),
            new MetricCard("Clics Totales", "0", VaadinIcon.CURSOR, UIConstants.COLOR_RED),
            new MetricCard("Tasa de Respuesta", "0%", VaadinIcon.CHART_LINE, UIConstants.COLOR_DEEP_PURPLE)
        );
        
        add(metricsRow2);

        // Panel de estado del sistema
        SystemStatusPanel systemStatus = new SystemStatusPanel(testConnectionUseCase, botManagerService);
        add(systemStatus);

        // Actividad reciente
        VerticalLayout recentActivity = createRecentActivitySection();
        add(recentActivity);
    }

    private VerticalLayout createRecentActivitySection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(true);
        section.getStyle()
            .set("background", "white")
            .set("border-radius", "8px")
            .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        H3 sectionTitle = new H3("📋 Actividad Reciente");
        sectionTitle.getStyle().set("margin-top", "0");

        Paragraph placeholder = new Paragraph(Messages.NO_RECENT_ACTIVITY);
        placeholder.getStyle().set("color", "var(--lumo-secondary-text-color)");

        section.add(sectionTitle, placeholder);
        return section;
    }
}
