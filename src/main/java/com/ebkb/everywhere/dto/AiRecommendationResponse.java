package com.ebkb.everywhere.dto;

import lombok.Data;
import java.util.List;

// 2-2. AI모델2 호출 API Response (AI -> BE) - 전체 응답 구조
@Data
public class AiRecommendationResponse {
    private String status;
    private String message;
    private List<AiRecommendationResult> data;
}