package com.ebkb.everywhere.dto;

import lombok.Data;

// 2-2. AI모델2 호출 API Response (AI -> BE) - Data List 내부 객체
@Data
public class AiRecommendationResult {
    private Long spaceId;
    private Double finalRecommendScore;
}