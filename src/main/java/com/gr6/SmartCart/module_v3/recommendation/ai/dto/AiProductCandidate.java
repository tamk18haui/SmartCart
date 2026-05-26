package com.gr6.SmartCart.module_v3.recommendation.ai.dto;

import java.util.ArrayList;
import java.util.List;

public class AiProductCandidate {

    private Long productId;
    private String text;
    private String imageUrl;
    private List<String> imageUrls = new ArrayList<>();

    private Long categoryId;
    private String categoryName;
    private String brand;
    private String productName;

    private Integer soldCount;
    private Double rating;
    private Long reviewCount;

    public AiProductCandidate() {
    }

    public AiProductCandidate(
            Long productId,
            String text,
            String imageUrl,
            List<String> imageUrls,
            Long categoryId,
            String categoryName,
            String brand,
            String productName,
            Integer soldCount,
            Double rating,
            Long reviewCount
    ) {
        this.productId = productId;
        this.text = text;
        this.imageUrl = imageUrl;
        this.imageUrls = imageUrls == null ? new ArrayList<>() : imageUrls;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.brand = brand;
        this.productName = productName;
        this.soldCount = soldCount;
        this.rating = rating;
        this.reviewCount = reviewCount;
    }

    public Long getProductId() {
        return productId;
    }

    public String getText() {
        return text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getBrand() {
        return brand;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getSoldCount() {
        return soldCount;
    }

    public Double getRating() {
        return rating;
    }

    public Long getReviewCount() {
        return reviewCount;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls == null ? new ArrayList<>() : imageUrls;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setSoldCount(Integer soldCount) {
        this.soldCount = soldCount;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public void setReviewCount(Long reviewCount) {
        this.reviewCount = reviewCount;
    }
}