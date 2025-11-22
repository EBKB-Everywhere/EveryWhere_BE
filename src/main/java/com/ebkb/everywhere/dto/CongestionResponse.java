package com.ebkb.everywhere.dto;

import lombok.Data;

// 1-1. 인원수 조회 API Response (BE -> FE)
@Data
public class CongestionResponse {
    private Long spaceId;
    private String spaceName;
    private Double latitude;
    private Double longitude;
    private Integer predictCount;
}