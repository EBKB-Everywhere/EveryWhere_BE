package com.ebkb.everywhere.service;

import com.ebkb.everywhere.config.AppConfig;
import com.ebkb.everywhere.dto.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class AiServerService {

    private final RestTemplate restTemplate;
    private final String aiServerUrl;

    // AI 서버 내부 호출 URL
    private static final String AI_PREDICT_COUNT_URL = "/ai/predict/count";
    private static final String AI_RECOMMENDATION_URL = "/api/internal/ai/recommendation";

    @Autowired
    public AiServerService(RestTemplate restTemplate, AppConfig appConfig) {
        this.restTemplate = restTemplate;
        this.aiServerUrl = appConfig.getAiServerUrl();
    }

    // 2-1. AI 모델 1 호출 (인원수 조회)
    public AiPredictCountResponse callAiModel1(Long spaceId) {
        // 실제로는 imagePath, bluetooth, audioFile 등을 DB/센서 데이터에서 조회해야 함
        AiPredictCountRequest request = new AiPredictCountRequest();
        request.setSpaceId(spaceId);
        // 하드코딩된 더미 데이터
        request.setImagePath("/path/to/image");
        request.setBluetooth(10);
        request.setAudioFile(null);

        // RestTemplate 호출
        String url = aiServerUrl + AI_PREDICT_COUNT_URL;
        return restTemplate.postForObject(url, request, AiPredictCountResponse.class);
    }

    // 2-2. AI 모델 2 호출 (공간 추천)
    public AiRecommendationResponse callAiModel2(AiRecommendationRequest request) {
        // RestTemplate 호출
        String url = aiServerUrl + AI_RECOMMENDATION_URL;
        return restTemplate.postForObject(url, request, AiRecommendationResponse.class);
    }

    // 3-1. 거리 계산 더미 로직 (간단한 예시)
    /**
     * @param lat1 사용자 위도
     * @param lon1 사용자 경도
     * @param lat2 공간 위도
     * @param lon2 공간 경도
     * @return 거리 (km)
     */
    public double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        // 실제 Haversine 공식 대신, 간단히 위도/경도 차이의 절대값을 합하여 더미 거리를 반환
        // 실제 해커톤에서는 Haversine 공식을 사용해야 함.
        final double R = 6371; // 지구 반지름 (km)

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    /**
     * 거리를 기반으로 AI Feature 스코어 (0.0 ~ 1.0) 계산 (더미)
     */
    public double calculateDistanceFeature(double distanceKm) {
        // 거리가 0에 가까울수록 1.0, 멀어질수록 0.0에 가까워지도록 단순화
        // 1km를 기준으로 계산 (최대 5km까지 영향)
        return Math.max(0.0, 1.0 - (distanceKm / 5.0));
    }

    /**
     * DB에서 공간 정보 목록을 가져오는 더미 메서드
     * 실제로는 JPA 등을 이용해 DB에서 조회
     */
    public List<CandidateRoomInfo> getCandidateRoomsFromDb() {
        // DB 테이블 구조와 유사한 더미 정보
        return Arrays.asList(
                new CandidateRoomInfo(101L, "중앙 도서관 1열람실", 37.5665, 126.9780, 50, 0.8, 0.1, 0.9, 0.2),
                new CandidateRoomInfo(102L, "A동 팀플실 203호", 37.5668, 126.9785, 10, 0.5, 0.7, 0.4, 0.3),
                new CandidateRoomInfo(103L, "교수회관 라운지", 37.5670, 126.9790, 30, 0.3, 0.9, 0.1, 0.8)
        );
    }

    // DB 테이블 구조를 흉내낸 클래스
    public static class CandidateRoomInfo {
        public Long spaceId;
        public String spaceName;
        public Double latitude;
        public Double longitude;
        public Integer capacity;
        public Double quiet_score;
        public Double talk_score;
        public Double study_score;
        public Double rest_score;

        public CandidateRoomInfo(Long spaceId, String spaceName, Double latitude, Double longitude,
                                 Integer capacity, Double quiet_score, Double talk_score,
                                 Double study_score, Double rest_score) {
            this.spaceId = spaceId;
            this.spaceName = spaceName;
            this.latitude = latitude;
            this.longitude = longitude;
            this.capacity = capacity;
            this.quiet_score = quiet_score;
            this.talk_score = talk_score;
            this.study_score = study_score;
            this.rest_score = rest_score;
        }
    }
}