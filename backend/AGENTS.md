# backend — AI 작업 규칙

Spring Boot API 서버. 익스텐션·웹이 같은 API와 같은 DB를 쓴다.
명세와 다르게 구현해야 하면 코드보다 명세를 먼저 고친다 (연동 기록 변경 로그, `docs/dev/specs/`).

| 문서 | 내용 |
| --- | --- |
| `docs/dev/specs/api.md` | API 공통 규칙 (인증·쿠키·에러 형식·에러 코드). 엔드포인트별 명세는 Swagger |
| `docs/dev/specs/erd.md` | 테이블·코드값·인덱스 |
| `docs/dev/specs/interfaces.md` | Spring ↔ FastAPI 호출 계약 |
| `docs/dev/specs/architecture.md` 3절 | 백엔드 구조와 선택 이유 |

## 스택

- Java 17, Spring Boot 4.0.x, Gradle
- PostgreSQL 16, Flyway, Spring Data JPA (Hibernate)
- Spring Security, springdoc-openapi 3.0.x, Lombok

### Spring Boot 4 주의 (Boot 3 예제를 그대로 쓰지 않는다)

- Jackson은 3.x: `tools.jackson.databind.ObjectMapper` (`com.fasterxml` 아님)
- 웹 테스트 어노테이션: `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`
- JPA 테스트 어노테이션: `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest`
- springdoc은 3.0.x (2.x는 Boot 3용)

## 패키지 구조

```
com.pbit.api
├─ global/            모든 기능이 같이 쓰는 것
│   ├─ config/         Security, CORS, Swagger, Clock, PasswordEncoder 설정
│   ├─ auth/           JWT 발급·검증, 인증 필터, AuthUser, Refresh 쿠키
│   ├─ error/          ErrorCode, BusinessException, ErrorResponse, GlobalExceptionHandler
│   └─ logging/        RequestIdFilter
├─ domain/            기능별 (현재 auth·user·term, 예정 session·event·record·dashboard)
│   └─ {기능}/
│       ├─ controller/
│       ├─ service/
│       ├─ repository/
│       ├─ entity/
│       ├─ validation/   커스텀 검증 어노테이션 (필요할 때만)
│       └─ dto/
│           ├─ request/
│           └─ response/
└─ infra/
    └─ ai/             FastAPI 호출 (AiClient, AI 연동 DTO) — AI 연동 시 추가
```

- 새 기능도 위 모양을 그대로 따른다. 빈 폴더는 만들지 않는다.
- 여러 기능이 쓰는 코드만 `global/`에 둔다. 한 기능만 쓰면 그 기능 폴더에 둔다.
- 다른 기능의 데이터가 필요하면 그 기능의 Service를 호출한다. 다른 기능의 Repository를 직접 쓰지 않는다.

## 계층 규칙

- 호출 방향: Controller → Service → Repository. 역방향 호출 금지.
- Controller: 요청 검증(`@Valid`)과 응답 반환만. 비즈니스 로직 금지.
- Controller 메서드는 항상 `ResponseEntity<응답 DTO>`를 반환한다. 200이면 `ResponseEntity.ok(응답)`, 생성은 `ResponseEntity.status(HttpStatus.CREATED)`, 본문 없음은 `ResponseEntity.noContent()`, 쿠키 등은 `.header(...)`로 붙인다.
- 로그인 사용자는 `@AuthenticationPrincipal AuthUser authUser`로 받고, 사용자 ID는 `authUser.userId()`에서만 꺼낸다. 요청 본문·파라미터의 userId를 믿지 않는다. `UUID`로 바로 받으면 null이 들어온다.
- Service: 비즈니스 로직과 트랜잭션(`@Transactional`). 조회는 `@Transactional(readOnly = true)`.
- 엔티티를 API 응답으로 직접 반환하지 않는다. 항상 응답 DTO로 바꾼다.
- FastAPI 호출은 DB 트랜잭션 밖에서 하고, 결과를 받은 뒤 짧은 트랜잭션으로 저장한다.

## DTO

