# API 명세

프론트(웹·익스텐션) ↔ 백엔드 API의 **공통 규칙**만 적는다. 엔드포인트별 요청·응답은 코드에서 자동 생성되는 Swagger가 기준이다.

## 1. 기본

| 항목 | 규칙 |
| --- | --- |
| Base URL | `/api/v1` |
| 엔드포인트 명세 | Swagger UI — 로컬 `http://localhost:8080/swagger-ui.html` (`local` 프로필에서만 열린다) |
| 형식 | JSON, 필드 이름 camelCase |
| 시간 | ISO 8601 UTC, 끝에 `Z` (예: `2026-11-23T03:00:00Z`). 한국 시간 변환은 화면에서 한다 |
| ID | UUID 문자열 |
| 요청 추적 | 모든 응답에 `X-Request-Id` 헤더. 에러 응답 `error.requestId`와 같은 값이다. 문의·버그 제보 때 이 값을 같이 준다 |
| CORS | 허용 출처만 호출 가능, `credentials` 허용. 응답 헤더 중 `X-Request-Id`, `Retry-After`를 읽을 수 있다 |

## 2. 인증

### 2.1 토큰

| 토큰 | 형식 | 유효 시간 | 전달 |
| --- | --- | --- | --- |
| Access Token | JWT | 30분 | 응답 본문 `accessToken` → 요청 헤더 `Authorization: Bearer {accessToken}` |
| Refresh Token | 불투명 문자열 | 14일 | 웹: 쿠키 `refresh_token` (본문에 없음). 익스텐션: 본문 (익스텐션 로그인 구현 시 확정) |

- 응답 본문 `expiresIn`은 Access 유효 시간(초)이다.
- Access는 메모리에만 둔다. Refresh 쿠키는 `HttpOnly`라 스크립트로 읽을 수 없고, 읽을 필요도 없다.

### 2.2 Refresh 쿠키 (웹)

| 속성 | 값 | 이유 |
| --- | --- | --- |
| `HttpOnly` | 켬 | 스크립트가 토큰을 읽지 못하게 한다 |
| `SameSite` | `Lax` | 다른 사이트에서 보낸 요청에는 실리지 않는다 |
| `Path` | `/api/v1/auth` | 재발급·로그아웃 요청에만 실린다 |
| `Max-Age` | 1209600 (14일) | Refresh 유효 시간과 같다 |
| `Secure` | 운영 켬 · 로컬 끔 | 로컬은 http라 켜면 저장되지 않는다 |

- 쿠키를 주고받으려면 웹은 `/auth/*` 요청에 `credentials: 'include'`(axios는 `withCredentials: true`)를 붙인다.
- 운영은 웹과 API를 같은 도메인 하위(예: `pbit.com` / `api.pbit.com`)에 둔다. 다른 도메인이면 쿠키가 실리지 않는다.

### 2.3 웹 흐름

| 상황 | 호출 | 결과 |
| --- | --- | --- |
| 가입·로그인 | `POST /auth/signup` · `POST /auth/login` | 본문에 Access, `Set-Cookie`로 Refresh |
| 일반 API | `Authorization: Bearer {Access}` | |
| 새로고침·Access 만료 | `POST /auth/refresh` (쿠키 자동 전송) | 새 Access + 새 Refresh 쿠키. 이전 Refresh는 폐기된다 |
| 로그아웃 | `POST /auth/logout` (쿠키 자동 전송) | 항상 204 + 쿠키 삭제. Access 불필요 |

재발급·로그아웃은 Access가 만료된 상태에서도 부를 수 있도록 인증 없이 열려 있다.

### 2.4 401 처리

| `error.code` | 뜻 | 프론트 동작 |
| --- | --- | --- |
| `TOKEN_EXPIRED` | Access 만료 | `POST /auth/refresh` 후 원래 요청을 한 번 다시 보낸다 |
| `TOKEN_INVALID` | Access 없음·위조·회원 없음 | 로그인 화면으로 |
| `REFRESH_TOKEN_INVALID` | Refresh 없음·만료·폐기·이미 사용 | 로그인 화면으로 |
| `INVALID_CREDENTIALS` | 로그인 이메일·비밀번호 불일치 | 로그인 폼에 `userMessage` 표시 |

