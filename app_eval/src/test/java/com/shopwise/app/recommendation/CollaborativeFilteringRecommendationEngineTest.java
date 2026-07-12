package com.shopwise.app.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

import java.util.Map;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

import com.shopwise.app.entity.Product;
import com.shopwise.app.repository.ProductRepository;
import com.shopwise.app.sale.repository.SaleRepository;

@ExtendWith(MockitoExtension.class)
class CollaborativeFilteringRecommendationEngineTest {
    @Mock ProductRepository productRepository;
    @Mock SaleRepository saleRepository;
    @InjectMocks CollaborativeFilteringRecommendationEngine engine;

    @Test
    void cosineDetectsIdenticalPurchasePatterns() {
        double score = CollaborativeFilteringRecommendationEngine.cosine(
                Map.of(1L, 2, 2L, 1), Map.of(1L, 2, 2L, 1));
        assertThat(score).isCloseTo(1.0, offset(1e-12));
    }

    @Test
    void cosineReturnsZeroWithoutCommonBasket() {
        double score = CollaborativeFilteringRecommendationEngine.cosine(
                Map.of(1L, 2), Map.of(2L, 4));
        assertThat(score).isZero();
    }

    @Test
    void coldStartReturnsCatalogInDeterministicOrder() {
        Product zebra = product(1L, "Zebra");
        Product alpha = product(2L, "Alpha");
        when(productRepository.findAll()).thenReturn(List.of(zebra, alpha));
        when(saleRepository.findAll()).thenReturn(List.of());

        var recommendations = engine.recommend(null, 2);

        assertThat(recommendations).extracting(RecommendationResponse::getProductName)
                .containsExactly("Alpha", "Zebra");
        assertThat(recommendations).allMatch(item -> item.getScore() == 0.0);
    }

    private Product product(Long id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        return product;
    }
}