- `record`로 만든다.
- 이름: `{동작}Request` / `{동작}Response` (예: `SignupRequest`, `LoginResponse`)
- 요청 DTO는 `dto/request/`, 응답 DTO는 `dto/response/`에 둔다.
- 서비스 결과에 응답 본문으로 내보내면 안 되는 값(예: Refresh 토큰 원문 → 쿠키로만 전달)이 있을 때만 `service/`에 `{동작}Result` record를 따로 둔다. 컨트롤러가 `Result`에서 쿠키와 `Response`를 나눠 만든다. 그 외에는 서비스가 응답 DTO를 바로 반환한다.
- 검증은 Bean Validation 어노테이션으로 하고 `message`에 화면 문구를 쓴다. 이 문구가 에러 응답 `fieldErrors[].reason`으로 나간다.
- 엔티티 → 응답 변환은 응답 DTO의 정적 메서드 `from(엔티티)`로 한다.
- JSON 필드는 camelCase. FastAPI용 snake_case 변환은 `infra/ai`의 DTO에서만 한다.

## 엔티티

- 생성은 정적 팩토리 메서드(예: `User.createLocal(...)`)로 한다. 빌더·public setter는 만들지 않는다.
- 값 변경은 의미 있는 이름의 메서드로 한다 (예: `revoke(now)`).
- 현재 시각은 주입받은 `Clock`에서 꺼낸다 (`Instant.now(clock)`). 테스트에서 시각을 고정하기 위해서다.

## 에러 처리

- 예외는 `throw new BusinessException(ErrorCode.XXX)`만 쓴다. 다른 예외를 새로 만들지 않는다.
- 필요한 코드가 없으면 `ErrorCode`에 추가하고 `docs/dev/specs/api.md` 에러 코드 표에도 추가한다.
- 에러 응답 형식은 `ErrorResponse`(code, userMessage, retryable, requestId, fieldErrors, fallback)로 고정이다. `message` 필드는 없다.
- 예상하지 못한 예외의 원인은 로그에만 남기고 응답에 넣지 않는다.

## Swagger

- 새 API는 `@Tag`, `@Operation`, `@ApiResponse`(성공 + 나올 수 있는 에러 코드)를 붙인다. 엔드포인트 명세는 Swagger가 기준이다.
- 인증이 필요 없는 API는 `@SecurityRequirements`(빈 값)로 자물쇠를 뗀다.
- 쿠키로만 받는 값은 `@Parameter(hidden = true)`로 숨기고 설명에 적는다.
- Swagger UI는 `local` 프로필에서만 열린다.

## JWT·인증

| 항목 | 규칙 |
| --- | --- |
| Access Token | JWT, HS256, 30분. `Authorization: Bearer` 헤더 |
| Access 내용 | `sub` = userId, `role`만. 이메일·닉네임 등 개인정보는 넣지 않는다 |
| Refresh Token | 불투명 랜덤 문자열, 14일. DB에는 SHA-256 해시만 저장 (원문 저장 금지) |
| 회전 | 재발급할 때마다 새 Refresh를 발급하고 이전 것은 `revoked_at`으로 폐기 |
| 재사용 | 폐기된 Refresh가 오면 거절 (`REFRESH_TOKEN_INVALID`) |
| 웹 전달 | Refresh는 쿠키 `refresh_token` (`HttpOnly`, `SameSite=Lax`, 경로 `/api/v1/auth`, 14일). 로컬은 `Secure` 해제 |
| 익스텐션 전달 | Refresh를 응답·요청 본문으로 주고받는다 (쿠키 사용 안 함). 익스텐션 로그인 구현 시 적용 |
| 기기 구분 | `refresh_tokens.client_type` = `WEB` · `EXTENSION`. 로그아웃은 현재 기기만 폐기 |
| 에러 | 만료 `TOKEN_EXPIRED`, 위조·없음 `TOKEN_INVALID`, Refresh 문제 `REFRESH_TOKEN_INVALID` |
| 비밀값 | `JWT_SECRET` 등은 환경변수로만 받는다. 코드·설정 파일에 기본값을 두지 않는다 |

