package com.alexia.views;

import com.alexia.repository.BotCommandRepository;
import com.alexia.repository.TelegramMessageRepository;
import com.alexia.service.BotManagerService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

/**
 * Vista principal del Bot de Telegram.
 * Permite controlar el bot (iniciar/detener) y visualizar estadísticas.
 */
@Route(value = "telegram", layout = MainLayout.class)
@PageTitle("Telegram | Alexia")
public class TelegramView extends VerticalLayout {

    private final BotManagerService botManagerService;
    private final TelegramMessageRepository messageRepository;
    private final BotCommandRepository commandRepository;
    
    private final Span botStatusBadge;
    private final Button toggleBotButton;
    private final Button restartBotButton;
    private final Paragraph botStatusDescription;
    
    private final Span totalMessagesSpan;
    private final Span totalCommandsSpan;

    public TelegramView(BotManagerService botManagerService,
                       TelegramMessageRepository messageRepository,
                       BotCommandRepository commandRepository) {
        this.botManagerService = botManagerService;
        this.messageRepository = messageRepository;
        this.commandRepository = commandRepository;
        
        // Inicializar componentes del bot
        botStatusBadge = new Span();
        botStatusBadge.getElement().getThemeList().add("badge");
        
        botStatusDescription = new Paragraph();
        botStatusDescription.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        toggleBotButton = new Button();
        toggleBotButton.addClickListener(e -> toggleBot());
        
        restartBotButton = new Button("Reiniciar Bot", new Icon(VaadinIcon.REFRESH));
        restartBotButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        restartBotButton.addClickListener(e -> restartBot());
        
        // Inicializar componentes de estadísticas
        totalMessagesSpan = new Span("0");
        totalMessagesSpan.getStyle()
            .set("font-size", "2em")
            .set("font-weight", "bold")
            .set("color", "var(--lumo-primary-color)");
        
        totalCommandsSpan = new Span("0");
        totalCommandsSpan.getStyle()
            .set("font-size", "2em")
            .set("font-weight", "bold")
            .set("color", "var(--lumo-success-color)");
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Título principal
        H2 title = new H2(VaadinIcon.CHAT.create(), new Span(" Bot de Telegram"));
        Paragraph description = new Paragraph(
            "Controla el bot de Telegram, visualiza estadísticas y gestiona la comunicación con usuarios."
        );

        // === SECCIÓN 1: Control del Bot ===
        Div botControlCard = createBotControlSection();
        
        // === SECCIÓN 2: Estadísticas ===
        Div statsCard = createStatsSection();
        
        // === SECCIÓN 3: Accesos Rápidos ===
        Div quickLinksCard = createQuickLinksSection();

        add(title, description, botControlCard, statsCard, quickLinksCard);
        
        // Actualizar datos iniciales
        updateBotStatus();
        updateStats();
    }

    /**
     * Crea la sección de control del bot.
     */
    private Div createBotControlSection() {
        H3 sectionTitle = new H3(VaadinIcon.POWER_OFF.create(), new Span(" Control del Bot"));
        
        HorizontalLayout buttonLayout = new HorizontalLayout(toggleBotButton, restartBotButton);
        buttonLayout.setSpacing(true);
        
        VerticalLayout content = new VerticalLayout(
            sectionTitle,
            botStatusBadge,
            botStatusDescription,
            buttonLayout
        );
        content.setSpacing(true);
        content.setPadding(false);
        
        return createCard(content);
    }

    /**
     * Crea la sección de estadísticas.
     */
    private Div createStatsSection() {
        H3 sectionTitle = new H3(VaadinIcon.CHART.create(), new Span(" Estadísticas"));
        
        Div messagesMetric = createMetricBox("📨 Total Mensajes", totalMessagesSpan);
        Div commandsMetric = createMetricBox("⚡ Total Comandos", totalCommandsSpan);
        
        HorizontalLayout metricsLayout = new HorizontalLayout(messagesMetric, commandsMetric);
        metricsLayout.setWidthFull();
        metricsLayout.setSpacing(true);
        
        VerticalLayout content = new VerticalLayout(sectionTitle, metricsLayout);
        content.setSpacing(true);
        content.setPadding(false);
        
        return createCard(content);
    }

