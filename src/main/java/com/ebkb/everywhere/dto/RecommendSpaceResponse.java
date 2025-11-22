package com.ebkb.everywhere.dto;

import lombok.Data;

// 1-2. 최적 공간 추천 요청 API Response (BE -> FE) - List 내부 객체
@Data
public class RecommendSpaceResponse {
    private Long spaceId;
    private String spaceName;
    private Double distanceKm; // BE 계산 결과
    private Double recommendationScore; // AI 모델 2 결과
}