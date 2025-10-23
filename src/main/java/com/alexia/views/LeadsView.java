package com.alexia.views;

import com.alexia.entity.Lead;
import com.alexia.service.LeadService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "leads", layout = MainLayout.class)
@PageTitle("Leads | Alexia")
public class LeadsView extends VerticalLayout {

    private final LeadService leadService;
    private final Grid<Lead> grid;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private ComboBox<String> sourceFilter;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public LeadsView(LeadService leadService) {
        this.leadService = leadService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2(VaadinIcon.USERS.create(), new Span(" Gestión de Leads"));
        Paragraph description = new Paragraph("Leads capturados con consentimiento GDPR/LGPD desde Telegram, WhatsApp y otros canales.");

        HorizontalLayout toolbar = createToolbar();
        grid = createGrid();
        refreshGrid();

        add(title, description, toolbar, grid);
    }

    private HorizontalLayout createToolbar() {
        searchField = new TextField();
        searchField.setPlaceholder("Buscar por nombre...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("300px");
        searchField.addValueChangeListener(e -> filterLeads());

        statusFilter = new ComboBox<>("Estado");
        statusFilter.setItems("Todos", "new", "contacted", "qualified", "converted", "lost", "archived");
        statusFilter.setValue("Todos");
        statusFilter.setWidth("150px");
        statusFilter.addValueChangeListener(e -> filterLeads());

        sourceFilter = new ComboBox<>("Fuente");
        sourceFilter.setItems("Todos", "telegram", "whatsapp", "web", "organic", "data_alexia");
        sourceFilter.setValue("Todos");
        sourceFilter.setWidth("150px");
        sourceFilter.addValueChangeListener(e -> filterLeads());

        Button refreshBtn = new Button("Actualizar", VaadinIcon.REFRESH.create());
        refreshBtn.addClickListener(e -> refreshGrid());

        HorizontalLayout toolbar = new HorizontalLayout(searchField, statusFilter, sourceFilter, refreshBtn);
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.END);

        return toolbar;
    }

    private Grid<Lead> createGrid() {
        Grid<Lead> grid = new Grid<>(Lead.class, false);
        grid.setSizeFull();

        grid.addColumn(Lead::getId).setHeader("ID").setWidth("80px").setSortable(true);
        
        grid.addColumn(lead -> lead.getFullName()).setHeader("Nombre").setFlexGrow(2).setSortable(true);
        
        grid.addColumn(Lead::getPhone).setHeader("Teléfono").setFlexGrow(1);
        grid.addColumn(Lead::getEmail).setHeader("Email").setFlexGrow(2);
        
        grid.addColumn(new ComponentRenderer<>(lead -> {
            Span badge = new Span(translateStatus(lead.getStatus()));
            badge.getElement().getThemeList().add("badge");
            
            switch (lead.getStatus()) {
                case "new":
                    badge.getElement().getThemeList().add("primary");
                    break;
                case "contacted":
                    badge.getElement().getThemeList().add("contrast");
                    break;
                case "qualified":
                    badge.getElement().getThemeList().add("success");
                    break;
                case "converted":
                    badge.getElement().getThemeList().add("success");
                    break;
                case "lost":
                    badge.getElement().getThemeList().add("error");
                    break;
                case "archived":
                    badge.getElement().getThemeList().add("");
                    break;
            }
            return badge;
        })).setHeader("Estado").setWidth("120px");

        grid.addColumn(new ComponentRenderer<>(lead -> {
            Span badge = new Span(translateSource(lead.getSource()));
            badge.getElement().getThemeList().add("badge");
            return badge;
        })).setHeader("Fuente").setWidth("120px");

        grid.addColumn(new ComponentRenderer<>(lead -> {
            Span badge = new Span(lead.getConsentGiven() ? "✓ Sí" : "✗ No");
            badge.getElement().getThemeList().add("badge");
            if (lead.getConsentGiven()) {
                badge.getElement().getThemeList().add("success");
            } else {
                badge.getElement().getThemeList().add("error");
            }
            return badge;
        })).setHeader("Consentimiento").setWidth("140px");

        grid.addColumn(lead -> lead.getCreatedAt().format(DATE_FORMATTER))
            .setHeader("Fecha Creación").setWidth("150px").setSortable(true);

        grid.addColumn(new ComponentRenderer<>(lead -> {
            Button viewBtn = new Button(VaadinIcon.EYE.create());
            viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            viewBtn.addClickListener(e -> openLeadDialog(lead));

            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editBtn.addClickListener(e -> openEditDialog(lead));

            return new HorizontalLayout(viewBtn, editBtn);
        })).setHeader("Acciones").setWidth("120px").setFlexGrow(0);

        return grid;
    }

