package com.gr6.SmartCart.module_v3.recommendation.ai.dto;

import java.util.List;

public class AiTextRecommendRequest {

    private String seedText;
    private List<AiProductCandidate> candidates;
    private int page;
    private int size;

    public AiTextRecommendRequest() {
    }

    public AiTextRecommendRequest(
            String seedText,
            List<AiProductCandidate> candidates,
            int page,
            int size
    ) {
        this.seedText = seedText;
        this.candidates = candidates;
        this.page = page;
        this.size = size;
    }

    public String getSeedText() {
        return seedText;
    }

    public void setSeedText(String seedText) {
        this.seedText = seedText;
    }

    public List<AiProductCandidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<AiProductCandidate> candidates) {
        this.candidates = candidates;
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