package com.shopwise.app.sale.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class CreateSaleRequest {
    @NotEmpty
    @Size(max = 100)
    private List<@Valid SaleItemRequest> items;

    public CreateSaleRequest() {
    }

    public CreateSaleRequest(List<SaleItemRequest> items) {
        this.items = items;
    }

    public List<SaleItemRequest> getItems() {
        return items;
    }

    public void setItems(List<SaleItemRequest> items) {
        this.items = items;
    }
}
