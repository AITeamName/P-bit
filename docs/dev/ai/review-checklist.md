# 리뷰 체크리스트

> 담당: AI 협업 도구 (파트별 섹션은 각 파트 확인) · 최종 수정: 2026-11-25

자체 리뷰(`prompts/40-review.md` A), 리뷰 보조(B), 사람이 하는 리뷰가 같이 쓰는 기준이다. 커밋 전 확인(`prompts/30-implement.md`)에도 쓴다.

- 항목마다 무엇을 어디와 비교할지만 적는다. 판정 기준은 근거 문서에 있다.
- PR 템플릿의 셀프 체크리스트(`.github/pull_request_template.md`)는 여기에 다시 적지 않는다. PR 본문에서 따로 체크한다.
- `AGENTS.md` 파일은 영어로 작성돼 있다. 아래 `AGENTS.md` 참조는 영어 절 제목 그대로 검색하면 된다.
    - `backend/AGENTS.md`는 백엔드가 한국어로 작성했다. 백엔드 섹션은 한국어 절 제목을 쓴다.

## 판정

| 판정 | 쓰는 법 |
| --- | --- |
| 통과 | 근거를 적는다 — 파일과 위치, 또는 실행한 명령과 결과 |
| 미통과 | 위치와 이유를 적는다 |
| 해당 없음 | 이유를 적는다 |
| 미확인 | 확인하지 못했다. 통과로 바꾸지 않는다 |

- 미통과가 0건이어야 PR을 만든다.

## 적용 범위

1. `git diff --stat <base>...HEAD`로 바뀐 경로를 본다 (일반 PR은 `develop`, Stack PR은 선행 브랜치).
2. 경로로 파트를 판별한다 (`CONTRIBUTING.md` > `1.3 Scope 정의`).
3. 공통 섹션(A~E)은 항상 적용한다. 파트 섹션은 그 파트 파일이 바뀌었을 때 적용한다.

---

## 공통

### A. 범위

| # | 확인 | 방법 | 근거 |
| --- | --- | --- | --- |
| A1 | 이슈 범위 밖 파일 변경이 없다. 있으면 PR `💬 리뷰어에게`에 사유가 있다 | `git diff --stat` ↔ 이슈 작업 내용 | `AGENTS.md` > `3. Scope and prohibitions` |
| A2 | 다른 파트 소유 파일을 바꿨다면 그 파트 확인 요청이 있다 | 바뀐 경로 ↔ scope 표, `.github/CODEOWNERS` | `CONTRIBUTING.md` > `1.3 Scope 정의` |
| A3 | 커밋 메시지가 규칙을 따르고, 한 커밋에 한 scope다 | `git log --oneline <base>..HEAD` | `CONTRIBUTING.md` > `2. 커밋 규칙` |
| A4 | 브랜치 이름과 PR 제목에 같은 `KAN-번호`가 있다 | 브랜치 이름, PR 제목 | `CONTRIBUTING.md` > `1.2 브랜치 네이밍`, `4.2 PR 제목` |
| A5 | Stack PR(base가 `develop`이 아님)이면 `🧱 stack` 라벨이 있다 | PR 라벨 | `CONTRIBUTING.md` > `4.6 PR 본문 작성` |

### B. 계약·문서

| # | 확인 | 방법 | 근거 |
| --- | --- | --- | --- |
| B1 | 계약(`docs/dev/specs/`)을 바꿨다면 문서를 먼저 고쳤고 관련 파트 확인이 있다 | 문서 diff, PR 리뷰어 | `docs/README.md` > `4. 작성 규칙` |
| B2 | 코드와 계약 문서가 다르지 않다 (엔드포인트, 필드, 에러 코드) | 코드 ↔ `interfaces.md`·`api.md` | `docs/dev/specs/` |
| B3 | 문서를 추가·삭제했다면 `docs/README.md` 문서 목록도 고쳤다 | `find docs -name "*.md"` ↔ 목록 | `docs/README.md` > `4. 작성 규칙` |
| B4 | 같은 내용을 두 문서에 옮겨 적지 않았다. 다른 문서는 위치만 가리킨다 | 문서 diff | `docs/README.md` > `4. 작성 규칙` |
| B5 | 코드가 기준인 값(DB 컬럼, 환경변수 키)을 문서에 복사하지 않았다 | 문서 diff | `docs/README.md` > `4. 작성 규칙` |