    private void filterLeads() {
        String searchTerm = searchField.getValue().trim();
        String status = statusFilter.getValue();
        String source = sourceFilter.getValue();

        List<Lead> leads = leadService.getActiveLeads();

        // Filter by status
        if (!"Todos".equals(status)) {
            leads = leads.stream()
                .filter(lead -> status.equals(lead.getStatus()))
                .toList();
        }

        // Filter by source
        if (!"Todos".equals(source)) {
            leads = leads.stream()
                .filter(lead -> source.equals(lead.getSource()))
                .toList();
        }

        // Filter by search term
        if (!searchTerm.isEmpty()) {
            leads = leads.stream()
                .filter(lead -> lead.getFullName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                               (lead.getEmail() != null && lead.getEmail().toLowerCase().contains(searchTerm.toLowerCase())) ||
                               (lead.getPhone() != null && lead.getPhone().contains(searchTerm)))
                .toList();
        }

        grid.setItems(leads);
    }

    private void openLeadDialog(Lead lead) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");

        H3 dialogTitle = new H3("Detalles del Lead");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        content.add(createInfoRow("ID:", String.valueOf(lead.getId())));
        content.add(createInfoRow("Nombre:", lead.getFullName()));
        content.add(createInfoRow("Teléfono:", lead.getPhone() != null ? lead.getPhone() : "-"));
        content.add(createInfoRow("Email:", lead.getEmail() != null ? lead.getEmail() : "-"));
        content.add(createInfoRow("Ciudad:", lead.getCity() != null ? lead.getCity() : "-"));
        content.add(createInfoRow("País:", lead.getCountry()));
        content.add(createInfoRow("Estado:", translateStatus(lead.getStatus())));
        content.add(createInfoRow("Fuente:", translateSource(lead.getSource())));
        content.add(createInfoRow("Consentimiento:", lead.getConsentGiven() ? "✓ Otorgado" : "✗ No otorgado"));
        if (lead.getConsentDate() != null) {
            content.add(createInfoRow("Fecha Consentimiento:", lead.getConsentDate().format(DATE_FORMATTER)));
        }
        if (lead.getNotes() != null && !lead.getNotes().trim().isEmpty()) {
            content.add(createInfoRow("Notas:", lead.getNotes()));
        }
        content.add(createInfoRow("Creado:", lead.getCreatedAt().format(DATE_FORMATTER)));
        content.add(createInfoRow("Actualizado:", lead.getUpdatedAt().format(DATE_FORMATTER)));

        Button closeBtn = new Button("Cerrar", e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(new VerticalLayout(dialogTitle, content, closeBtn));
        dialog.open();
    }

    private void openEditDialog(Lead lead) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");

        H3 dialogTitle = new H3("Actualizar Estado del Lead");

        ComboBox<String> statusCombo = new ComboBox<>("Nuevo Estado");
        statusCombo.setItems("new", "contacted", "qualified", "converted", "lost", "archived");
        statusCombo.setValue(lead.getStatus());
        statusCombo.setItemLabelGenerator(this::translateStatus);
        statusCombo.setWidthFull();

        TextArea notesField = new TextArea("Notas");
        notesField.setValue(lead.getNotes() != null ? lead.getNotes() : "");
        notesField.setWidthFull();
        notesField.setHeight("150px");

        Button saveBtn = new Button("Guardar", e -> {
            try {
                leadService.updateLeadStatus(lead.getId(), statusCombo.getValue());
                
                if (!notesField.isEmpty()) {
                    Lead updatedLead = leadService.getLeadById(lead.getId()).orElseThrow();
                    updatedLead.setNotes(notesField.getValue());
                    leadService.updateLead(lead.getId(), updatedLead);
                }
                
                Notification.show("Lead actualizado exitosamente", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                refreshGrid();
            } catch (Exception ex) {
                Notification.show("Error al actualizar: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancelar", e -> dialog.close());

        FormLayout form = new FormLayout(statusCombo, notesField);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        dialog.add(new VerticalLayout(dialogTitle, form, new HorizontalLayout(saveBtn, cancelBtn)));
        dialog.open();
    }

    private HorizontalLayout createInfoRow(String label, String value) {
        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("font-weight", "bold").set("min-width", "180px");
        
        Span valueSpan = new Span(value);
        
        HorizontalLayout row = new HorizontalLayout(labelSpan, valueSpan);
        row.setWidthFull();
        row.setAlignItems(Alignment.BASELINE);
        
        return row;
    }

    private void refreshGrid() {
        grid.setItems(leadService.getActiveLeads());
    }

    private String translateStatus(String status) {
        return switch (status) {
            case "new" -> "Nuevo";
            case "contacted" -> "Contactado";
            case "qualified" -> "Calificado";
            case "converted" -> "Convertido";
            case "lost" -> "Perdido";
            case "archived" -> "Archivado";
            default -> status;
        };
    }

    private String translateSource(String source) {
        return switch (source) {
            case "telegram" -> "Telegram";
            case "whatsapp" -> "WhatsApp";
            case "web" -> "Web";
            case "organic" -> "Orgánico";
            case "data_alexia" -> "Data Alexia";
            default -> source;
        };
    }
}

