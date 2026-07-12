package com.shopwise.app.sale.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shopwise.app.entity.Product;
import com.shopwise.app.exception.NotFoundException;
import com.shopwise.app.repository.ProductRepository;
import com.shopwise.app.sale.dto.CreateSaleRequest;
import com.shopwise.app.sale.dto.SaleItemRequest;
import com.shopwise.app.sale.entity.Sale;
import com.shopwise.app.sale.repository.SaleRepository;

@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest {
    @Mock SaleRepository saleRepository;
    @Mock ProductRepository productRepository;
    @InjectMocks SaleServiceImpl service;

    @Test
    void createAggregatesDuplicateLinesAndCalculatesTotalFromCatalogPrice() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Livre");
        product.setPrice(new BigDecimal("10.50"));
        when(productRepository.findAllById(any())).thenReturn(List.of(product));
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new CreateSaleRequest(List.of(
                new SaleItemRequest(1L, 2), new SaleItemRequest(1L, 1)));
        var response = service.create(request, "merchant");

        assertThat(response.getTotal()).isEqualByComparingTo("31.50");
        assertThat(response.getLines()).hasSize(1);
        assertThat(response.getLines().getFirst().getQuantity()).isEqualTo(3);
        assertThat(response.getCreatedBy()).isEqualTo("merchant");
    }

    @Test
    void createRejectsUnknownProduct() {
        when(productRepository.findAllById(any())).thenReturn(List.of());
        var request = new CreateSaleRequest(List.of(new SaleItemRequest(404L, 1)));

        assertThatThrownBy(() -> service.create(request, "merchant"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("404");
    }
}