- 여러 요청이 동시에 `TOKEN_EXPIRED`를 받으면 재발급은 한 번만 하고 나머지는 그 결과를 기다린다. Refresh는 한 번 쓰면 폐기되므로 동시에 두 번 부르면 하나는 `REFRESH_TOKEN_INVALID`가 된다.

## 3. 에러 응답

### 3.1 형식

모든 에러는 같은 모양이다. `message` 필드는 없다.

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "userMessage": "입력한 내용을 다시 확인해주세요.",
    "retryable": false,
    "requestId": "8f1c2a4e-5b6d-4e7f-9a0b-1c2d3e4f5a6b",
    "fieldErrors": [
      { "field": "email", "reason": "올바른 이메일 형식이 아니에요" }
    ]
  },
  "fallback": null
}
```

| 필드 | 설명 |
| --- | --- |
| `code` | 에러 코드. 프론트 분기는 이 값으로 한다 |
| `userMessage` | 화면에 그대로 보여줄 문구 |
| `retryable` | 같은 요청을 다시 보내면 성공할 수 있는지 |
| `requestId` | `X-Request-Id`와 같은 값 |
| `fieldErrors` | 입력 검증 실패 시 필드별 이유. 없으면 빈 배열 |
| `fallback` | AI 기능 실패 시 대체 결과 (AI 연동 시 사용, 지금은 항상 `null`) |

### 3.2 에러 코드

코드 기준: `backend/src/main/java/com/pbit/api/global/error/ErrorCode.java`

| 코드 | HTTP | userMessage | 상황 |
| --- | --- | --- | --- |
| `VALIDATION_ERROR` | 400 | 입력한 내용을 다시 확인해주세요. | 입력 형식 오류. `fieldErrors` 참고 |
| `REQUIRED_TERMS_NOT_AGREED` | 400 | 필수 약관에 동의해주세요. | 가입 시 필수 약관 미동의 |
| `PASSWORD_MISMATCH` | 400 | 비밀번호가 일치하지 않아요. | 가입 시 비밀번호 확인 불일치 |
| `INVALID_CREDENTIALS` | 401 | 이메일 또는 비밀번호를 확인해주세요. | 로그인 실패 (원인을 구분하지 않는다) |
| `TOKEN_EXPIRED` | 401 | 로그인이 만료되었어요. | Access 만료 |
| `TOKEN_INVALID` | 401 | 다시 로그인해주세요. | Access 없음·위조 |
| `REFRESH_TOKEN_INVALID` | 401 | 다시 로그인해주세요. | Refresh 문제 |
| `FORBIDDEN` | 403 | 접근 권한이 없어요. | 다른 사용자의 데이터 |
| `NOT_FOUND` | 404 | 요청한 주소를 찾을 수 없어요. | 없는 경로 |
| `TERM_NOT_FOUND` | 404 | 약관을 찾을 수 없어요. | 없는 약관 코드 |
| `METHOD_NOT_ALLOWED` | 405 | 지원하지 않는 요청이에요. | 잘못된 HTTP 메서드 |
| `EMAIL_DUPLICATED` | 409 | 이미 사용 중인 이메일이에요. | 가입 시 이메일 중복 |
| `NICKNAME_DUPLICATED` | 409 | 이미 사용 중인 닉네임이에요. | 가입 시 닉네임 중복 |
| `INTERNAL_ERROR` | 500 | 잠시 후 다시 시도해주세요. | 예상하지 못한 서버 오류 |

AI 연동 에러 코드(`AI_TIMEOUT` 등)는 AI 연동 API 구현 시 추가한다.

## 4. 변경 규칙

- API를 추가·변경하면 컨트롤러의 Swagger 어노테이션(`@Operation`, `@ApiResponse`)을 같이 고친다.
- 이 문서의 공통 규칙(인증·쿠키·에러 형식·에러 코드)을 바꾸면 이 문서를 먼저 고치고 프론트에 공유한다.
- 웹 연동 중 명세와 다르게 구현한 내용은 노션 연동 기록 변경 로그에 남긴다.

## 변경 이력

| 날짜 | 내용 | 작성 |
| --- | --- | --- |
| 2026-11-23 | 공통 규칙 작성. 엔드포인트별 명세는 Swagger로 대체 (KAN-81) | 백엔드(BE) |
