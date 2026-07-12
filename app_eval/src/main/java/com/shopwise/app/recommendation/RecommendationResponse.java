package com.shopwise.app.recommendation;

public class RecommendationResponse {
    private Long productId;
    private String productName;
    private double score;
    private String reason;

    public RecommendationResponse(Long productId, String productName, double score, String reason) {
        this.productId = productId;
        this.productName = productName;
        this.score = score;
        this.reason = reason;
    }

    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public double getScore() { return score; }
    public String getReason() { return reason; }
}
