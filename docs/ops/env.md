# 환경변수 목록 — 서비스별 `.env` 키와 GitHub Secrets
 
> 담당: 클라우드(김성민) · 최종 수정: 2026-11-15
> 출처: 백엔드 기술기획서 v1.1 3.2, LLM 모델 설계서 v1.0, 저장소의 `.env.example` 파일들
 
## 0. 비밀값 규칙 (꼭 지킬 것)
 
1. **실제 값은 이 문서·저장소·Notion에 쓰지 않는다.** 저장소는 Public이고, AI 도구가 Notion을 읽을 수 있다. 이 문서에는 **키 이름과 설명만** 둔다.
2. 실제 값은 **Discord 🔒 팀계정-관리 채널**로만 공유한다.
3. 팀이 같이 쓰는 키는 **개발 전용 + 사용 한도 설정**된 것만 쓴다.
4. 운영(AWS) 값은 **SSM Parameter Store**에만 둔다.
5. CI에는 **더미 값**만 쓴다 (`ci.yml`에 직접 적혀 있음).
6. 키가 유출되면 **즉시 폐기하고 재발급**한 뒤 팀 채널에 알린다.
7. `.env`는 `.gitignore` 대상이다. 커밋 전에 `git status`로 확인한다.
 
**표기:** 🔑 비밀값 · 📝 일반 설정값 · 필수 = 값이 없으면 기동/빌드 실패
 
## 1. 백엔드 — `backend/` (Spring Boot)
 
| 키 | 구분 | 필수 | 설명 | local 예시 |
| --- | --- | --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | 📝 | ✅ | `local` · `dev` · `prod` | `local` |
| `DB_URL` | 📝 | ✅ | JDBC 주소 | `jdbc:postgresql://localhost:5432/pbit` |
| `DB_USERNAME` | 📝 | ✅ | DB 사용자 | `pbit` |
| `DB_PASSWORD` | 🔑 | ✅ | DB 비밀번호 | (로컬 전용 값) |
| `JWT_SECRET` | 🔑 | ✅ (KAN-34 이후) | JWT 서명 키, 32자 이상 | — |
| `JWT_ACCESS_TTL` | 📝 | | Access 토큰 유효기간 | `PT30M` |
| `JWT_REFRESH_TTL` | 📝 | | Refresh 토큰 유효기간 | `P14D` |
| `GOOGLE_CLIENT_ID` | 📝 | ✅ (Google 로그인) | Google OAuth 클라이언트 ID | — |
| `GOOGLE_CLIENT_SECRET` | 🔑 | ✅ (Google 로그인) | Google OAuth 시크릿 | — |
| `AI_BASE_URL` | 📝 | ✅ (AI 연동) | FastAPI 주소 | `http://localhost:8001` |
| `AI_INTERNAL_TOKEN` | 🔑 | ✅ (AI 연동) | Spring→FastAPI 내부 인증 토큰. **ai-server와 같은 값.** 생성·보관: 클라우드 (김성민) | — |
| `AI_TIMEOUT` | 📝 | | FastAPI 호출 타임아웃 | `PT35S` |
| `WEB_ORIGIN` | 📝 | ✅ | CORS 허용 웹 주소 | `http://localhost:5173` |
| `EXTENSION_ORIGIN` | 📝 | ✅ (익스텐션 연동) | CORS 허용 익스텐션 주소 | `chrome-extension://{고정 ID}` |
 
## 2. AI 서버 — `ai-server/` (FastAPI)
 
| 키 | 구분 | 필수 | 설명 | 예시 / 기본값 |
| --- | --- | --- | --- | --- |
| `LLM_API_KEY` | 🔑 | ✅ | 외부 LLM API 키. 개발용은 사용 한도를 건 키만 공유 | — |
| `LLM_MODEL_JUDGE` | 📝 | ✅ | 판정·조립·재판정용 모델 식별자 (중급 모델) | — |
| `LLM_MODEL_ANSWER` | 📝 | ✅ | Before/After 답변 생성·이유 요약용 모델 식별자 (경량 모델) | — |
| `RUBRIC_VERSION` | 📝 | ✅ | 루브릭 버전. 루브릭(요소·판정 기준)이나 판정 모델을 바꾸면 올린다. 이미지에 포함된 루브릭 YAML과 일치해야 한다 | `v1.0` |
| `AI_INTERNAL_TOKEN` | 🔑 | ✅ | Spring ↔ FastAPI 공유 시크릿. 백엔드와 **같은 이름·같은 값**, 요청 헤더 `X-Internal-Token` 검증. 생성·보관: 클라우드 (김성민) | — |
| `LLM_TIMEOUT_JUDGE_SEC` | 📝 | | FastAPI → LLM 타임아웃(초): 판정·조립·재판정 (중급 모델) | `12` |
| `LLM_TIMEOUT_ANSWER_SEC` | 📝 | | FastAPI → LLM 타임아웃(초): Before/After 답변 생성·이유 요약 (경량 모델) | `20` |
| `LLM_MAX_RETRY` | 📝 | | FastAPI 내부 LLM 재시도 횟수 | `2` |
| `LOG_LEVEL` | 📝 | | 로그 레벨 | `INFO` |
 
