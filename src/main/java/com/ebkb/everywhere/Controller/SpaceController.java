package com.ebkb.everywhere.controller;

import com.ebkb.everywhere.dto.*;
import com.ebkb.everywhere.service.AiServerService;
import com.ebkb.everywhere.service.AiServerService.CandidateSpaceData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class SpaceController {

    private final AiServerService aiServerService;

    @Autowired
    public SpaceController(AiServerService aiServerService) {
        this.aiServerService = aiServerService;
    }

    // 1-1. 인원수 조회 API (뷰1 OUTPUT)
    @GetMapping("/congestion")
    public ResponseEntity<CongestionResponse> getCongestion(
            @RequestParam Long spaceId,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {

        // 1. AI 모델 1 호출 (2-1)
        AiPredictCountResponse aiResponse = aiServerService.callAiModel1(spaceId);

        // 2. Response DTO 구성
        CongestionResponse response = new CongestionResponse();
        response.setSpaceId(spaceId);

        // 공간 이름을 찾기 위한 간단한 로직 (하드코딩 데이터 기반)
        String spaceName = aiServerService.getAllCandidateSpaces().stream()
                .filter(s -> s.spaceId.equals(spaceId))
                .map(s -> s.spaceName)
                .findFirst()
                .orElse("알 수 없는 공간");

        response.setSpaceName(spaceName);
        response.setLatitude(latitude);
        response.setLongitude(longitude);
        response.setPredictCount(aiResponse.getPredictCount());

        return ResponseEntity.ok(response);
    }

    // 1-2. 최적 공간 추천 요청 API (뷰2 OUTPUT)
    @PostMapping("/recommendation")
    public ResponseEntity<List<RecommendSpaceResponse>> getRecommendation(
            @RequestBody RecommendationRequest request) {

        // 1. 하드코딩된 모든 공간 정보 목록 조회
        List<CandidateSpaceData> allSpaces = aiServerService.getAllCandidateSpaces();

        // 2. AI 모델 2 요청 DTO (AiRecommendationRequest) 구성 및 BE Feature (거리) 연산
        List<AiRecommendationRequest.CandidateRoom> candidateRooms = allSpaces.stream()
                .map(spaceData -> {
                    // 2-1. 거리 계산 및 Feature 변환
                    double distanceKm = aiServerService.calculateDistanceKm(
                            request.getCurrentLatitude(), request.getCurrentLongitude(),
                            spaceData.spaceLat, spaceData.spaceLon);
                    double distanceFeature = aiServerService.calculateDistanceFeature(distanceKm);

                    // 🌟 목적 점수는 AI 서버가 계산하도록 임시값 0.0을 보냄
                    Double dummyPurposeScore = 0.0;

                    // 2-2. 혼잡도 (AI Model 1) 호출은 시간상 생략하고 더미값 사용
                    Integer dummyPredictCount = 10;

                    AiRecommendationRequest.CandidateRoom room = new AiRecommendationRequest.CandidateRoom();
                    room.setSpaceId(spaceData.spaceId);
                    room.setSpaceName(spaceData.spaceName);
                    room.setPurposeScore(dummyPurposeScore); // 🌟 임시값 (AI 서버에서 덮어씀)
                    room.setDistanceFeature(distanceFeature); // BE 계산 결과 사용
                    room.setPredictCount(dummyPredictCount);
                    room.setCapacity(spaceData.spaceCapacity);
                    room.setQuiet_score(spaceData.quiteScore);
                    room.setTalk_score(spaceData.talkScore);
                    room.setStudy_score(spaceData.studyScore);
                    room.setRest_score(spaceData.restScore);
                    return room;
                })
                .collect(Collectors.toList());

        AiRecommendationRequest aiRequest = new AiRecommendationRequest();
        aiRequest.setUserId(request.getUserId());
        aiRequest.setUserText(request.getPurpose()); // 🌟 사용자 목적 텍스트를 AI 서버로 전달
        aiRequest.setCandidateRooms(candidateRooms);

        // 3. AI 모델 2 호출 (2-2)
        AiRecommendationResponse aiResponse = aiServerService.callAiModel2(aiRequest);

        // 4. AI 결과와 거리 정보를 통합하여 최종 응답 DTO 구성
        List<RecommendSpaceResponse> finalResponse = aiResponse.getData().stream()
                .map(aiResult -> {
                    // 원본 공간 정보 찾기
                    CandidateSpaceData originalSpace = allSpaces.stream()
                            .filter(d -> d.spaceId.equals(aiResult.getSpaceId()))
                            .findFirst()
                            .orElse(null);

                    if (originalSpace == null) return null;

                    // Response DTO에 포함할 거리(km) 재계산
                    double distanceKm = aiServerService.calculateDistanceKm(
                            request.getCurrentLatitude(), request.getCurrentLongitude(),
                            originalSpace.spaceLat,
                            originalSpace.spaceLon
                    );

                    RecommendSpaceResponse response = new RecommendSpaceResponse();
                    response.setSpaceId(aiResult.getSpaceId());
                    response.setSpaceName(originalSpace.spaceName);
                    response.setDistanceKm(distanceKm);
                    response.setRecommendationScore(aiResult.getFinalRecommendScore());
                    return response;
                })
                .filter(java.util.Objects::nonNull)
                // 최종 추천 점수가 높은 순으로 정렬
                .sorted(Comparator.comparing(RecommendSpaceResponse::getRecommendationScore).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(finalResponse);
    }
}