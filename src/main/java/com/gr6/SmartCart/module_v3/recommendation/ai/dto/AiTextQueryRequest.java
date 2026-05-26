package com.gr6.SmartCart.module_v3.recommendation.ai.dto;

import java.util.List;

public class AiTextQueryRequest {

    private String seedText;
    private List<Long> excludeProductIds;
    private List<Long> allowedProductIds;
    private int page;
    private int size;

    public AiTextQueryRequest() {
    }

    public AiTextQueryRequest(
            String seedText,
            List<Long> excludeProductIds,
            int page,
            int size
    ) {
        this(seedText, excludeProductIds, List.of(), page, size);
    }

    public AiTextQueryRequest(
            String seedText,
            List<Long> excludeProductIds,
            List<Long> allowedProductIds,
            int page,
            int size
    ) {
        this.seedText = seedText;
        this.excludeProductIds = excludeProductIds;
        this.allowedProductIds = allowedProductIds;
        this.page = page;
        this.size = size;
    }

    public String getSeedText() {
        return seedText;
    }

    public void setSeedText(String seedText) {
        this.seedText = seedText;
    }

    public List<Long> getExcludeProductIds() {
        return excludeProductIds;
    }

    public void setExcludeProductIds(List<Long> excludeProductIds) {
        this.excludeProductIds = excludeProductIds;
    }

    public List<Long> getAllowedProductIds() {
        return allowedProductIds;
    }

    public void setAllowedProductIds(List<Long> allowedProductIds) {
        this.allowedProductIds = allowedProductIds;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}