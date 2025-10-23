package com.alexia.views;

import com.alexia.entity.TelegramMessage;
import com.alexia.repository.TelegramMessageRepository;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

/**
 * Vista para mostrar los logs de mensajes de Telegram.
 */
@Route(value = "telegram-logs", layout = MainLayout.class)
@PageTitle("Logs de Telegram | Alexia")
public class TelegramLogsView extends VerticalLayout {

    private static final Logger logger = LoggerFactory.getLogger(TelegramLogsView.class);

    private final TelegramMessageRepository telegramMessageRepository;
    private final Grid<TelegramMessage> grid;
    private final Span totalMessagesLabel;
    private DatePicker dateFilter;

    public TelegramLogsView(TelegramMessageRepository telegramMessageRepository) {
        this.telegramMessageRepository = telegramMessageRepository;

        // Configuración del layout
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Título
        H2 title = new H2("📱 Logs de Telegram");
        title.getStyle()
                .set("margin", "0")
                .set("color", "#1976D2");

        // Estadísticas
        totalMessagesLabel = new Span();
        totalMessagesLabel.getStyle()
                .set("font-size", "14px")
                .set("color", "#666");
        updateStats();

        // Filtros
        HorizontalLayout filters = createFilters();

        // Grid de mensajes
        grid = createGrid();

        // Botones de acción
        HorizontalLayout actions = createActions();

        // Agregar componentes
        add(title, totalMessagesLabel, filters, actions, grid);

        // Cargar datos iniciales
        loadMessages();

        // Configurar auto-refresh
        setupAutoRefresh();
    }

    private HorizontalLayout createFilters() {
        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setAlignItems(Alignment.END);

        dateFilter = new DatePicker("Filtrar por fecha");
        dateFilter.setPlaceholder("Seleccionar fecha");
        dateFilter.setClearButtonVisible(true);
        dateFilter.addValueChangeListener(e -> loadMessages());

        Button todayButton = new Button("Hoy", new Icon(VaadinIcon.CALENDAR));
        todayButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        todayButton.addClickListener(e -> {
            dateFilter.setValue(LocalDate.now());
        });

        Button clearButton = new Button("Limpiar", new Icon(VaadinIcon.CLOSE_SMALL));
        clearButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_CONTRAST);
        clearButton.addClickListener(e -> {
            dateFilter.clear();
        });