> 서버 포트는 **8001**이고, 루브릭 YAML은 이미지에 포함되므로 환경변수가 아니다.
> 
> ⚠️ 타임아웃 값을 조정할 때는 **답변 타임아웃(`LLM_TIMEOUT_ANSWER_SEC`) < Nginx(30s) < Spring(35s)** 순서를 지킨다. 어기면 중간 구간에서 먼저 끊겨 504가 난다 (인터페이스 명세 4.1).
>
> 이 순서는 LLM 호출 1회 기준이다. `LLM_MAX_RETRY` 재시도까지 포함한 요청 전체 시간 예산은 인터페이스 명세 9장(LLM 추가 제안 A)에서 논의 중이다.
 
## 3. 웹 — `frontend/web/` (Vite)
 
| 키 | 구분 | 필수 | 설명 | local 예시 |
| --- | --- | --- | --- | --- |
| `VITE_API_BASE_URL` | 📝 | ✅ (기본값 없음, KAN-48) | API 기본 주소 | `http://localhost:8080/api/v1` |
 
> ⚠️ `VITE_`로 시작하는 값은 **빌드 결과물에 그대로 들어가 누구나 볼 수 있다.** 비밀값을 절대 `VITE_` 변수로 만들지 않는다.
 
## 4. 익스텐션 — `frontend/extension/`
 
코드가 생기면 추가한다. 익스텐션 ID 고정용 manifest `key`는 비밀값이 아니지만, 대응하는 **비공개 키(.pem)는 저장소에 올리지 않는다.**
 
## 5. 로컬 docker-compose — 루트 `.env`
 
루트 `.env.example`을 복사해 `.env`를 만든다. `docker-compose.yml`의 PostgreSQL 컨테이너 설정에 쓰인다 (DB 이름·사용자·비밀번호). 키 이름은 루트 `.env.example`을 기준으로 한다.
 
## 6. 환경별로 값을 넣는 위치
 
| 환경 | 백엔드 | 웹 |
| --- | --- | --- |
| local | `backend/.env` 또는 IDE 실행 설정 | `frontend/web/.env.local` |
| CI | `ci.yml`의 `env:` (더미 값) | `ci.yml`의 `env:` (더미 값) |
| 테스트 환경 (KAN-61) | Render → Environment | Vercel → Settings → Environment Variables |
| AWS (보류) | SSM Parameter Store `/pbit/{env}/...` (SecureString) | 빌드 시 주입 |
 
> 테스트 환경 값은 **로컬·운영과 다른 값**을 쓴다 (특히 `JWT_SECRET`, `DB_PASSWORD`).
 
## 7. GitHub Secrets / Variables
 
| 이름 | 종류 | 쓰는 곳 | 상태 |
| --- | --- | --- | --- |
| Discord 알림 웹훅 | Secret | `.github/workflows/discord-notification.yml` | 사용 중 (정확한 이름은 해당 워크플로 파일 기준) |
| `AWS_DEPLOY_ROLE_ARN` | Variable | AWS 배포 (OIDC) | 예정 — 비밀값은 아님 |
 
- CI(`ci.yml`)는 Secrets를 쓰지 않는다. 더미 값만 사용.
- AWS 배포는 OIDC를 쓰므로 **AWS 액세스 키를 Secrets에 저장하지 않는다.**
- Secrets 추가·변경은 저장소 관리자만 가능하다. 추가하면 이 표도 같이 갱신한다.
 
## 8. 새 환경변수를 추가할 때
 
1. 해당 폴더의 `.env.example`에 **키 이름 + 더미 값** 추가
2. 이 문서의 해당 표에 한 줄 추가
3. 필수 값이면 `ci.yml`에 더미 값 추가 (안 하면 CI가 실패한다)
4. 테스트 환경에 실제 값 등록 → Discord 🔒 채널에 공유
5. PR 설명에 "환경변수 추가: `KEY_NAME`"이라고 적는다
 
