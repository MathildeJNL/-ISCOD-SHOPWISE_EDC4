package com.shopwise.app.sale.service;

import java.util.List;

import com.shopwise.app.sale.dto.CreateSaleRequest;
import com.shopwise.app.sale.dto.SaleResponse;

public interface SaleService {
    SaleResponse create(CreateSaleRequest request, String username);
    List<SaleResponse> getAll();
    SaleResponse getById(Long id);
}
