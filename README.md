₩# 🎯 P-bit (Prompt Trainer)

AI 프롬프트 작성 능력을 향상시키는 질문력 트레이너 서비스의 **모노레포**입니다. 
백엔드 · AI Server · 프론트(웹 / 크롬 익스텐션)가 하나의 레포에서 함께 관리됩니다.

---

## 📁 폴더 구조

| 폴더 | 내용 | 담당 |
| --- | --- | --- |
| `backend/` | Spring Boot API 서버 | BE |
| `ai-server/` | FastAPI LLM 판정 서버 | LLM |
| `frontend/web/` | 웹사이트 (로그인 · 온보딩 · 대시보드) | FE |
| `frontend/extension/` | 크롬 익스텐션 사이드바 | FE |
| `docs/` | 설계 문서 · API 명세 · ERD | 전원 |
| `docs/dev/ai` | AI 협업 문서 | LLM |
| `docker-compose.yml` | 로컬 DB (PostgreSQL) | BE · INFRA |

> ⚠️ **자기 담당 폴더만 수정합니다.** 다른 파트 폴더 수정이 필요하면 담당자와 먼저 상의하세요.
> 

---

## 🌿 브랜치 전략

| 브랜치 | 설명 |
| --- | --- |
| `main` | 🚀 배포 브랜치 (직접 수정 금지) |
| `develop` | 🔧 개발 통합 브랜치 (직접 수정 금지) |
| `feat/*` | ✨ 기능 개발 |
| `fix/*` | 🐛 버그 수정 |
| `refactor/*` | ♻️ 리팩토링 |
| `docs/*` | 📝 문서 |
| `chore/*` | 🔨 설정 / 빌드 |
| `hotfix/*` | 🚑 운영 긴급 수정 |

> 파트별 장기 브랜치(`backend`, `frontend` 등)는 두지 않습니다. 파트 구분은 **폴더**와 **커밋 scope**로 합니다.
> 

### 브랜치 네이밍

```
<type>/KAN-<number>-<subject>
```

예시

```
feat/KAN-12-signup-api
fix/KAN-31-jwt-expiration
feat/KAN-18-llm-format-judgement
chore/KAN-29-monorepo-setup
```

> 💡 브랜치 이름에 `KAN-번호`가 있으면 **Jira 이슈에 자동 연결**됩니다.
> 

---

## 🔄 작업 순서

```bash
# 1. develop 최신화 후 작업 브랜치 생성
git switch develop && git pull origin develop
git switch -c feat/KAN-12-signup-api

# 2. 담당 폴더만 스테이징 — git add . 지양
git add ai-server/

# 3. 커밋 (컨벤션 준수)
git commit -m "feat(llm): 형식 요소 판정 로직 구현 (KAN-18)"

# 4. push → Jira에서 Draft PR 생성
git push origin feat/KAN-18-llm-format-judgement
```

| 파트 | add 명령 |
| --- | --- |
| BE | `git add backend/` |
| AI | `git add ai-server/` |
| FE 웹 | `git add frontend/web/` |
| FE 익스텐션 | `git add frontend/extension/` |
| 공통 문서 | `git add docs/` |

> 자세한 협업 규칙은 CONTRIBUTING.md를 참고하세요.
> 

---

## 📮 Pull Request

```
feat/*  → develop  ✅
develop → main     ✅ (배포 시)
hotfix/* → main   ✅ (긴급 수정)
```

**PR 제목**

```
[KAN-번호] type(scope): 작업 내용 요약
```

예시

```
[KAN-18] feat(llm): 형식 요소 판정 로직 구현
[KAN-20] feat(ext): 사이드바 진단 화면 구현
[KAN-31] chore(be): 로컬 개발 환경 구축
```

---

## ⚙️ CI (자동 빌드·테스트)

`develop`·`main`으로 PR을 올리거나 push하면 GitHub Actions가 **바뀐 파트만** 자동으로 빌드·테스트합니다.
워크플로 파일: `.github/workflows/ci.yml` (담당: INFRA)

| 파트 | 실행 조건 | 검사 내용 | 상태 |
| --- | --- | --- | --- |
| BE | `backend/**` 변경 | `./gradlew build` (컴파일 + 테스트) | ✅ 적용 |
| AI | `ai-server/**` 변경 | 린트 + `pytest` | ⏳ 코드 생성 후 추가 |
| FE 웹 | `frontend/web/**` 변경 | `npm ci` → lint → build | ⏳ 코드 생성 후 추가 |
| FE 익스텐션 | `frontend/extension/**` 변경 | `npm ci` → lint → build | ⏳ 코드 생성 후 추가 |

### 머지 규칙
- PR 화면 하단의 **`ci-result` 체크가 초록색(✅)이어야 머지**합니다. 빨간색(❌)이면 Actions 탭에서 로그를 확인하고 고친 뒤 다시 push하세요.
- 파트별 담당자가 1명이라 사람 승인은 필수로 두지 않습니다. 리뷰는 팀 채널 요청 + AI 리뷰 프롬프트로 보완합니다.
- 내 파트가 안 바뀐 PR에서는 해당 job이 건너뛰어지며(Skipped), 이는 정상입니다.

### 참고
- CI는 실제 비밀값을 쓰지 않습니다. 테스트용 더미 값만 워크플로에 들어 있습니다.
- 배포(CD)는 서버 연결 시 별도 워크플로로 추가합니다 (클라우드 기술기획서 3.2).
  
