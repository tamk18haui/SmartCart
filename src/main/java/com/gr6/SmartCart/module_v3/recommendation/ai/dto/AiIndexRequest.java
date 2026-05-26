package com.gr6.SmartCart.module_v3.recommendation.ai.dto;

import java.util.List;

public class AiIndexRequest {

    private List<AiProductCandidate> candidates;

    public AiIndexRequest() {
    }

    public AiIndexRequest(List<AiProductCandidate> candidates) {
        this.candidates = candidates;
    }

    public List<AiProductCandidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<AiProductCandidate> candidates) {
        this.candidates = candidates;
    }
}