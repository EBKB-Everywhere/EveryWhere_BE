package com.ebkb.everywhere.dto;

import lombok.Data;

// 1-2. 최적 공간 추천 요청 API Request (FE -> BE)
@Data
public class RecommendationRequest {
    private Long userId;
    private Double currentLatitude;
    private Double currentLongitude;
    private Integer currentFloor;
    private String purpose;
    private Long timeTableId;
}