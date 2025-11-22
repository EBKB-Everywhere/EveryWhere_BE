package com.ebkb.everywhere.dto;

import lombok.Data;

// 1-1. 인원수 조회 API Request (FE -> BE)
@Data
public class CongestionRequest {
    private Long spaceId;
    private Double latitude;
    private Double longitude;
}