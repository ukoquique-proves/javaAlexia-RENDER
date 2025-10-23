package com.alexia.views;

import com.alexia.entity.Business;
import com.alexia.service.BusinessService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "businesses", layout = MainLayout.class)
@PageTitle("Negocios | Alexia")
public class BusinessesView extends VerticalLayout {

    private final BusinessService businessService;
    private final Grid<Business> grid;
    private TextField searchField;

    public BusinessesView(BusinessService businessService) {
        this.businessService = businessService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2(VaadinIcon.SHOP.create(), new com.vaadin.flow.component.html.Span(" Gestión de Negocios"));
        Paragraph description = new Paragraph("Crea, edita y gestiona los negocios registrados en el sistema.");

        HorizontalLayout toolbar = createToolbar();

        grid = createGrid();
        refreshGrid();

        add(title, description, toolbar, grid);
    }

    private HorizontalLayout createToolbar() {
        searchField = new TextField();
        searchField.setPlaceholder("Buscar por nombre o categoría...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("350px");
        searchField.addValueChangeListener(e -> filterBusinesses());

        Button newBusinessBtn = new Button("Nuevo Negocio", VaadinIcon.PLUS.create());
        newBusinessBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newBusinessBtn.addClickListener(e -> openBusinessDialog(null));

        HorizontalLayout toolbar = new HorizontalLayout(searchField, newBusinessBtn);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        return toolbar;
    }

    private Grid<Business> createGrid() {
        Grid<Business> grid = new Grid<>(Business.class, false);
        grid.setSizeFull();

        grid.addColumn(Business::getName).setHeader("Nombre").setSortable(true).setFlexGrow(2);
        grid.addColumn(Business::getCategory).setHeader("Categoría").setSortable(true);
        grid.addColumn(Business::getPhone).setHeader("Teléfono");
        grid.addColumn(Business::getAddress).setHeader("Dirección").setFlexGrow(2);

        grid.addColumn(new ComponentRenderer<>(business -> {
            com.vaadin.flow.component.html.Span badge = new com.vaadin.flow.component.html.Span(
                business.getIsActive() ? "Activo" : "Inactivo"
            );
            badge.getElement().getThemeList().add(
                business.getIsActive() ? "badge success" : "badge error"
            );
            return badge;
        })).setHeader("Estado");

        grid.addColumn(new ComponentRenderer<>(business -> {
            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editBtn.addClickListener(e -> openBusinessDialog(business));

            Button deleteBtn = new Button(VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e -> deleteBusiness(business));

            return new HorizontalLayout(editBtn, deleteBtn);
        })).setHeader("Acciones").setFlexGrow(0);

        return grid;
    }

    private void filterBusinesses() {
        String searchTerm = searchField.getValue().trim();
        if (searchTerm.isEmpty()) {
            refreshGrid();
        } else {
            List<Business> byName = businessService.searchByName(searchTerm);
            List<Business> byCategory = businessService.searchByCategory(searchTerm);
            byName.addAll(byCategory);
            grid.setItems(byName.stream().distinct().toList());
        }
    }

    private void openBusinessDialog(Business business) {
        Dialog dialog = new Dialog();
        boolean isEdit = business != null;
        H3 dialogTitle = new H3(isEdit ? "Editar Negocio" : "Nuevo Negocio");

        TextField nameField = new TextField("Nombre");
        nameField.setRequiredIndicatorVisible(true);
        TextField categoryField = new TextField("Categoría");
        TextField addressField = new TextField("Dirección");
        TextField phoneField = new TextField("Teléfono");

        if (isEdit) {
            nameField.setValue(business.getName());
            categoryField.setValue(business.getCategory() != null ? business.getCategory() : "");
            addressField.setValue(business.getAddress() != null ? business.getAddress() : "");
            phoneField.setValue(business.getPhone() != null ? business.getPhone() : "");
        }

        FormLayout formLayout = new FormLayout(nameField, categoryField, phoneField, addressField);
        formLayout.setColspan(addressField, 2);

        Button saveBtn = new Button("Guardar", e -> {
            if (nameField.isEmpty()) {
                Notification.show("El nombre es obligatorio", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            Business businessToSave = isEdit ? business : new Business();
            businessToSave.setName(nameField.getValue());
            businessToSave.setCategory(categoryField.getValue());
            businessToSave.setAddress(addressField.getValue());
            businessToSave.setPhone(phoneField.getValue());
            businessToSave.setIsActive(true); // Always active on save

            try {
                businessService.saveBusiness(businessToSave);
                Notification.show(isEdit ? "Negocio actualizado" : "Negocio creado", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                refreshGrid();
            } catch (Exception ex) {
                Notification.show("Error al guardar: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancelar", e -> dialog.close());

        dialog.add(new VerticalLayout(dialogTitle, formLayout, new HorizontalLayout(saveBtn, cancelBtn)));
        dialog.open();
    }

    private void deleteBusiness(Business business) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.add(new H3("Confirmar eliminación"));
        confirmDialog.add(new Paragraph("¿Estás seguro de que deseas eliminar el negocio '" + business.getName() + "'? Esta acción es reversible (soft delete)."));

        Button confirmBtn = new Button("Eliminar", e -> {
            businessService.deleteBusiness(business.getId());
            Notification.show("Negocio eliminado", 3000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            confirmDialog.close();
            refreshGrid();
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelBtn = new Button("Cancelar", e -> confirmDialog.close());

        confirmDialog.add(new HorizontalLayout(confirmBtn, cancelBtn));
        confirmDialog.open();
    }

    private void refreshGrid() {
        grid.setItems(businessService.getAllActiveBusinesses());
    }
}
