package com.ebkb.everywhere.dto;

import lombok.Data;

// 2-1. AI모델1 호출 API Request (BE -> AI)
@Data
public class AiPredictCountRequest {
    private Long spaceId;
    private String imagePath;
    private Integer bluetooth;
    private Object audioFile; // 단순 Object로 처리
}