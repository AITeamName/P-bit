# LLM 파트 스택·선택 근거

> 담당: LLM(조하린) · 최종 수정: 2026-11-19
> 출처: AI 파트 기술 명세서 v1.0 1.1 · `docs/dev/specs/architecture.md` 2절 · `docs/ops/env.md` 2절

AI 서버(`ai-server/`)를 무엇으로 만들고 왜 그렇게 골랐는지만 적는다.

| 알고 싶은 것 | 문서 |
| --- | --- |
| Spring ↔ FastAPI 계약 (엔드포인트·스키마·에러·타임아웃) | `docs/dev/specs/interfaces.md` |
| 엔진 구성·호출 흐름 | `docs/dev/llm/system-design.md` |
| 모델 배정·루브릭·채점 로직 | `docs/dev/llm/model-design.md` |
| 인프라 배치 (EC2·보안그룹·테스트 환경) | `docs/dev/specs/architecture.md` |
| 환경변수 | `docs/ops/env.md` 2절 |

---

## 1. 스택

| 레이어 | 선택 | 선택 근거 · 비고 |
| --- | --- | --- |
| 언어 / 런타임 | Python | 버전 미정 |
| 패키지 관리 | uv | 로컬 실행 `uv run uvicorn src.main:app --reload --port 8001` (README) |
| 품질 도구 | ruff(린트·포맷) · mypy(`src`) · pytest | PR 템플릿 검증 명령 기준. CI job은 코드가 생기면 추가 (deploy.md 1절) |
| 웹 프레임워크 | **FastAPI** | `pydantic v2` 기반 스키마 검증. 요청/응답 모델이 곧 인터페이스 명세 3장의 계약이다 |
| ASGI 서버 | Uvicorn (`--workers 2`) | I/O 바운드(LLM 대기)라 워커 수보다 async 동시성이 중요하다. ⚠️ 워커 수는 2절 참고 |
| 리버스 프록시 | Nginx | 타임아웃 상한(`proxy_read_timeout 30s`), 요청 크기 제한(`client_max_body_size 1m`) |
| 컨테이너 | Docker / docker-compose | 이미지 1개(`pbit-ai`) |
| 포트 | FastAPI **8001** | Nginx 포트는 AWS 착수 시 결정 (architecture 2.4) |
| 인프라 | AWS EC2 #2 (t3.small 기준), Spring Boot와 **분리된 인스턴스** | AWS는 ⏸ 보류. 무료 테스트 환경의 AI 서버 배치는 미정 (architecture 2.2) |
| 루브릭·템플릿 | YAML을 **이미지에 포함** (`prompts/rubric/{버전}/`) | 환경변수가 아니다. `RUBRIC_VERSION`이 버전 디렉터리 이름이다. 배포된 버전 디렉터리는 수정하지 않는다 (`model-design.md` 4.1) |
| 비밀값 | 환경변수 (`.env` 미커밋) | `LLM_API_KEY`, `AI_INTERNAL_TOKEN`. 내부 토큰 생성·보관은 클라우드 담당 |
| LLM 공급자 | 미정 | AWS 착수 결정 대기 항목 (architecture 2.3) |

- 판정·조립과 답변 생성은 모델을 나눈다(중급 / 경량). 모델 등급·파라미터와 그 근거는 `model-design.md` 3.1.
- FastAPI는 DB에 붙지 않는 **stateless 서버**다. 경계 원칙은 인터페이스 명세 1.2.

## 2. 확인 필요

| # | 내용 |
| --- | --- |
| 1 | **Uvicorn 워커 2개 vs 메모리 상태.** `Idempotency-Key` 60초 캐시를 프로세스 메모리에 두면 워커마다 따로 동작해 중복 요청을 못 막는다. 워커 1개로 줄이거나 공유 저장소를 쓸지 결정 필요 (architecture 2.6의 Spring 단일 인스턴스 원칙과 같은 문제) |
| 2 | **S3 백업.** 기술 명세서 v1.0은 "루브릭 YAML 이미지 포함 + S3 백업, 부팅 시 S3 로드"였으나 architecture 2.3 AWS 구성에는 S3(루브릭)가 없다. 이미지 포함만으로 갈지 확인 필요 |
| 3 | **Python 버전** 확정 |
