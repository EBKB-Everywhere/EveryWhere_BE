from fastapi import FastAPI
from pydantic import BaseModel
from typing import List, Optional, Dict, Any

# ==================== DTO 정의 ====================

# 2-1. AI모델1 호출 API Request (BE -> AI)
class AiPredictCountRequest(BaseModel):
    spaceId: int
    imagePath: str
    bluetooth: int
    audioFile: Optional[Any] # raw file은 Any로 처리

# 2-1. AI모델1 호출 API Response (AI -> BE)
class AiPredictCountResponse(BaseModel):
    spaceId: int
    predictCount: int

# 2-2. AI모델2 호출 API Request (BE -> AI) - List 내부 객체
class CandidateRoom(BaseModel):
    spaceId: int
    spaceName: str
    purposeScore: float
    distanceFeature: float
    predictCount: int
    capacity: int
    quiet_score: float
    talk_score: float
    study_score: float
    rest_score: float

# 2-2. AI모델2 호출 API Request (BE -> AI)
class AiRecommendationRequest(BaseModel):
    userId: int
    userText: str
    candidateRooms: List[CandidateRoom]

# 2-2. AI모델2 호출 API Response (AI -> BE) - Data List 내부 객체
class AiRecommendationResult(BaseModel):
    spaceId: int
    finalRecommendScore: float

# 2-2. AI모델2 호출 API Response (AI -> BE) - 전체 응답 구조
class AiRecommendationResponse(BaseModel):
    status: str
    message: str
    data: List[AiRecommendationResult]

# ==================== FastAPI App ====================

app = FastAPI()

# 2-1. AI 모델 1 호출 API (인원수 계산)
@app.post("/ai/predict/count", response_model=AiPredictCountResponse)
def predict_count(request: AiPredictCountRequest):
    """
    AI 모델 1 추론 (특정 공간의 예측 인원수 계산)
    """

    # **핵심 AI 로직 더미 (Fastest way)**
    # spaceId를 기반으로 단순하게 예측 인원수를 반환
    dummy_count = (request.spaceId % 5) + 5 # 5명 ~ 9명 사이

    return AiPredictCountResponse(
        spaceId=request.spaceId,
        predictCount=dummy_count
    )


# 2-2. AI 모델 2 호출 API (공간 추천 점수 계산)
@app.post("/api/internal/ai/recommendation", response_model=AiRecommendationResponse)
def get_recommendation_score(request: AiRecommendationRequest):
    """
    AI 모델 2 추론 (각 공간의 최종 추천 점수 계산)
    """

    results = []

    # **핵심 AI 로직 더미 (Fastest way)**
    for room in request.candidateRooms:
        # 단순 가중치 합산으로 최종 추천 점수 계산 (Fastest Mock)
        # 0.5 * 목적 점수 + 0.3 * 거리 점수 + 0.2 * (1 - 혼잡도)
        # 혼잡도 계산: (predictCount / capacity)를 0.0 ~ 1.0 사이로 정규화
        congestion = min(1.0, room.predictCount / room.capacity)
        congestion_score = 1.0 - congestion

        # 목적 점수와 거리 점수 사용
        final_score = (
            0.5 * room.purposeScore +
            0.3 * room.distanceFeature +
            0.2 * congestion_score +
            (room.study_score if request.userText == "개인 학습" else 0.0) # 목적에 따른 간단한 추가 반영
        )

        # 0.0 ~ 1.0 사이로 점수 보정
        final_score = max(0.0, min(1.0, final_score))

        results.append(AiRecommendationResult(
            spaceId=room.spaceId,
            finalRecommendScore=final_score
        ))

    return AiRecommendationResponse(
        status="200",
        message="AI 추천 점수 계산 완료",
        data=results
    )

if __name__ == "__main__":
    import uvicorn
    # uvicorn main:app --reload --port 8000
    uvicorn.run(app, host="0.0.0.0", port=8000)