        filters.add(dateFilter, todayButton, clearButton);
        return filters;
    }

    private HorizontalLayout createActions() {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.END);

        Button deleteButton = new Button("Eliminar Seleccionados", new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> confirmAndDeleteMessages());

        Button refreshButton = new Button("Actualizar", new Icon(VaadinIcon.REFRESH));
        refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshButton.addClickListener(e -> {
            loadMessages();
            updateStats();
        });

        actions.add(deleteButton, refreshButton);
        return actions;
    }

    private Grid<TelegramMessage> createGrid() {
        Grid<TelegramMessage> messageGrid = new Grid<>(TelegramMessage.class, false);
        messageGrid.setSizeFull();
        messageGrid.setPageSize(20);
        messageGrid.setSelectionMode(Grid.SelectionMode.MULTI);

        // Columna de fecha
        messageGrid.addColumn(message -> {
            if (message.getCreatedAt() != null) {
                return message.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            }
            return "-";
        })
                .setHeader("Fecha")
                .setWidth("180px")
                .setFlexGrow(0)
                .setSortable(true);

        // Columna de usuario
        messageGrid.addColumn(new ComponentRenderer<>(message -> {
            VerticalLayout userInfo = new VerticalLayout();
            userInfo.setPadding(false);
            userInfo.setSpacing(false);

            String displayName = message.getFirstName() != null ? message.getFirstName() : "Usuario";
            if (message.getLastName() != null) {
                displayName += " " + message.getLastName();
            }

            Span name = new Span(displayName);
            name.getStyle().set("font-weight", "bold");

            if (message.getUserName() != null) {
                Span username = new Span("@" + message.getUserName());
                username.getStyle()
                        .set("font-size", "12px")
                        .set("color", "#666");
                userInfo.add(name, username);
            } else {
                userInfo.add(name);
            }

            return userInfo;
        }))
                .setHeader("Usuario")
                .setWidth("200px")
                .setFlexGrow(0);

        // Columna de mensaje
        messageGrid.addColumn(TelegramMessage::getMessageText)
                .setHeader("Mensaje")
                .setFlexGrow(1);

        // Columna de respuesta del bot
        messageGrid.addColumn(new ComponentRenderer<>(message -> {
            if (message.getBotResponse() != null) {
                Span response = new Span(message.getBotResponse());
                response.getStyle()
                        .set("color", "#1976D2")
                        .set("font-style", "italic");
                return response;
            }
            return new Span("-");
        }))
                .setHeader("Respuesta Bot")
                .setFlexGrow(1);

        // Columna de Chat ID
        messageGrid.addColumn(TelegramMessage::getChatId)
                .setHeader("Chat ID")
                .setWidth("120px")
                .setFlexGrow(0);

        return messageGrid;
    }

    private void loadMessages() {
        List<TelegramMessage> messages;

        if (dateFilter.getValue() != null) {
            LocalDate selectedDate = dateFilter.getValue();
            LocalDateTime startOfDay = selectedDate.atStartOfDay();
            LocalDateTime endOfDay = selectedDate.plusDays(1).atStartOfDay();
            messages = telegramMessageRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startOfDay, endOfDay);
        } else {
            messages = telegramMessageRepository.findAllByOrderByCreatedAtDesc();
        }

        grid.setItems(messages);
        updateStats();
    }

    private void updateStats() {
        long total = telegramMessageRepository.count();
        totalMessagesLabel.setText("Total de mensajes: " + total);
    }

    private void setupAutoRefresh() {
        // Auto-refresh cada 5 segundos usando el mecanismo de polling de Vaadin
        getUI().ifPresent(ui -> {
            ui.setPollInterval(5000);
            ui.addPollListener(event -> {
                loadMessages();
                updateStats();
            });
        });
    }

    /**
     * Confirma y elimina los mensajes seleccionados.
     */
    private void confirmAndDeleteMessages() {
        Set<TelegramMessage> selectedMessages = grid.getSelectedItems();
        logger.info("Mensajes seleccionados para eliminar: {}", selectedMessages.size());

        if (selectedMessages.isEmpty()) {
            showWarning("Por favor, selecciona al menos un mensaje para eliminar");
            return;
        }

        ConfirmDialog dialog = createDeleteConfirmationDialog(selectedMessages);
        dialog.open();
    }

    /**
     * Crea el diálogo de confirmación para eliminar mensajes.
     */
    private ConfirmDialog createDeleteConfirmationDialog(Set<TelegramMessage> messages) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Confirmar eliminación");
        dialog.setText("¿Estás seguro de que deseas eliminar " + messages.size() +
                " mensaje(s)? Esta acción no se puede deshacer.");

        dialog.setCancelable(true);
        dialog.setCancelText("Cancelar");

        dialog.setConfirmText("Eliminar");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(event -> deleteMessages(messages));

        return dialog;
    }

    /**
     * Elimina los mensajes de la base de datos.
     */
    private void deleteMessages(Set<TelegramMessage> messages) {
        try {
            logger.info("Eliminando {} mensajes de la base de datos", messages.size());
            telegramMessageRepository.deleteAll(messages);

            showSuccess(messages.size() + " mensaje(s) eliminado(s) correctamente");

            loadMessages();
            updateStats();
            grid.deselectAll();
            logger.info("Mensajes eliminados exitosamente");
        } catch (Exception ex) {
            logger.error("Error al eliminar mensajes", ex);
            showError("Error al eliminar mensajes: " + ex.getMessage());
        }
    }

    /**
     * Muestra una notificación de éxito.
     */
    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /**
     * Muestra una notificación de advertencia.
     */
    private void showWarning(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
    }

    /**
     * Muestra una notificación de error.
     */
    private void showError(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