---

## ✍️ 커밋 컨벤션

```
<type>(<scope>): <subject> (KAN-번호)
```

| Type | 설명 |
| --- | --- |
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 코드 리팩토링 (기능 변화 X) |
| `perf` | 성능·품질 개선 (측정값 필수) |
| `docs` | 문서 수정 |
| `style` | 코드 포맷팅 (기능 변화 X) |
| `test` | 테스트 코드 |
| `chore` | 빌드, 설정 파일 수정 |
| `hotfix` | 긴급 수정 |

scope는 변경한 파일의 **담당 파트**입니다. 파트 소유 문서도 그 파트 scope를 씁니다 (예: `docs(be)`).

| Scope | 대상 |
| --- | --- |
| `be` | `backend/`, 백엔드 기술 명세 |
| `llm` | `ai-server/`, `docs/dev/llm/` |
| `web` | `frontend/web/` |
| `ext` | `frontend/extension/` |
| `fe` | `frontend/` 공통, 프론트엔드 기술 명세 |
| `infra` | `docker-compose.yml`, `.github/workflows/`, `docs/ops/`, 클라우드 기술 명세 |
| `de` | `docs/brand/` |
| `ai` | 루트 `CLAUDE.md`, `.claude/`, `.mcp.json`, `docs/dev/ai/` |
| `repo` | `.gitignore`, 루트 `.env.example`, `.github/CODEOWNERS`, PR 템플릿 |
| `docs` | `README.md`, `CONTRIBUTING.md`, `docs/planning/`, `docs/dev/specs/`, `docs/adr/` |

> 판단 기준과 예시는 [CONTRIBUTING.md 1.3 Scope 정의](CONTRIBUTING.md#13-scope-정의)를 참고하세요.

예시

```bash
git commit -m "feat(llm): 형식 요소 판정 로직 구현 (KAN-18)"
git commit -m "feat(ext): 사이드바 진단 화면 구현 (KAN-20)"
git commit -m "chore(infra): CI 워크플로우 추가 (KAN-29)"
```

---

## ⚠️ 주의사항

### 🚫 절대 금지

- ❌ `main`·`develop` 브랜치에 직접 push
- ❌ 다른 사람 브랜치에 함부로 push
- ❌ 다른 파트 폴더를 상의 없이 수정
- ❌ `.env`, API Key 등 민감 정보 커밋

### ✅ 필수 수칙

- 작업 시작 전 Jira 이슈 생성 → 브랜치 생성
- 작업 시작 전 `git pull origin develop` 무조건 실행
- 충돌 발생 시 해당 파트 담당자와 상의 후 해결
- PR 전 반드시 로컬 테스트

---

## 🔒 환경 변수 관리

보안을 위해 API Key 등 민감 정보는 절대 커밋하지 않습니다.

| 서비스 | 로컬 설정 파일 | 예시 파일 (커밋 대상) |
| --- | --- | --- |
| 루트 | `.env` | `.env.example` |
| ai-server | `ai-server/.env` | `ai-server/.env.example` |
| frontend/web | `frontend/web/.env` | `frontend/web/.env.example` |
| frontend/extension | `frontend/extension/.env` | `frontend/extension/.env.example` |
| backend | `backend/src/main/resources/application-local.yml` | `application-local.yml.example` |

> 실제 값은 팀 노션 페이지에서 확인 후 로컬에 복사하세요.
> 

---

## 🐳 로컬 실행

**사전 요구사항:** Docker Desktop, Java 17, Python 3.12+, Node.js 20+

### DB (공통)

```bash
# 최초 1회: .env 생성 후 값 입력 (값은 팀 노션 참고)
cp .env.example .env

# DB 실행 (최초 1회는 이미지 다운로드)
docker compose up -d
```

| 항목 | 값 |
| --- | --- |
| DB | `localhost:5432` / DB 이름·계정·비밀번호는 루트 `.env` |

### 백엔드 (Spring Boot)

```bash
cd backend
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
# → 값 입력 후 IntelliJ에서 ApiApplication 실행 (또는 ./gradlew bootRun)
```

| 항목 | 값 |
| --- | --- |
| API 문서 | http://localhost:8080/swagger-ui.html |
| 헬스체크 | http://localhost:8080/actuator/health |

기본 프로필은 `local`입니다. 앱을 실행하면 Flyway가 스키마를 자동으로 생성합니다.

```bash
# 테이블 확인 (서비스 테이블 10개 + flyway_schema_history)
docker exec -it pbit-postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "\dt"'

# DB 초기화 (로컬 데이터 전부 삭제)
docker compose down -v
```

> 스키마는 Flyway로만 변경합니다 (`backend/src/main/resources/db/migration`). 이미 적용된 `V*.sql`은 수정하지 말고 새 버전 파일을 추가하세요.
> 

### AI 서버 (FastAPI)

```bash
cd ai-server
cp .env.example .env     # 최초 1회 — 값은 팀 노션 참고
uv sync
uv run uvicorn src.main:app --reload --port 8001
```

| 항목 | 값 |
| --- | --- |
| API 문서 | http://localhost:8001/docs |
| 헬스체크 | http://localhost:8001/health |

### 프론트엔드

프론트 실행 방법은 해 폴더의 README를 참고하세요.