### C. 보안·비밀값

| # | 확인 | 방법 | 근거 |
| --- | --- | --- | --- |
| C1 | 코드·설정·테스트·문서에 비밀값이 없다 | `git diff <base>...HEAD \| grep -nE "password=\|api_key=\|_TOKEN=\|ghp_\|github_pat_\|ATATT\|BEGIN.*PRIVATE"` | `AGENTS.md` > `0. Absolute rules` |
| C2 | `.env` 파일이 커밋되지 않았다. 새 키는 `.env.example`에 이름만 있다 | `git diff --stat` | `docs/ops/env.md` |
| C3 | 사용자 프롬프트 원문(`prompt_text`)이 로그·에러 응답에 남지 않는다 | 로그·예외 처리 코드 | `docs/dev/specs/interfaces.md` > `1.2` |

### D. 테스트·빌드

| # | 확인 | 방법 | 근거 |
| --- | --- | --- | --- |
| D1 | 바뀐 파트의 커밋 전 루틴이 통과한다 | 각 파트 `AGENTS.md` > `1. Stack and commands`의 명령 | 파트 `AGENTS.md` |
| D2 | 정상 경로와 예외 경로 테스트가 있다 | 테스트 목록 | 파트 `AGENTS.md` > `4. Tests` |
| D3 | PR 테스트 항목에 실제로 실행한 명령만 체크했다 | PR 본문 ↔ 실행한 명령 | `CONTRIBUTING.md` > `4.6 PR 본문 작성` |

### E. 문서·AI 산출물

문서, 설정, 이슈, PR, 코멘트, AI가 쓴 코드에 적용한다.

| # | 확인 | 방법 |
| --- | --- | --- |
| E1 | 구현됐다고 쓴 기능이 코드·설정에 실제로 있다 | 해당 파일을 직접 연다 |
| E2 | 존재하지 않는 컴포넌트·도구·서비스를 있는 것처럼 쓰지 않았다 | 설명한 대상마다 파일·설정 위치 확인 |
| E3 | 도구 동작(CI, 린트)은 설정 파일로 확인했다 | `.github/workflows/`, `package.json`, `build.gradle` |
| E4 | "필요하면", "적절히" 같은 모호한 표현이 없다 | 문장 확인 |
| E5 | 확인을 건너뛰어도 된다는 뜻의 문장이 없다 | 문장 확인 |
| E6 | 문서 위치를 줄 번호가 아니라 `문서 > 섹션`으로 썼다 | 인용 형식 |
| E7 | 모르는 값(Jira 번호, 기한, 담당자)을 추측하지 않고 비워 뒀다 | 빈 칸 확인 |
| E8 | AI가 쓴 날짜·숫자·이름·버전을 실제 값과 대조했다 | 달력, Jira, `package.json`, `build.gradle` |
| E9 | 개인 이름이나 개인 작업 파일 경로가 없다 | 문장 확인 |
| E10 | 스킬·외부 지침 때문에 저장소 규칙과 다른 구조가 생기지 않았다 | 파트 `AGENTS.md` ↔ 결과물 |

---

## 파트별

### BE. 백엔드 (`backend/`)

| # | 확인 | 근거 |
| --- | --- | --- |
| BE1 | 패키지 위치와 계층 호출 방향이 규칙과 맞다 | `backend/AGENTS.md` > `패키지 구조`, `계층 규칙` |
| BE2 | 엔티티를 응답으로 직접 반환하지 않고 응답 DTO(`record`)로 바꾼다 | `backend/AGENTS.md` > `DTO` |
| BE3 | 예외는 `BusinessException(ErrorCode)`만 쓰고, 새 에러 코드는 명세에 먼저 추가했다 | `backend/AGENTS.md` > `에러 처리` |
| BE4 | 인증 규칙(토큰 내용, Refresh 회전, 소유자 검증)을 지킨다 | `backend/AGENTS.md` > `JWT·인증` |
| BE5 | 스키마 변경은 새 Flyway 버전 파일이다. 적용된 `V*.sql`을 고치지 않았다 | `backend/AGENTS.md` > `DB` > `스키마 변경 (Flyway)` |
| BE6 | 시간 타입은 `Instant`, 판정 로그에 `rubric_version`·`domain`·`language`가 있다 | `backend/AGENTS.md` > `DB` > `타입` |
| BE7 | AI 서버 호출과 snake_case ↔ camelCase 변환은 `infra/ai` 한 곳에서 하고, DB 트랜잭션 밖에서 호출한다 | `docs/dev/specs/interfaces.md` > `7`, `backend/AGENTS.md` > `계층 규칙`, `DTO` |

