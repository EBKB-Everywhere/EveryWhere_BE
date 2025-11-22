## FastAPI 실행
uvicorn main:app --reload --port 8001

1. FastAPI 설치: ai-server/ 디렉토리에서 필요한 라이브러리를 설치합니다.

Bash

pip install fastapi uvicorn pydantic
2. FastAPI 서버 실행: 8000 포트에서 서버를 실행합니다.

Bash

uvicorn main:app --reload --port 8000
3. FastAPI 더미 로직 검토: main.py에 작성된 AI 모델 1과 2의 **더미 로직(predict_count 및 get_recommendation_score)**이 Spring Boot에서 넘어오는 요청을 정상적으로 처리하고 더미 값을 반환하는지 확인합니다.