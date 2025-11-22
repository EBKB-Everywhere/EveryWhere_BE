이게 최종본이야?



# ═══════════════════════════════════════════════════════

# FastAPI AI Server - Port 8001 (최종 수정본)

# ═══════════════════════════════════════════════════════



import sys

import json

import math

from typing import List, Dict, Any, Optional

from fastapi import FastAPI, HTTPException

from pydantic import BaseModel, Field

from reco import recommend_rooms  # 추천 모델 함수



from google import genai

from google.genai import types



# ═══════════════════════════════════════════════════════

# 설정 및 하드코딩 데이터

# ═══════════════════════════════════════════════════════

MY_GEMINI_API_KEY = "AIzaSyDGuN4D3ZDvFWxii5D0U_-pn420C_EAx-k"  # Gemini API 키 설정 필요

app = FastAPI(title="AI Space Recommendation API")

client = genai.Client(api_key=MY_GEMINI_API_KEY)



# Spring Boot BE에서 하드코딩한 Space 데이터를 동일하게 적용

ALL_SPACE_DATA = [

    {"space_id": 201, "space_name": "마태오관 104호", "space_lat": 37.5526, "space_lon": 126.9392, "space_floor": 1, "space_capacity": 60, "quite_score": 0.0, "talk_score": 1.0, "study_score": 1.0, "rest_score": 0.0},

    {"space_id": 202, "space_name": "마태오관 101호", "space_lat": 37.5526, "space_lon": 126.9392, "space_floor": 1, "space_capacity": 20, "quite_score": 1.0, "talk_score": 0.0, "study_score": 0.0, "rest_score": 1.0},

    {"space_id": 203, "space_name": "금호아시아나바오로경영관 1층 라운지", "space_lat": 37.5524, "space_lon": 126.9388, "space_floor": 1, "space_capacity": 55, "quite_score": 1.0, "talk_score": 0.0, "study_score": 1.0, "rest_score": 0.0},

    {"space_id": 204, "space_name": "삼성가브리엘관 2층 라운지", "space_lat": 37.5521, "space_lon": 126.9390, "space_floor": 2, "space_capacity": 18, "quite_score": 1.0, "talk_score": 0.0, "study_score": 1.0, "rest_score": 0.0},

    {"space_id": 205, "space_name": "정하상관 J 열람실 앞 소파", "space_lat": 37.5504, "space_lon": 126.9430, "space_floor": 1, "space_capacity": 6, "quite_score": 1.0, "talk_score": 0.0, "study_score": 0.0, "rest_score": 1.0},

    {"space_id": 206, "space_name": "게페르트남덕우경제관 계단1-2층", "space_lat": 37.5504, "space_lon": 126.9398, "space_floor": 1, "space_capacity": 30, "quite_score": 0.0, "talk_score": 1.0, "study_score": 0.0, "rest_score": 1.0},

    {"space_id": 207, "space_name": "로욜라도서관 꿈꾸는숲(숙면공간)", "space_lat": 37.5515, "space_lon": 126.9418, "space_floor": 1, "space_capacity": 15, "quite_score": 1.0, "talk_score": 0.0, "study_score": 0.0, "rest_score": 1.0},

    {"space_id": 208, "space_name": "다산관 1층", "space_lat": 37.5521, "space_lon": 126.9432, "space_floor": 1, "space_capacity": 40, "quite_score": 1.0, "talk_score": 0.0, "study_score": 1.0, "rest_score": 0.0},

    {"space_id": 209, "space_name": "베르크만스우정원 2층", "space_lat": 37.5505, "space_lon": 126.9390, "space_floor": 2, "space_capacity": 40, "quite_score": 1.0, "talk_score": 0.0, "study_score": 1.0, "rest_score": 0.0}

]



# ═══════════════════════════════════════════════════════

# Pydantic 모델 정의 (Spring Boot DTO와 일치)

# ═══════════════════════════════════════════════════════



# 2-1. AI모델1 호출 API Request (BE -> AI)

class AiPredictCountRequest(BaseModel):

    spaceId: int

    imagePath: str

    bluetooth: int

    audioFile: Optional[Any]



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

    quiet_score: float = Field(alias="quite_score") # DB 스키마 오타에 맞춤

    talk_score: float

    study_score: float

    rest_score: float

    class Config:

        allow_population_by_field_name = True



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





# ═══════════════════════════════════════════════════════

# Gemini NLP 모델 (목적 점수 계산) 함수

# ═══════════════════════════════════════════════════════



GEMINI_SCHEMA: Dict[str, Any] = {

    "type": "OBJECT",

    "properties": {

        "topSpaces": {

            "type": "ARRAY",

            "items": {

                "type": "OBJECT",

                "properties": {

                    "spaceId": {"type": "INTEGER"},

                    "purposeScore": {"type": "NUMBER"},

                },

                "required": ["spaceId", "purposeScore"],

            },

        },

        "placeFlag": {

            "type": "INTEGER",

            "description": "실제 장소 언급 여부 (1/0)",

        },

        "placeName": {

            "type": "STRING",

            "description": "사용자가 말한 실제 장소명 (없으면 빈 문자열)",

        },

    },

    "required": ["topSpaces", "placeFlag", "placeName"],

}