    /**
     * Crea la sección de accesos rápidos.
     */
    private Div createQuickLinksSection() {
        H3 sectionTitle = new H3(VaadinIcon.LINK.create(), new Span(" Accesos Rápidos"));
        
        RouterLink logsLink = new RouterLink("Ver Logs de Telegram", TelegramLogsView.class);
        logsLink.getStyle()
            .set("font-size", "1.1em")
            .set("text-decoration", "none");
        
        Paragraph logsDescription = new Paragraph("Visualiza el historial completo de mensajes y conversaciones");
        logsDescription.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        VerticalLayout content = new VerticalLayout(
            sectionTitle,
            logsLink,
            logsDescription
        );
        content.setSpacing(true);
        content.setPadding(false);
        
        return createCard(content);
    }

    /**
     * Crea una caja de métrica.
     */
    private Div createMetricBox(String label, Span valueSpan) {
        Div box = new Div();
        box.getStyle()
            .set("padding", "var(--lumo-space-m)")
            .set("border", "1px solid var(--lumo-contrast-10pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("text-align", "center")
            .set("flex", "1");
        
        Paragraph labelParagraph = new Paragraph(label);
        labelParagraph.getStyle()
            .set("margin", "0")
            .set("color", "var(--lumo-secondary-text-color)");
        
        box.add(labelParagraph, valueSpan);
        return box;
    }

    /**
     * Alterna el estado del bot (iniciar/detener).
     */
    private void toggleBot() {
        try {
            if (botManagerService.isBotRunning()) {
                botManagerService.stopBot();
                showNotification("Bot detenido correctamente", NotificationVariant.LUMO_SUCCESS);
            } else {
                botManagerService.startBot();
                showNotification("Bot iniciado correctamente", NotificationVariant.LUMO_SUCCESS);
            }
        } catch (Exception e) {
            showNotification("Error: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
        updateBotStatus();
    }

    /**
     * Reinicia el bot.
     */
    private void restartBot() {
        try {
            botManagerService.restartBot();
            showNotification("Bot reiniciado correctamente", NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            showNotification("Error al reiniciar: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
        updateBotStatus();
    }

    /**
     * Actualiza la interfaz con el estado actual del bot.
     */
    private void updateBotStatus() {
        boolean isRunning = botManagerService.isBotRunning();
        
        if (isRunning) {
            // Bot activo
            botStatusBadge.setText("✅ ACTIVO");
            botStatusBadge.getElement().getThemeList().remove("error");
            botStatusBadge.getElement().getThemeList().add("success");
            
            botStatusDescription.setText(botManagerService.getBotStatus());
            
            toggleBotButton.setText("Detener Bot");
            toggleBotButton.setIcon(new Icon(VaadinIcon.STOP));
            toggleBotButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            toggleBotButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            
            restartBotButton.setEnabled(true);
        } else {
            // Bot inactivo
            botStatusBadge.setText("⏹️ DETENIDO");
            botStatusBadge.getElement().getThemeList().remove("success");
            botStatusBadge.getElement().getThemeList().add("error");
            
            botStatusDescription.setText(botManagerService.getBotStatus());
            
            toggleBotButton.setText("Iniciar Bot");
            toggleBotButton.setIcon(new Icon(VaadinIcon.PLAY));
            toggleBotButton.removeThemeVariants(ButtonVariant.LUMO_ERROR);
            toggleBotButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            
            restartBotButton.setEnabled(false);
        }
    }

    /**
     * Actualiza las estadísticas.
     */
    private void updateStats() {
        long totalMessages = messageRepository.count();
        long totalCommands = commandRepository.count();
        
        totalMessagesSpan.setText(String.valueOf(totalMessages));
        totalCommandsSpan.setText(String.valueOf(totalCommands));
    }

    /**
     * Crea un card estilizado.
     */
    private Div createCard(VerticalLayout content) {
        Div card = new Div();
        card.getStyle()
            .set("background", "var(--lumo-base-color)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("box-shadow", "var(--lumo-box-shadow-xs)")
            .set("padding", "var(--lumo-space-l)")
            .set("margin-bottom", "var(--lumo-space-m)");
        card.add(content);
        return card;
    }

    /**
     * Muestra una notificación.
     */
    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
        notification.open();
    }
}
