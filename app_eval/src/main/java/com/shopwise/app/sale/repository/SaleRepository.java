package com.shopwise.app.sale.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.shopwise.app.sale.entity.Sale;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    @EntityGraph(attributePaths = {"lines", "lines.product"})
    List<Sale> findAllByOrderBySoldAtDescIdDesc();

    @Query("select distinct s from Sale s left join fetch s.lines l left join fetch l.product")
    List<Sale> findAllWithLines();
}
