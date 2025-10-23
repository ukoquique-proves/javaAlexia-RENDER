package com.alexia.views;

import com.alexia.entity.Business;
import com.alexia.entity.Product;
import com.alexia.service.BusinessService;
import com.alexia.service.ProductService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

@Route(value = "products", layout = MainLayout.class)
@PageTitle("Productos | Alexia")
public class ProductsView extends VerticalLayout {

    private final ProductService productService;
    private final BusinessService businessService;
    private final Grid<Product> grid;
    private ComboBox<Business> businessFilter;
    private TextField searchField;

    public ProductsView(ProductService productService, BusinessService businessService) {
        this.productService = productService;
        this.businessService = businessService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Header
        H2 title = new H2(VaadinIcon.PACKAGE.create(), new com.vaadin.flow.component.html.Span(" Catálogo de Productos"));
        Paragraph description = new Paragraph("Gestiona el catálogo de productos de todos los negocios registrados.");

        // Toolbar
        HorizontalLayout toolbar = createToolbar();

        // Grid
        grid = createGrid();
        grid.setItems(productService.getActiveProducts());

        add(title, description, toolbar, grid);
    }

    private HorizontalLayout createToolbar() {
        // Search field
        searchField = new TextField();
        searchField.setPlaceholder("Buscar productos...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("300px");
        searchField.addValueChangeListener(e -> filterProducts());

        // Business filter
        businessFilter = new ComboBox<>("Filtrar por negocio");
        businessFilter.setItems(businessService.getAllActiveBusinesses());
        businessFilter.setItemLabelGenerator(Business::getName);
        businessFilter.setWidth("250px");
        businessFilter.addValueChangeListener(e -> filterProducts());

        // Clear filters button
        Button clearFilters = new Button("Limpiar filtros", VaadinIcon.CLOSE_SMALL.create());
        clearFilters.addClickListener(e -> {
            searchField.clear();
            businessFilter.clear();
            grid.setItems(productService.getActiveProducts());
        });

        // New product button
        Button newProductBtn = new Button("Nuevo Producto", VaadinIcon.PLUS.create());
        newProductBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newProductBtn.addClickListener(e -> openProductDialog(null));

        HorizontalLayout toolbar = new HorizontalLayout(searchField, businessFilter, clearFilters, newProductBtn);
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.END);
        toolbar.setFlexGrow(1, searchField);

        return toolbar;
    }

    private Grid<Product> createGrid() {
        Grid<Product> grid = new Grid<>(Product.class, false);
        grid.setSizeFull();

        grid.addColumn(Product::getName).setHeader("Nombre").setSortable(true).setFlexGrow(2);
        grid.addColumn(product -> product.getBusiness() != null ? product.getBusiness().getName() : "N/A")
            .setHeader("Negocio").setSortable(true).setFlexGrow(1);
        grid.addColumn(Product::getCategory).setHeader("Categoría").setSortable(true);
        grid.addColumn(Product::getFormattedPrice).setHeader("Precio").setSortable(true);
        grid.addColumn(Product::getStock).setHeader("Stock").setSortable(true);
        
        grid.addColumn(new ComponentRenderer<>(product -> {
            com.vaadin.flow.component.html.Span badge = new com.vaadin.flow.component.html.Span(
                product.getIsActive() ? "Activo" : "Inactivo"
            );
            badge.getElement().getThemeList().add(
                product.getIsActive() ? "badge success" : "badge error"
            );
            return badge;
        })).setHeader("Estado");

        grid.addColumn(new ComponentRenderer<>(product -> {
            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editBtn.addClickListener(e -> openProductDialog(product));

            Button deleteBtn = new Button(VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e -> deleteProduct(product));

            return new HorizontalLayout(editBtn, deleteBtn);
        })).setHeader("Acciones").setFlexGrow(0);

        return grid;
    }

    private void filterProducts() {
        String searchTerm = searchField.getValue();
        Business selectedBusiness = businessFilter.getValue();

        List<Product> products;

        if (selectedBusiness != null && searchTerm != null && !searchTerm.isEmpty()) {
            // Filter by both business and search term
            products = productService.getActiveProductsByBusiness(selectedBusiness.getId()).stream()
                .filter(p -> p.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                           (p.getDescription() != null && p.getDescription().toLowerCase().contains(searchTerm.toLowerCase())))
                .toList();
        } else if (selectedBusiness != null) {
            // Filter by business only
            products = productService.getActiveProductsByBusiness(selectedBusiness.getId());
        } else if (searchTerm != null && !searchTerm.isEmpty()) {
            // Filter by search term only
            products = productService.searchProducts(searchTerm);
        } else {
            // No filters
            products = productService.getActiveProducts();
        }

        grid.setItems(products);
    }

    private void openProductDialog(Product product) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");

        boolean isEdit = product != null;
        H3 dialogTitle = new H3(isEdit ? "Editar Producto" : "Nuevo Producto");

