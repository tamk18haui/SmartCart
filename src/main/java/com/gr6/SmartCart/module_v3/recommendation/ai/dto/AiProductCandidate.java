package com.gr6.SmartCart.module_v3.recommendation.ai.dto;

public class AiProductCandidate {

    private Long productId;
    private String text;
    private String imageUrl;
    private Integer soldCount;
    private Double rating;
    private Long reviewCount;

    public AiProductCandidate() {
    }

    public AiProductCandidate(
            Long productId,
            String text,
            String imageUrl,
            Integer soldCount,
            Double rating,
            Long reviewCount
    ) {
        this.productId = productId;
        this.text = text;
        this.imageUrl = imageUrl;
        this.soldCount = soldCount;
        this.rating = rating;
        this.reviewCount = reviewCount;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(Integer soldCount) {
        this.soldCount = soldCount;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Long getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Long reviewCount) {
        this.reviewCount = reviewCount;
    }
}