package com.shopwise.app.recommendation;

import java.util.List;

public interface RecommendationEngine {
    List<RecommendationResponse> recommend(Long productId, int limit);
}
