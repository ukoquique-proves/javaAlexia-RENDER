package com.alexia.views;

import com.alexia.entity.Supplier;
import com.alexia.service.SupplierService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "suppliers", layout = MainLayout.class)
@PageTitle("Suppliers | Alexia")
@PermitAll
public class SuppliersView extends VerticalLayout {

    private final SupplierService supplierService;
    private final Grid<Supplier> grid = new Grid<>(Supplier.class);

    public SuppliersView(SupplierService supplierService) {
        this.supplierService = supplierService;
        addClassName("suppliers-view");
        setSizeFull();
        configureGrid();
        add(grid);
        updateList();
    }

    private void configureGrid() {
        grid.addClassName("supplier-grid");
        grid.setSizeFull();
        grid.setColumns("id", "name", "category", "deliveryTimeDays", "rating", "isVerified");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private void updateList() {
        grid.setItems(supplierService.getAllSuppliers());
    }
}
