package com.ebkb.everywhere.dto;

import lombok.Data;
import java.util.List;

// 2-2. AI모델2 호출 API Request (BE -> AI)
@Data
public class AiRecommendationRequest {
    private Long userId;
    private String userText;
    private List<CandidateRoom> candidateRooms;

    @Data
    public static class CandidateRoom {
        private Long spaceId;
        private String spaceName;
        private Double purposeScore;
        private Double distanceFeature;
        private Integer predictCount;
        private Integer capacity;
        private Double quiet_score;
        private Double talk_score;
        private Double study_score;
        private Double rest_score;
    }
}