### FE. 프론트엔드 (`frontend/`)

| # | 확인 | 근거 |
| --- | --- | --- |
| FE1 | AI 서버를 직접 호출하지 않고 Spring API만 쓴다 | `docs/dev/specs/interfaces.md` > `1.2`, `frontend/AGENTS.md` > `3. Rules — API and auth` |
| FE2 | AI를 거치는 요청에 `Idempotency-Key`를 보낸다 | `interfaces.md` > `5.1`, `frontend/AGENTS.md` > `3. Rules — API and auth` |
| FE3 | 액세스 토큰은 메모리에만 두고, Refresh는 쿠키로 다룬다 | `frontend/AGENTS.md` > `3. Rules — API and auth` |
| FE4 | 화면에 보이는 문구는 i18n 키로 쓰고 언어별 키가 일치한다 | `frontend/AGENTS.md`, `npm run lint` 결과 |
| FE5 | `VITE_` 환경변수에 비밀값이 없다 | `.env.example`, `frontend/AGENTS.md` > `5. Prohibitions` |
| FE6 | 미확정 항목을 임의로 구현하지 않았다 | `frontend/AGENTS.md` |

### LLM. AI 서버 (`ai-server/`)

| # | 확인 | 근거 |
| --- | --- | --- |
| LLM1 | DB에 접근하지 않고, `channel`로 판정을 나누지 않는다 | `ai-server/AGENTS.md` > `3. Rules` > `Boundaries` |
| LLM2 | 필드는 snake_case, 열거형은 소문자, span 위치는 코드포인트 기준이다 | `ai-server/AGENTS.md` > `3. Rules` > `Contract` |
| LLM3 | 루브릭·임계값·문장 템플릿이 `prompts/rubric/{버전}/`에 있고 코드에 하드코딩되지 않았다. 배포된 버전 디렉터리를 고치지 않았다 | `ai-server/AGENTS.md` > `3. Rules` > `Rubric` |
| LLM4 | 판정·조립은 temperature 0, 판정은 JSON 스키마 강제, evidence 검증, `degraded`면 레벨·충족률 `null` | `ai-server/AGENTS.md` > `3. Rules` > `Judgment` |
| LLM5 | 사용자에게 보이는 문장은 템플릿 + 변수 주입이다 | `ai-server/AGENTS.md` > `3. Rules` > `Sentences` |
| LLM6 | 조립 제약과 재생성 횟수를 지킨다 | `ai-server/AGENTS.md` > `3. Rules` > `Assembly` |
| LLM7 | 모델·타임아웃·재시도는 환경변수로 받는다 | `ai-server/AGENTS.md` > `3. Rules` > `Config` |
| LLM8 | 판정 로직·루브릭·모델을 바꿨다면 PR에 Before/After 측정값이 있다 (`perf`) | `CONTRIBUTING.md` > `2. 커밋 규칙`, PR 템플릿 측정 결과 |

### INFRA. 인프라 (`docker-compose.yml`, `.github/workflows/`, `docs/ops/`)

| # | 확인 | 근거 |
| --- | --- | --- |
| INFRA1 | 비밀값은 GitHub Secrets로만 참조한다 | `docs/ops/env.md` > `7` |
| INFRA2 | CI가 바뀐 파트만 도는 구조(`changes` → 파트 job → `ci-result`)를 유지한다 | `.github/workflows/ci.yml`, `docs/ops/deploy.md` > `1` |
| INFRA3 | 새 환경변수는 `.env.example`과 `docs/ops/env.md`에 함께 추가했다 | `docs/ops/env.md` > `8` |
| INFRA4 | 배포·롤백 절차가 바뀌었다면 `docs/ops/deploy.md`를 고쳤다 | `docs/ops/deploy.md` |

---

end.
