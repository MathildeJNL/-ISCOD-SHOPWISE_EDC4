package com.shopwise.app.sale.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopwise.app.sale.entity.Sale;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findAllByOrderBySoldAtDescIdDesc();
}
