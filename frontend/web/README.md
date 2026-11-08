# P-bit Web

`frontend/web`: 웹 프론트엔드 (Vite + React + TypeScript)

## 요구 사항

- Node.js 20.19 이상 (또는 22.12 이상)

## 실행

```bash
cd frontend/web
cp .env.example .env
npm install
npm run dev
```

`http://localhost:5173`에서 열립니다.

## 스크립트

- `npm run dev`: 개발 서버
- `npm run build`: 타입 검사 + 빌드
- `npm run lint`: 린트
- `npm run preview`: 빌드 결과 미리보기

PR 전에 `npm run lint && npm run build`가 통과해야 합니다.

## 환경 변수

`VITE_API_BASE_URL`: 백엔드 API 주소 (기본값 `http://localhost:8080/api/v1`)

`.env`는 커밋하지 않습니다. `VITE_`로 시작하는 값은 브라우저에 그대로 노출되므로 비밀 값을 넣지 않습니다.

## 로컬 주소

백엔드 CORS가 `http://localhost:5173`만 허용해서 개발 서버 포트를 5173으로 고정했습니다. 이미 사용 중이면 실행이 실패하니 해당 프로그램을 종료하세요. 브라우저에서도 `127.0.0.1`이 아니라 `localhost`로 접속합니다.
