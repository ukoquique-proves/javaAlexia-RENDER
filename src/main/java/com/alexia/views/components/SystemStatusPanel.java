package com.alexia.views.components;

import com.alexia.constants.Messages;
import com.alexia.dto.ConnectionResultDTO;
import com.alexia.service.BotManagerService;
import com.alexia.usecase.TestConnectionUseCase;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Panel completo para mostrar el estado del sistema.
 * Incluye badges de estado de servicios y botón de prueba de conexión.
 * 
 * @author Alexia Team
 * @version 1.0
 * @since 2025-10-15
 */
public class SystemStatusPanel extends VerticalLayout {
    
    private final TestConnectionUseCase testConnectionUseCase;
    private final BotManagerService botManagerService;
    private final StatusBadge supabaseBadge;
    private final StatusBadge telegramBadge;
    private final StatusBadge whatsappBadge;
    private final StatusBadge aiBadge;
    private final StatusBadge googlePlacesBadge;
    private final Div resultMessage;
    
    /**
     * Constructor para crear el panel de estado del sistema.
     * 
     * @param testConnectionUseCase Caso de uso para probar la conexión
     * @param botManagerService Servicio para gestionar el estado del bot
     */
    public SystemStatusPanel(TestConnectionUseCase testConnectionUseCase, 
                            BotManagerService botManagerService) {
        this.testConnectionUseCase = testConnectionUseCase;
        this.botManagerService = botManagerService;
        
        setSpacing(true);
        setPadding(true);
        getStyle()
            .set("background", "white")
            .set("border-radius", "8px")
            .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");
        
        // Título
        H3 title = new H3("🔌 Estado del Sistema");
        title.getStyle().set("margin-top", "0");
        
        // Badges de estado
        HorizontalLayout statusRow = new HorizontalLayout();
        statusRow.setWidthFull();
        statusRow.setSpacing(true);
        
        // Consultar estado real de los servicios
        boolean isTelegramRunning = botManagerService.isBotRunning();
        
        supabaseBadge = new StatusBadge("Supabase", true);
        telegramBadge = new StatusBadge("Telegram", isTelegramRunning);
        whatsappBadge = new StatusBadge("WhatsApp", false);
        aiBadge = new StatusBadge("OpenAI/Grok", false);
        googlePlacesBadge = new StatusBadge("Google Places", false);
        
        statusRow.add(supabaseBadge, telegramBadge, whatsappBadge, aiBadge, googlePlacesBadge);
        
        // Botón de prueba
        Button testButton = new Button("Probar Conexión a Supabase", VaadinIcon.REFRESH.create());
        testButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        testButton.addClickListener(e -> testConnection());
        
        // Mensaje de resultado
        resultMessage = new Div();
        resultMessage.getStyle()
            .set("margin-top", "10px")
            .set("padding", "10px")
            .set("border-radius", "4px")
            .set("display", "none");
        
        add(title, statusRow, testButton, resultMessage);
    }
    
    /**
     * Ejecuta la prueba de conexión a Supabase.
     */
    private void testConnection() {
        ConnectionResultDTO result = testConnectionUseCase.execute();
        
        if (result.isSuccess()) {
            showSuccessMessage(result.toDisplayString());
            supabaseBadge.setActive(true);
            
            Notification notification = Notification.show(
                Messages.CONNECTION_SUCCESS,
                3000,
                Notification.Position.TOP_END
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else {
            showErrorMessage(result.toDisplayString());
            supabaseBadge.setActive(false);
            
            Notification notification = Notification.show(
                Messages.CONNECTION_ERROR,
                5000,
                Notification.Position.TOP_END
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    /**
     * Muestra un mensaje de éxito.
     * 
     * @param message Mensaje a mostrar
     */
    private void showSuccessMessage(String message) {
        resultMessage.setText(message);
        resultMessage.getStyle()
            .set("display", "block")
            .set("background", "#E8F5E9")
            .set("color", "#2E7D32")
            .set("border", "1px solid #4CAF50");
    }
    
    /**
     * Muestra un mensaje de error.
     * 
     * @param message Mensaje a mostrar
     */
    private void showErrorMessage(String message) {
        resultMessage.setText(message);
        resultMessage.getStyle()
            .set("display", "block")
            .set("background", "#FFEBEE")
            .set("color", "#C62828")
            .set("border", "1px solid #F44336");
    }
    
    /**
     * Actualiza el estado de un servicio específico.
     * 
     * @param service Nombre del servicio ("supabase", "telegram", "whatsapp", "ai", "googleplaces")
     * @param active true si está activo, false si está inactivo
     */
    public void updateServiceStatus(String service, boolean active) {
        switch (service.toLowerCase()) {
            case "supabase":
                supabaseBadge.setActive(active);
                break;
            case "telegram":
                telegramBadge.setActive(active);
                break;
            case "whatsapp":
                whatsappBadge.setActive(active);
                break;
            case "ai":
            case "openai":
            case "grok":
                aiBadge.setActive(active);
                break;
            case "googleplaces":
            case "google":
                googlePlacesBadge.setActive(active);
                break;
        }
    }
}
