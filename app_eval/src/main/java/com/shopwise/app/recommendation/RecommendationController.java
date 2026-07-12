package com.shopwise.app.recommendation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/recommendations")
@Validated
public class RecommendationController {
    private final RecommendationEngine recommendationEngine;

    public RecommendationController(RecommendationEngine recommendationEngine) {
        this.recommendationEngine = recommendationEngine;
    }

    @GetMapping
    public ResponseEntity<List<RecommendationResponse>> recommend(
            @RequestParam(required = false) @Positive Long productId,
            @RequestParam(defaultValue = "5") @Min(1) @Max(50) int limit) {
        return ResponseEntity.ok(recommendationEngine.recommend(productId, limit));
    }
}
