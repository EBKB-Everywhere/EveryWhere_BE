package com.ebkb.everywhere.dto;

import lombok.Data;

// 2-1. AI모델1 호출 API Response (AI -> BE)
@Data
public class AiPredictCountResponse {
    private Long spaceId;
    private Integer predictCount;
}