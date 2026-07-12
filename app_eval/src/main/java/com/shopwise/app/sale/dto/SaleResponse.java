package com.shopwise.app.sale.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SaleResponse {
    private Long id;
    private LocalDateTime soldAt;
    private String createdBy;
    private BigDecimal total;
    private List<SaleLineResponse> lines;

    public SaleResponse(Long id, LocalDateTime soldAt, String createdBy,
            BigDecimal total, List<SaleLineResponse> lines) {
        this.id = id;
        this.soldAt = soldAt;
        this.createdBy = createdBy;
        this.total = total;
        this.lines = lines;
    }

    public Long getId() { return id; }
    public LocalDateTime getSoldAt() { return soldAt; }
    public String getCreatedBy() { return createdBy; }
    public BigDecimal getTotal() { return total; }
    public List<SaleLineResponse> getLines() { return lines; }
}
