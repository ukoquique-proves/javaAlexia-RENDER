package com.alexia.repository;

import com.alexia.entity.ConnectionTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConnectionTestRepository extends JpaRepository<ConnectionTest, Long> {
}
