package com.shopwise.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shopwise.app.dto.response.ProductResponse;
import com.shopwise.app.entity.Product;
import com.shopwise.app.mapper.ProductMapper;
import com.shopwise.app.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    @Mock ProductRepository productRepository;
    @Mock ProductMapper productMapper;
    @InjectMocks ProductServiceImpl service;

    @Test
    void getAllPreservesExistingCatalogBehavior() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Produit test");
        product.setPrice(new BigDecimal("12.50"));
        ProductResponse response = new ProductResponse();
        response.setId(1L);
        response.setName("Produit test");

        when(productRepository.findAll()).thenReturn(List.of(product));
        when(productMapper.toResponseList(List.of(product))).thenReturn(List.of(response));

        assertThat(service.getAll()).extracting(ProductResponse::getName).containsExactly("Produit test");
    }
}
