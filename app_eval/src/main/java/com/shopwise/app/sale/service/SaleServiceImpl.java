package com.shopwise.app.sale.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopwise.app.entity.Product;
import com.shopwise.app.exception.NotFoundException;
import com.shopwise.app.repository.ProductRepository;
import com.shopwise.app.sale.dto.CreateSaleRequest;
import com.shopwise.app.sale.dto.SaleItemRequest;
import com.shopwise.app.sale.dto.SaleLineResponse;
import com.shopwise.app.sale.dto.SaleResponse;
import com.shopwise.app.sale.entity.Sale;
import com.shopwise.app.sale.entity.SaleLine;
import com.shopwise.app.sale.repository.SaleRepository;

@Service
@Transactional
public class SaleServiceImpl implements SaleService {
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    public SaleServiceImpl(SaleRepository saleRepository, ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
    }

    @Override
    public SaleResponse create(CreateSaleRequest request, String username) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (SaleItemRequest item : request.getItems()) {
            int currentQuantity = quantities.getOrDefault(item.getProductId(), 0);
            int newQuantity = currentQuantity + item.getQuantity();
            if (newQuantity > 9999) {
                throw new IllegalArgumentException(
                        "La quantité cumulée d'un produit ne peut pas dépasser 9999");
            }
            quantities.put(item.getProductId(), newQuantity);
        }

        Map<Long, Product> products = new LinkedHashMap<>();
        for (Product product : productRepository.findAllById(quantities.keySet())) {
            products.put(product.getId(), product);
        }

        List<Long> missingIds = new ArrayList<>();
        for (Long productId : quantities.keySet()) {
            if (!products.containsKey(productId)) {
                missingIds.add(productId);
            }
        }
        if (!missingIds.isEmpty()) {
            throw new NotFoundException("Produits introuvables : " + missingIds);
        }

        Sale sale = new Sale();
        sale.setCreatedBy(username);
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Product product = products.get(entry.getKey());
            sale.addLine(new SaleLine(product, entry.getValue()));
        }
        return toResponse(saleRepository.save(sale));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> getAll() {
        List<SaleResponse> responses = new ArrayList<>();
        for (Sale sale : saleRepository.findAllByOrderBySoldAtDescIdDesc()) {
            responses.add(toResponse(sale));
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponse getById(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vente " + id + " introuvable"));
        sale.getLines().size();
        return toResponse(sale);
    }

    private SaleResponse toResponse(Sale sale) {
        List<SaleLineResponse> lines = new ArrayList<>();
        for (SaleLine line : sale.getLines()) {
            SaleLineResponse response = new SaleLineResponse(
                    line.getProduct().getId(),
                    line.getProductName(),
                    line.getQuantity(),
                    line.getUnitPrice(),
                    line.getLineTotal());
            lines.add(response);
        }
        return new SaleResponse(sale.getId(), sale.getSoldAt(), sale.getCreatedBy(), sale.getTotal(), lines);
    }
}
