package com.gr6.SmartCart.module_v3.recommendation.ai.dto;

import java.util.List;

public class AiImageSearchRequest {

    private String fileName;
    private String contentType;
    private String imageBase64;
    private List<AiProductCandidate> candidates;
    private int page;
    private int size;

    public AiImageSearchRequest() {
    }

    public AiImageSearchRequest(
            String fileName,
            String contentType,
            String imageBase64,
            List<AiProductCandidate> candidates,
            int page,
            int size
    ) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.imageBase64 = imageBase64;
        this.candidates = candidates;
        this.page = page;
        this.size = size;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public List<AiProductCandidate> getCandidates() {
        return candidates;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public void setCandidates(List<AiProductCandidate> candidates) {
        this.candidates = candidates;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setSize(int size) {
        this.size = size;
    }
}