package com.ebkb.everywhere.Controller;

import com.ebkb.everywhere.dto.*;
import com.ebkb.everywhere.service.AiServerService;

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
        response.setSpaceName("중앙 도서관 " + spaceId + "열람실"); // 더미 공간 이름
        response.setLatitude(latitude);
        response.setLongitude(longitude);
        response.setPredictCount(aiResponse.getPredictCount());

        return ResponseEntity.ok(response);
    }

    // 1-2. 최적 공간 추천 요청 API (뷰2 OUTPUT)
    @PostMapping("/recommendation")
    public ResponseEntity<List<RecommendSpaceResponse>> getRecommendation(
            @RequestBody RecommendationRequest request) {

        // 1. DB에서 공간 후보 목록 조회 (더미)
        List<AiServerService.CandidateRoomInfo> dbRooms = aiServerService.getCandidateRoomsFromDb();

        // 2. AI 모델 2 요청 DTO (AiRecommendationRequest) 구성 및 BE Feature (거리) 연산
        List<AiRecommendationRequest.CandidateRoom> candidateRooms = dbRooms.stream()
                .map(dbRoom -> {
                    double distanceKm = aiServerService.calculateDistanceKm(
                            request.getCurrentLatitude(), request.getCurrentLongitude(),
                            dbRoom.latitude, dbRoom.longitude);
                    double distanceFeature = aiServerService.calculateDistanceFeature(distanceKm);

                    // TODO: AI 모델 1을 순회하며 호출하여 predictCount를 가져와야 함.
                    // 시간 절약을 위해 여기서는 하드코딩된 더미 값을 사용하거나, AI 모델 1 호출 로직을 분리하는 것이 좋음.
                    // 여기서는 **더미 predictCount (10명)**를 사용합니다.
                    Integer dummyPredictCount = 10;

                    // TODO: NLP 모델의 purposeScore (목적 점수)도 여기서 가져와야 함.
                    // 여기서는 **더미 purposeScore (0.8)**를 사용합니다.
                    Double dummyPurposeScore = 0.8;

                    AiRecommendationRequest.CandidateRoom room = new AiRecommendationRequest.CandidateRoom();
                    room.setSpaceId(dbRoom.spaceId);
                    room.setSpaceName(dbRoom.spaceName);
                    room.setPurposeScore(dummyPurposeScore);
                    room.setDistanceFeature(distanceFeature);
                    room.setPredictCount(dummyPredictCount);
                    room.setCapacity(dbRoom.capacity);
                    room.setQuiet_score(dbRoom.quiet_score);
                    room.setTalk_score(dbRoom.talk_score);
                    room.setStudy_score(dbRoom.study_score);
                    room.setRest_score(dbRoom.rest_score);
                    return room;
                })
                .collect(Collectors.toList());

        AiRecommendationRequest aiRequest = new AiRecommendationRequest();
        aiRequest.setUserId(request.getUserId());
        aiRequest.setUserText(request.getPurpose());
        aiRequest.setCandidateRooms(candidateRooms);

        // 3. AI 모델 2 호출 (2-2)
        AiRecommendationResponse aiResponse = aiServerService.callAiModel2(aiRequest);

        // 4. AI 결과와 거리 정보를 통합하여 최종 응답 DTO 구성
        List<RecommendSpaceResponse> finalResponse = aiResponse.getData().stream()
                .map(aiResult -> {
                    AiRecommendationRequest.CandidateRoom originalRoom = candidateRooms.stream()
                            .filter(c -> c.getSpaceId().equals(aiResult.getSpaceId()))
                            .findFirst().orElse(null);

                    if (originalRoom == null) return null; // 오류 처리

                    double distanceKm = aiServerService.calculateDistanceKm(
                            request.getCurrentLatitude(), request.getCurrentLongitude(),
                            dbRooms.stream().filter(d -> d.spaceId.equals(aiResult.getSpaceId())).findFirst().get().latitude,
                            dbRooms.stream().filter(d -> d.spaceId.equals(aiResult.getSpaceId())).findFirst().get().longitude
                    );

                    RecommendSpaceResponse response = new RecommendSpaceResponse();
                    response.setSpaceId(aiResult.getSpaceId());
                    response.setSpaceName(originalRoom.getSpaceName());
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