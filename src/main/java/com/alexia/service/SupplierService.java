package com.alexia.service;

import com.alexia.entity.Supplier;
import com.alexia.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public List<Supplier> findSuppliersByCategory(String category) {
        return supplierRepository.findByCategory(category);
    }

    public Supplier saveSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    public List<Supplier> findAndCompareSuppliers(String productName) {
        List<Supplier> suppliers = supplierRepository.findByProduct(productName);
        return suppliers.stream()
                .sorted(Comparator.comparing(s -> getPriceForProduct(s, productName)))
                .collect(Collectors.toList());
    }

    private BigDecimal getPriceForProduct(Supplier supplier, String productName) {
        Object price = supplier.getProducts().get(productName);
        if (price instanceof Number) {
            return new BigDecimal(price.toString());
        }
        return BigDecimal.ZERO;
    }
}
