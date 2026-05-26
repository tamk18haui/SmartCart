package com.gr6.SmartCart.module_v3.recommendation.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiRecommendItem {
    private Long productId;
    private Double score;
    private String reason;
}