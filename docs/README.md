# 문서 안내 — `docs/`

> 담당: 팀 공통 · 최종 수정: 2026-11-25

---

## 1. 디렉터리 구분

| 디렉터리 | 담는 것 |
| --- | --- |
| `planning/` | 기획 — MVP 범위와 완료 기준. PRD 원문은 Notion에 둔다 |
| `dev/specs/` | **파트 사이의 계약** — 아키텍처, LLM ↔ 백엔드 인터페이스, API, ERD. 바꾸면 다른 파트가 깨지는 문서 |
| `dev/llm/` | LLM 파트 설계 — 스택, 채점 엔진 구조, 레벨·지표·루브릭 |
| `dev/ai/` | AI 협업 도구 — 사용 가이드, 단계별 프롬프트, 리뷰 체크리스트, Jira 이슈 양식 |
| `ops/` | 운영 — 배포 절차, 환경변수 |
| `adr/` | 되돌리기 어려운 결정의 기록 (Architecture Decision Record) |
| `brand/` | 브랜딩·카피 가이드 |
| `assets/` | 문서에 쓰는 이미지 등 |

---

## 2. 문서 목록

상태: **작성됨** 본문이 있고 현재 기준으로 쓰는 문서 · **초안** 본문은 있으나 관련 파트 확정 전 · **뼈대** 제목만 있음 · **템플릿** 복사해서 쓰는 양식

| 문서 | 다루는 것 | 담당 | 상태 |
| --- | --- | --- | --- |
| `planning/mvp.md` | MVP 목표·우선순위·기능별 완료 기준·데모 시나리오·파트별 산출물 | PM | 작성됨 |
| `dev/specs/architecture.md` | 전체 서비스·인프라 구성, 백엔드 아키텍처 | 클라우드 · 백엔드 | 작성됨 |
| `dev/specs/interfaces.md` | Spring Boot ↔ FastAPI 계약 — 엔드포인트·스키마·에러·타임아웃·멱등성·표기 변환 | 백엔드 · LLM 공동 | 초안 |
| `dev/specs/api.md` | 클라이언트 ↔ Spring Boot API (`/api/v1`) 공통 규칙 — 인증·에러 형식·에러 코드. 엔드포인트별 명세는 Swagger | 백엔드 | 작성됨 |
| `dev/specs/erd.md` | DB 테이블·관계·설계 의도·모드별 적재 규칙 | 백엔드 | 작성됨 |
| `dev/llm/llm-tech-spec.md` | AI 서버 스택과 선택 근거 | LLM | 작성됨 |
| `dev/llm/system-design.md` | 서비스 정의·6요소·3모드·엔진 구성·LLM 호출 수·로그 필드 | LLM | 작성됨 |
| `dev/llm/model-design.md` | 레벨·지표 계산, 루브릭, 문장 템플릿, 모호표현, 조립 제약, 신뢰도 검증 | LLM | 작성됨 |
| `dev/ai/README.md` | AI 협업 가이드 — 파일 구조, 단계별 프롬프트 사용법, MCP·CLI 연결, 웹 채팅 | AI 협업 도구 | 작성됨 |
| `dev/ai/prompts/` | 단계별 프롬프트 7종 (`00`~`60`) | AI 협업 도구 | 작성됨 |
| `dev/ai/review-checklist.md` | 리뷰 기준 — 공통 + 파트별 | AI 협업 도구 (파트별 섹션: 각 파트) | 작성됨 |
| `dev/ai/jira-guide.md` | Jira 이슈 제목·필드 규칙, 유형별 양식, 상태 흐름 | AI 협업 도구 | 작성됨 |
| `ops/deploy.md` | CI/CD 파이프라인과 롤백 절차 | 클라우드 | 작성됨 |
| `ops/env.md` | 서비스별 `.env` 키, GitHub Secrets | 클라우드 (2절 AI 서버: LLM) | 작성됨 |
| `adr/0000-template.md` | ADR 작성 양식 | 팀 | 템플릿 |
| `brand/branding.md` | 브랜딩 가이드 | 디자인 | 뼈대 |

---

## 3. 주제별 문서 위치

| 궁금한 것 | 문서 |
| --- | --- |
| MVP에 무엇이 들어가나, 기능별 완료 기준 | `planning/mvp.md` 2–3장 |
| AI 서버 엔드포인트, 요청·응답 JSON, 에러 코드 | `dev/specs/interfaces.md` 3장 |
| Spring이 AI 서버를 부를 때 타임아웃·재시도·헤더 | `dev/specs/interfaces.md` 4–5장 |
| `snake_case` ↔ `camelCase`, 열거형 대소문자 변환 | `dev/specs/interfaces.md` 7장 |
| 레벨이 어떻게 정해지나, 세분화율은 무엇인가 | `dev/llm/model-design.md` 1–2장 |
| 6요소가 무엇이고 어떻게 판정하나 | `dev/llm/system-design.md` 1.3 · `dev/llm/model-design.md` 3–4장 |
| DB 테이블·관계, 모드별 적재 규칙 | `dev/specs/erd.md` |
| DB 컬럼 | `backend/src/main/resources/db/migration/` (Flyway) |
| 서버·네트워크 구성 | `dev/specs/architecture.md` |
| 환경변수 키 이름 | `ops/env.md` |
| 배포·롤백 | `ops/deploy.md` |
| AI 도구로 작업하는 단계와 프롬프트, Jira·GitHub MCP 연결 | `dev/ai/README.md` |
| Jira 이슈 양식, 상태별 옮기는 시점 | `dev/ai/jira-guide.md` |
| 리뷰할 때 무엇을 확인하나 | `dev/ai/review-checklist.md` |

---

## 4. 작성 규칙

1. **머리말.** 제목 아래에 담당과 최종 수정일을 적는다. 다른 문서에서 옮겨 왔으면 출처도 적는다.

    ```markdown
    # 문서 제목

    > 담당: 파트(이름) · 최종 수정: YYYY-MM-DD
    > 출처: 원본 문서 · 절
    ```

2. **같은 내용은 한 곳에만.** 문서끼리 값이 어긋나는 것을 막기 위해 겹치는 내용은 한 문서에 쓰고 다른 문서는 링크한다. 
3. **코드가 기준인 값은 코드를 가리킨다.** DB 컬럼의 기준은 Flyway 마이그레이션, 환경변수 키의 기준은 각 서비스 폴더의 `.env.example`이다. 문서에는 설계 의도와 설명을 쓴다.
4. **비밀값의 실제 값은 쓰지 않는다.** 키 이름과 설명만 쓴다 (`ops/env.md` 0절).
5. **계약 문서(`dev/specs/`)는 문서를 먼저 고친다.** 구현을 바꾸기 전에 문서를 고치고 관련 파트의 확인을 받는다.
6. **다른 파트 문서는 상의 후 수정한다.** 고칠 때는 담당자를 리뷰어로 넣고, PR "리뷰어에게"에 이유를 적는다 (`CONTRIBUTING.md` 7절).
7. **커밋 scope.** 파트 소유 문서는 그 파트 scope(`llm`, `be`, `infra` 등), 팀 공통 문서는 `docs`를 쓴다 (`CONTRIBUTING.md` 1.3).
8. **되돌리기 어려운 결정은 ADR로 남긴다.** `adr/0000-template.md`를 복사해 번호를 붙인다.
9. **문서를 추가하거나 뼈대를 채우면 이 README의 2장도 같이 고친다.**