def _call_gemini(

    user_text: str,

    spaces: List[Dict[str, Any]],

    top_n: int,

) -> Dict[str, Any]:

    """Gemini API 호출"""

    spaces_for_llm = [

        {

            "spaceId": s["space_id"],

            "vector": [

                s["quite_score"],

                s["talk_score"],

                s["study_score"],

                s["rest_score"],

            ],

        }

        for s in spaces

    ]

    spaces_json = json.dumps(spaces_for_llm, ensure_ascii=False)



    prompt = f"""

너는 캠퍼스 공간 추천 모델이다.



- spaces: 각 공간은 spaceId와 vector를 가진다.

  vector는 ["조용한", "대화하는", "공부하는", "휴식하는"] 순서의 점수이다.

- user_text: 한국어 문장.



1. user_text를 분석해서 위 4차원에 대한 intent_vector를 마음속으로 만든다.

2. 각 공간의 vector와 intent_vector 사이의 코사인 유사도를 계산해서 purposeScore로 사용한다.

3. purposeScore를 기준으로 내림차순 정렬하여 상위 {top_n}개 공간만

   topSpaces 배열에 넣는다.

   각 항목은 {{ "spaceId", "purposeScore" }} 만 포함해야 한다.

4. user_text 안에 실제 장소명이 언급되었는지 보고,

   - 언급되면 placeFlag = 1, placeName 에 대표 장소명을 문자열로 넣는다.

   - 아니면 placeFlag = 0, placeName = "".



! 위도/경도(lat/lng)는 절대 생성하지 마라.

! 출력은 내가 제공한 GEMINI_SCHEMA에 정확히 맞는 순수 JSON만 포함한다.

   자연어 설명은 포함하지 않는다.



spaces(JSON):

{spaces_json}



user_text:

\"\"\"{user_text}\"\"\"

"""



    resp = client.models.generate_content(

        model="gemini-2.5-flash",

        contents=prompt,

        config=types.GenerateContentConfig(

            response_mime_type="application/json",

            response_schema=GEMINI_SCHEMA,

        ),

    )

    return json.loads(resp.text)





def run_nlp_model(

    user_text: str,

    spaces: List[Dict[str, Any]],

) -> Dict[int, float]:

    """NLP 모델 실행 후 purposeScore 맵을 반환"""

    # spaces의 길이만큼 top_n 설정하여 모든 공간에 대해 점수를 계산하도록 요청

    gemini_res = _call_gemini(user_text, spaces, len(spaces))



    # spaceId: purposeScore 맵 생성

    purpose_score_map = {}

    for item in gemini_res.get("topSpaces", []):

        purpose_score_map[item["spaceId"]] = item["purposeScore"]



    return purpose_score_map



# ═══════════════════════════════════════════════════════

# API 엔드포인트

# ═══════════════════════════════════════════════════════



# 2-1. AI모델1 호출 API (인원수 계산)

@app.post("/ai/predict/count", response_model=AiPredictCountResponse)

async def predict_count_endpoint(request: AiPredictCountRequest):

    """

    AI 모델 1 (혼잡도 인원수 계산)

    """

    # **AI 로직 더미:** 요청된 spaceId를 기반으로 임의의 인원수 반환

    dummy_count = 10 + math.ceil(math.sin(request.spaceId * 10) * 5)



    return AiPredictCountResponse(

        spaceId=request.spaceId,

        predictCount=dummy_count

    )



# 2-2. AI모델2 호출 API (최종 추천 점수 계산)

@app.post("/api/internal/ai/recommendation", response_model=AiRecommendationResponse)

async def recommend_endpoint(request: AiRecommendationRequest):

    """

    AI 모델 2 (최종 추천 점수 계산) - NLP 통합

    """

    if not MY_GEMINI_API_KEY:

        raise HTTPException(status_code=500, detail="Gemini API 키가 설정되지 않았습니다")



    try:

        # 1. NLP 모델 실행: userText를 기반으로 모든 공간의 목적 점수를 계산

        purpose_score_map = run_nlp_model(request.userText, ALL_SPACE_DATA)



        # 2. BE에서 받은 후보 목록에 NLP 점수를 덮어쓰기 (Overwrite)

        candidate_rooms_dicts = []

        for room in request.candidateRooms:

            # Pydantic 모델을 딕셔너리로 변환 (by_alias=True는 'quite_score'가 'quiet_score'로 오더라도 처리)

            room_dict = room.dict(by_alias=True)

            space_id = room_dict["spaceId"]



            # NLP에서 계산된 목적 점수로 덮어쓰기

            calculated_purpose_score = purpose_score_map.get(space_id, 0.0)

            room_dict["purposeScore"] = calculated_purpose_score



            candidate_rooms_dicts.append(room_dict)



        # 3. 추천 모델(reco.py) 호출

        results = recommend_rooms(candidate_rooms_dicts)



        # 4. AiRecommendationResponse DTO에 맞게 결과 변환

        data = [

            AiRecommendationResult(

                spaceId=res["spaceId"],

                finalRecommendScore=res["finalRecommendScore"]

            ) for res in results

        ]



        return AiRecommendationResponse(

            status="200",

            message="AI 추천 점수 계산 완료 (NLP 통합)",

            data=data

        )



    except Exception as e:

        # 디버깅을 위해 오류 메시지를 상세히 출력

        raise HTTPException(status_code=500, detail=f"추천 모델 실행 오류: {str(e)}")





@app.get("/health")

async def health_check():

    """헬스 체크 엔드포인트"""

    return {"status": "healthy", "service": "AI Space Recommendation API"}



# ═══════════════════════════════════════════════════════

# 메인 실행

# ═══════════════════════════════════════════════════════

if __name__ == "__main__":

    import uvicorn

    # 💡 포트 8001로 실행

    uvicorn.run(app, host="0.0.0.0", port=8001)