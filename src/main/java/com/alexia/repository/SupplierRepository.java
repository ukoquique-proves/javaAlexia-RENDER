package com.alexia.repository;

import com.alexia.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    List<Supplier> findByCategory(String category);

    @Query(value = "SELECT * FROM suppliers s WHERE s.products ->> :productName IS NOT NULL", nativeQuery = true)
    List<Supplier> findByProduct(@Param("productName") String productName);
}
