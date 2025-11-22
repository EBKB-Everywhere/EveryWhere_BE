# 🚀 EveryWhere AI 공간 추천 시스템 – Backend (Spring Boot)

AI 기반 캠퍼스 공간 추천 서비스의 백엔드(Spring Boot) 레포지토리입니다.
사용자의 목적·위치·혼잡도를 반영하여 최적의 공간을 추천하며,
FastAPI 기반 AI 서버와 통신하여 추론 결과를 통합합니다.

## 📌 1. 프로젝트 개요
### 🔧 기술 스택

Java 21

Spring Boot 3.x

Gradle

Lombok

FastAPI (AI 서버, Python)

### 🎯 주요 기능

클라이언트(React Native)에서 요청한 추천 API 제공

AI 서버(FastAPI)의 NLP/추천 모델과 통신

사용자 위치 기반 거리 계산 (Haversine 공식)

DB 없이 하드코딩된 공간 정보로 서비스 운영 (해커톤 최적화)

### 🔗 AI 서버 주소
http://localhost:8001

### 📁 2. 실행 환경 설정
2.1 application.properties 설정

src/main/resources/application.properties

server.port=8080

### AI 서버 주소
ai.server.url=http://localhost:8001

### JSON 파싱 오류 방지
spring.jackson.deserialization.fail-on-unknown-properties=false
spring.jackson.deserialization.accept-single-value-as-array=true

### ▶ 2.2 실행 방법
1) FastAPI AI 서버 실행
cd ai-server
uvicorn main:app --reload --port 8001

2) Spring Boot 실행

IntelliJ / VSCode에서 EverywhereApplication.java 실행
또는

./gradlew bootRun

### 📡 3. API 엔드포인트
3.1 인원수 조회 API (View 1)
Method	URL	설명
GET	/api/v1/congestion	특정 공간의 예측 인원수 조회
Request Params
필드	타입	예시
spaceId	Long	201
latitude	Double	37.5526
longitude	Double	126.9392

3.2 최적 공간 추천 API (View 2)
Method	URL	설명
POST	/api/v1/recommendation	사용자 목적·위치 기반 최적 공간 추천
Request Body (JSON)
필드	타입 (BE DTO)	예시	설명
userId	String	"1001"	문자열로 전달해야 파싱 오류 방지
currentLatitude	Double	37.5520	사용자 위도
currentLongitude	Double	126.9390	사용자 경도
currentFloor	String	"1"	현재 층 (문자열로 전달)
purpose	String	"study"	추천 목적 텍스트
### 🔄 4. BE ↔ AI 서버 통신 구조

4.1 FastAPI 호출 구조
목적	BE 메서드	FastAPI 엔드포인트
혼잡도 예측 (Model 1)	aiServerService.callAiModel1()	POST /ai/predict/count
공간 추천 (NLP + Model2)	aiServerService.callAiModel2()	POST /api/internal/ai/recommendation

4.2 공간 추천 처리 흐름

Spring Boot가 사용자 위치를 기반으로
모든 공간과의 거리(Haversine) 를 계산

공간 특징 + 거리 + 사용자 목적을 조합하여
AiRecommendationRequest DTO 생성

FastAPI에 전달 → NLP 기반 purposeScore 계산

모델2 가중치로 추천 점수 최종 산출

Spring Boot가 응답을 정렬하여 클라이언트에 반환

### 📘 5. Swagger UI

서버 실행 중 아래 URL 접속:

http://localhost:8080/swagger-ui.html