- 인증 없이 허용할 API는 `SecurityConfig`에 명시한다. 재발급·로그아웃은 Access가 만료된 상태에서도 부르므로 인증 없이 열고, 대상은 Refresh 쿠키로 판단한다.
- 세션·기록 등 사용자 데이터는 항상 소유자(`user_id`)를 검증한다. 다르면 `FORBIDDEN`.
- 운영도 `SameSite=Lax`를 유지한다. 웹과 API를 같은 도메인 하위(예: `pbit.com` / `api.pbit.com`)에 두는 것이 전제다. 운영은 `Secure`를 켜므로 HTTPS가 필요하다.

## DB

### 이름

- 테이블·컬럼: `snake_case`, 테이블은 복수형 (`users`, `prompt_events`)
- 인덱스: `idx_{테이블약칭}_{용도}`, 유니크 인덱스: `uq_{테이블}_{컬럼}`
- 엔티티 클래스는 단수형 PascalCase (`User`, `PromptEvent`)

### 타입

| 대상 | DB | Java |
| --- | --- | --- |
| 클라이언트에 노출되는 ID | `UUID` | `UUID`, Java가 생성 (`@Id @GeneratedValue`). DB 기본값에 기대지 않는다 |
| 내부·로그 테이블 ID | `BIGINT IDENTITY` | `Long`, `GenerationType.IDENTITY` |
| 열거형 | `VARCHAR` + `CHECK` (PG ENUM 안 씀) | Java enum + `@Enumerated(EnumType.STRING)` |
| 시간 | `TIMESTAMPTZ`, UTC 저장 | `Instant` (`LocalDateTime` 사용 금지). 한국 시간은 표시·집계에서만 `atZone(ZoneId.of("Asia/Seoul"))` |
| 판정 원문 | `JSONB` | `@JdbcTypeCode(SqlTypes.JSON)` |

- 판정 원문(`element_flags` 등)은 FastAPI 응답 그대로 저장한다. 집계에 쓰는 값은 별도 컬럼으로 저장한다.
- 판정 로그에는 `rubric_version` · `domain` · `language`를 반드시 넣는다.
- 삭제: `users`만 소프트 삭제(`deleted_at`) + 개인정보 마스킹. 다른 테이블은 소프트 삭제를 쓰지 않는다. 탈퇴 회원은 조회·로그인·재발급에서 없는 회원으로 취급한다.
- `before_after_records.result_snapshot`은 저장 후 변경하지 않는다 (재채점 금지).

### 스키마 변경 (Flyway)

- 스키마는 Flyway로만 바꾼다. 위치: `src/main/resources/db/migration`
- 이미 적용된 `V*.sql`은 절대 수정하지 않는다. 변경은 새 버전 파일(`V{다음번호}__{설명}.sql`)로 추가한다.
- `spring.jpa.hibernate.ddl-auto`는 `validate`를 유지한다. 엔티티가 스키마를 바꾸지 않는다.
- 테이블·컬럼을 바꾸면 `docs/dev/specs/erd.md`도 같이 고친다.

## 테스트

- 웹 계층(컨트롤러·에러 응답·보안)은 `@WebMvcTest` + `@MockitoBean` 서비스. DB 없이 돈다.
  - 컨트롤러 생성에 필요하지만 그 테스트에서 안 쓰는 서비스도 `@MockitoBean`으로 등록한다.
- 서비스·리포지토리는 `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)`로 실제 PostgreSQL에서 돌린다. 테스트마다 롤백된다.
  - SQL로 직접 바꾼 값을 다시 읽을 때는 `entityManager.clear()`로 JPA 1차 캐시를 비운다.
- 테스트 메서드 이름은 한글로 동작을 설명한다 (예: `중복_이메일이면_409를_응답한다`).
- DB가 필요한 테스트는 PostgreSQL이 떠 있어야 한다 (`docker compose up -d`).

## 보안·금지

- `.env` 파일을 읽거나 커밋하지 않는다. 필요한 키는 `.env.example`에 이름만 추가한다.
- 비밀번호·토큰·비밀값, `prompt_text`(사용자 프롬프트 원문)는 로그에 남기지 않는다. 사용자는 `userId`로 식별한다.
- 명세에 없는 API·응답 필드를 임의로 추가하지 않는다.

## 명령어

```bash
docker compose up -d          # DB 실행 (레포 루트에서)
./gradlew bootRun             # 앱 실행 (backend/에서)
./gradlew test                # 테스트
```
