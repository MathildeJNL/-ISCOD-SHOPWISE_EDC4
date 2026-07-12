package com.shopwise.app.recommendation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopwise.app.entity.Product;
import com.shopwise.app.exception.NotFoundException;
import com.shopwise.app.repository.ProductRepository;
import com.shopwise.app.sale.entity.Sale;
import com.shopwise.app.sale.entity.SaleLine;
import com.shopwise.app.sale.repository.SaleRepository;

@Service
@Transactional(readOnly = true)
public class CollaborativeFilteringRecommendationEngine implements RecommendationEngine {
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;

    public CollaborativeFilteringRecommendationEngine(ProductRepository productRepository,
            SaleRepository saleRepository) {
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
    }

    @Override
    public List<RecommendationResponse> recommend(Long productId, int limit) {
        List<Product> catalog = productRepository.findAll();
        Product reference = null;
        if (productId != null) {
            for (Product product : catalog) {
                if (product.getId().equals(productId)) {
                    reference = product;
                    break;
                }
            }
            if (reference == null) {
                throw new NotFoundException("Produit " + productId + " introuvable");
            }
        }

        List<Sale> sales = saleRepository.findAll();
        Map<Long, Map<Long, Integer>> vectors = buildProductVectors(sales);
        Map<Long, Long> popularity = buildPopularity(sales);

        if (reference == null || !vectors.containsKey(reference.getId())) {
            return popularityRecommendations(catalog, popularity, productId, limit);
        }
        return similarityRecommendations(catalog, vectors, popularity, reference, limit);
    }

    private Map<Long, Map<Long, Integer>> buildProductVectors(List<Sale> sales) {
        // Pour chaque produit, on garde la quantité vendue dans chaque vente.
        Map<Long, Map<Long, Integer>> vectors = new HashMap<>();
        for (Sale sale : sales) {
            for (SaleLine line : sale.getLines()) {
                Long productId = line.getProduct().getId();
                Map<Long, Integer> productVector = vectors.get(productId);
                if (productVector == null) {
                    productVector = new HashMap<>();
                    vectors.put(productId, productVector);
                }
                productVector.put(sale.getId(), line.getQuantity());
            }
        }
        return vectors;
    }

    private Map<Long, Long> buildPopularity(List<Sale> sales) {
        Map<Long, Long> popularity = new HashMap<>();
        for (Sale sale : sales) {
            for (SaleLine line : sale.getLines()) {
                popularity.merge(line.getProduct().getId(), (long) line.getQuantity(), Long::sum);
            }
        }
        return popularity;
    }

    private List<RecommendationResponse> similarityRecommendations(List<Product> catalog,
            Map<Long, Map<Long, Integer>> vectors, Map<Long, Long> popularity,
            Product reference, int limit) {
        Map<Long, Integer> referenceVector = vectors.get(reference.getId());
        List<ScoredProduct> scored = new ArrayList<>();
        for (Product candidate : catalog) {
            if (candidate.getId().equals(reference.getId())) continue;
            double similarity = cosine(referenceVector, vectors.getOrDefault(candidate.getId(), Map.of()));
            scored.add(new ScoredProduct(candidate, similarity,
                    popularity.getOrDefault(candidate.getId(), 0L)));
        }
        scored.sort((first, second) -> {
            int scoreComparison = Double.compare(second.getScore(), first.getScore());
            if (scoreComparison != 0) return scoreComparison;
            int popularityComparison = Long.compare(second.getPopularity(), first.getPopularity());
            if (popularityComparison != 0) return popularityComparison;
            return first.getProduct().getName().compareTo(second.getProduct().getName());
        });

        List<RecommendationResponse> responses = new ArrayList<>();
        int numberOfResults = Math.min(limit, scored.size());
        for (int i = 0; i < numberOfResults; i++) {
            ScoredProduct item = scored.get(i);
            String reason = "Meilleure vente proposée en repli";
            if (item.getScore() > 0) {
                reason = "Souvent acheté dans les mêmes paniers que " + reference.getName();
            }
            responses.add(new RecommendationResponse(
                    item.getProduct().getId(), item.getProduct().getName(),
                    round(item.getScore()), reason));
        }
        return responses;
    }

    private List<RecommendationResponse> popularityRecommendations(List<Product> catalog,
            Map<Long, Long> popularity, Long excludedProductId, int limit) {
        long max = 0;
        for (Long quantity : popularity.values()) {
            if (quantity > max) max = quantity;
        }

        List<Product> candidates = new ArrayList<>();
        for (Product product : catalog) {
            if (excludedProductId == null || !product.getId().equals(excludedProductId)) {
                candidates.add(product);
            }
        }
        candidates.sort((first, second) -> {
            long firstQuantity = popularity.getOrDefault(first.getId(), 0L);
            long secondQuantity = popularity.getOrDefault(second.getId(), 0L);
            int quantityComparison = Long.compare(secondQuantity, firstQuantity);
            if (quantityComparison != 0) return quantityComparison;
            return first.getName().compareTo(second.getName());
        });

        List<RecommendationResponse> responses = new ArrayList<>();
        int numberOfResults = Math.min(limit, candidates.size());
        for (int i = 0; i < numberOfResults; i++) {
            Product product = candidates.get(i);
            long quantity = popularity.getOrDefault(product.getId(), 0L);
            double score = max == 0 ? 0 : quantity / (double) max;
            String reason = max == 0
                    ? "Nouveau produit du catalogue"
                    : "Produit parmi les meilleures ventes";
            responses.add(new RecommendationResponse(
                    product.getId(), product.getName(), round(score), reason));
        }
        return responses;
    }

    static double cosine(Map<Long, Integer> left, Map<Long, Integer> right) {
        // Plus le résultat est proche de 1, plus les deux produits sont vendus ensemble.
        if (left.isEmpty() || right.isEmpty()) return 0;
        double dot = 0;
        double leftNorm = 0;
        double rightNorm = 0;
        for (int value : left.values()) leftNorm += (double) value * value;
        for (int value : right.values()) rightNorm += (double) value * value;
        for (Map.Entry<Long, Integer> entry : left.entrySet()) {
            dot += (double) entry.getValue() * right.getOrDefault(entry.getKey(), 0);
        }
        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }

    private static double round(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }

    private static class ScoredProduct {
        private final Product product;
        private final double score;
        private final long popularity;

        ScoredProduct(Product product, double score, long popularity) {
            this.product = product;
            this.score = score;
            this.popularity = popularity;
        }

        Product getProduct() { return product; }
        double getScore() { return score; }
        long getPopularity() { return popularity; }
    }
}