        // Form fields
        ComboBox<Business> businessField = new ComboBox<>("Negocio");
        businessField.setItems(businessService.getAllActiveBusinesses());
        businessField.setItemLabelGenerator(Business::getName);
        businessField.setRequiredIndicatorVisible(true);

        TextField nameField = new TextField("Nombre");
        nameField.setRequiredIndicatorVisible(true);
        nameField.setWidthFull();

        TextArea descriptionField = new TextArea("Descripción");
        descriptionField.setWidthFull();

        TextField priceField = new TextField("Precio (COP)");
        priceField.setPrefixComponent(new com.vaadin.flow.component.html.Span("$"));

        TextField categoryField = new TextField("Categoría");
        categoryField.setPlaceholder("Ej: Vasos, Platos, Cubiertos");

        IntegerField stockField = new IntegerField("Stock");
        stockField.setValue(0);
        stockField.setMin(0);

        TextField imagesField = new TextField("URLs de Imágenes");
        imagesField.setPlaceholder("Separadas por comas");
        imagesField.setWidthFull();

        TextArea variantsField = new TextArea("Variantes (JSON)");
        variantsField.setPlaceholder("{\"sizes\": [\"S\", \"M\", \"L\"], \"colors\": [\"Rojo\", \"Azul\"]}");
        variantsField.setWidthFull();

        // Populate fields if editing
        if (isEdit) {
            businessField.setValue(product.getBusiness());
            nameField.setValue(product.getName());
            descriptionField.setValue(product.getDescription() != null ? product.getDescription() : "");
            priceField.setValue(product.getPrice() != null ? product.getPrice().toString() : "");
            categoryField.setValue(product.getCategory() != null ? product.getCategory() : "");
            stockField.setValue(product.getStock());
            
            if (product.getImages() != null && product.getImages().length > 0) {
                imagesField.setValue(String.join(", ", product.getImages()));
            }
            
            if (product.getVariants() != null && !product.getVariants().isEmpty()) {
                variantsField.setValue(product.getVariants().toString());
            }
        }

        FormLayout formLayout = new FormLayout();
        formLayout.add(businessField, nameField, descriptionField, priceField, categoryField, stockField, imagesField, variantsField);
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setColspan(descriptionField, 2);
        formLayout.setColspan(imagesField, 2);
        formLayout.setColspan(variantsField, 2);

        // Buttons
        Button saveBtn = new Button("Guardar", e -> {
            if (businessField.isEmpty() || nameField.isEmpty()) {
                Notification.show("Negocio y Nombre son obligatorios", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                Product productToSave = isEdit ? product : new Product();
                productToSave.setBusinessId(businessField.getValue().getId());
                productToSave.setName(nameField.getValue());
                productToSave.setDescription(descriptionField.getValue());
                
                if (!priceField.isEmpty()) {
                    productToSave.setPrice(new BigDecimal(priceField.getValue()));
                }
                
                productToSave.setCategory(categoryField.getValue());
                productToSave.setStock(stockField.getValue());
                
                // Parse images
                if (!imagesField.isEmpty()) {
                    String[] images = imagesField.getValue().split(",");
                    for (int i = 0; i < images.length; i++) {
                        images[i] = images[i].trim();
                    }
                    productToSave.setImages(images);
                }
                
                // Parse variants (simple approach - in production use proper JSON parser)
                if (!variantsField.isEmpty()) {
                    productToSave.setVariants(new HashMap<>());
                    // Note: In production, parse JSON properly with Jackson
                }

                if (isEdit) {
                    productService.updateProduct(product.getId(), productToSave);
                    Notification.show("Producto actualizado exitosamente", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else {
                    productService.createProduct(productToSave);
                    Notification.show("Producto creado exitosamente", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }

                dialog.close();
                refreshGrid();
            } catch (Exception ex) {
                Notification.show("Error al guardar: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancelar", e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(saveBtn, cancelBtn);
        buttons.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout dialogLayout = new VerticalLayout(dialogTitle, formLayout, buttons);
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void deleteProduct(Product product) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setWidth("400px");

        H3 title = new H3("Confirmar eliminación");
        Paragraph message = new Paragraph("¿Estás seguro de que deseas eliminar el producto \"" + product.getName() + "\"?");

        Button confirmBtn = new Button("Eliminar", e -> {
            productService.deleteProduct(product.getId());
            Notification.show("Producto eliminado exitosamente", 3000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            confirmDialog.close();
            refreshGrid();
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelBtn = new Button("Cancelar", e -> confirmDialog.close());

        HorizontalLayout buttons = new HorizontalLayout(confirmBtn, cancelBtn);
        buttons.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout layout = new VerticalLayout(title, message, buttons);
        layout.setPadding(true);

        confirmDialog.add(layout);
        confirmDialog.open();
    }

    private void refreshGrid() {
        filterProducts();
    }
}
