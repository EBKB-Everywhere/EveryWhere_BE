package com.ebkb.everywhere.service;

import com.ebkb.everywhere.config.AppConfig;
import com.ebkb.everywhere.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.lang.Math;

@Service
public class AiServerService {

    private final RestTemplate restTemplate;
    private final String aiServerUrl;

    // AI 서버 내부 호출 URL
    private static final String AI_PREDICT_COUNT_URL = "/api/v1/congestion";
    private static final String AI_RECOMMENDATION_URL = "/api/v1/recommendation"; // AI 모델 2

    // 🌟 1. 하드코딩된 공간 정보를 담는 내부 클래스 정의 (유지)
    public static class CandidateSpaceData {
        public Long spaceId;
        public String spaceName;
        public Double spaceLat;
        public Double spaceLon;
        public Integer spaceCapacity;
        public Double quiteScore;
        public Double talkScore;
        public Double studyScore;
        public Double restScore;

        public CandidateSpaceData(Long spaceId, String spaceName, Double spaceLat, Double spaceLon,
                                  Integer spaceCapacity, Double quiteScore, Double talkScore,
                                  Double studyScore, Double restScore) {
            this.spaceId = spaceId;
            this.spaceName = spaceName;
            this.spaceLat = spaceLat;
            this.spaceLon = spaceLon;
            this.spaceCapacity = spaceCapacity;
            this.quiteScore = quiteScore;
            this.talkScore = talkScore;
            this.studyScore = studyScore;
            this.restScore = restScore;
        }
    }

    @Autowired
    public AiServerService(RestTemplate restTemplate, AppConfig appConfig) {
        this.restTemplate = restTemplate;
        // application.properties에서 8001 포트 설정 확인
        this.aiServerUrl = appConfig.getAiServerUrl();
    }

    // 2-1. AI 모델 1 호출 (인원수 조회) (유지)
    public AiPredictCountResponse callAiModel1(Long spaceId) {
        AiPredictCountRequest request = new AiPredictCountRequest();
        request.setSpaceId(spaceId);
        request.setImagePath("/path/to/image");
        request.setBluetooth(10);
        request.setAudioFile(null);

        String url = aiServerUrl + AI_PREDICT_COUNT_URL;
        return restTemplate.postForObject(url, request, AiPredictCountResponse.class);
    }

    // 2-2. AI 모델 2 호출 (공간 추천) (유지)
    public AiRecommendationResponse callAiModel2(AiRecommendationRequest request) {
        String url = aiServerUrl + AI_RECOMMENDATION_URL;
        return restTemplate.postForObject(url, request, AiRecommendationResponse.class);
    }

    // 3-1. 거리 계산 (Haversine 공식) 구현 (유지)
    public double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    // 거리 기반 AI Feature 스코어 (유지)
    public double calculateDistanceFeature(double distanceKm) {
        return Math.max(0.0, 1.0 - (distanceKm / 5.0));
    }

    // 하드코딩된 공간 정보 목록 제공 메서드 (유지)
    public List<CandidateSpaceData> getAllCandidateSpaces() {
        return Arrays.asList(
                new CandidateSpaceData(201L, "마태오관 104호", 37.5526, 126.9392, 60, 0.0, 1.0, 1.0, 0.0),
                new CandidateSpaceData(202L, "마태오관 101호", 37.5526, 126.9392, 20, 1.0, 0.0, 0.0, 1.0),
                new CandidateSpaceData(203L, "금호아시아나바오로경영관 1층 라운지", 37.5524, 126.9388, 55, 1.0, 0.0, 1.0, 0.0),
                new CandidateSpaceData(204L, "삼성가브리엘관 2층 라운지", 37.5521, 126.9390, 18, 1.0, 0.0, 1.0, 0.0),
                new CandidateSpaceData(205L, "정하상관 J 열람실 앞 소파", 37.5504, 126.9430, 6, 1.0, 0.0, 0.0, 1.0),
                new CandidateSpaceData(206L, "게페르트남덕우경제관 계단1-2층", 37.5504, 126.9398, 30, 0.0, 1.0, 0.0, 1.0),
                new CandidateSpaceData(207L, "로욜라도서관 꿈꾸는숲(숙면공간)", 37.5515, 126.9418, 15, 1.0, 0.0, 0.0, 1.0),
                new CandidateSpaceData(208L, "다산관 1층", 37.5521, 126.9432, 40, 1.0, 0.0, 1.0, 0.0),
                new CandidateSpaceData(209L, "베르크만스우정원 2층", 37.5505, 126.9390, 40, 1.0, 0.0, 1.0, 0.0)
        );
    }
}