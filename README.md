# 🎯 P-bit (Prompt Trainer)

AI 프롬프트 작성 능력을 향상시키는 질문력 트레이너 서비스의 **모노레포**입니다.
백엔드 · AI · 프론트(웹 / 크롬 익스텐션)가 하나의 레포에서 함께 관리됩니다.

---

## 📁 폴더 구조

| 폴더 | 내용 | 담당 |
|---|---|---|
| `backend/` | Spring Boot API 서버 | BE |
| `ai/` | FastAPI AI 서버 | AI |
| `frontend/web/` | 웹사이트 (로그인 · 온보딩 · 대시보드) | FE |
| `frontend/extension/` | 크롬 익스텐션 사이드바 | FE |
| `docs/` | API 명세 · ERD 등 문서 | 전원 |
| `docker-compose.yml` | 로컬 DB (PostgreSQL) | BE · INFRA |

> ⚠️ **자기 담당 폴더만 수정합니다.** 다른 파트 폴더 수정이 필요하면 담당자와 먼저 상의하세요.

---

## 🌿 브랜치 전략

| 브랜치 | 설명 |
|---|---|
| `main` | 🚀 배포 브랜치 (직접 수정 금지) |
| `develop` | 🔧 개발 통합 브랜치 (직접 수정 금지) |
| `feat/*` | ✨ 기능 개발 |
| `fix/*` | 🐛 버그 수정 |
| `refactor/*` | ♻️ 리팩토링 |
| `docs/*` | 📝 문서 |
| `chore/*` | 🔨 설정 / 빌드 |

> 파트별 장기 브랜치(backend, frontend 등)는 두지 않습니다. 파트 구분은 **폴더**와 **커밋 scope**로 합니다.

### 브랜치 네이밍 규칙

```
{타입}/KAN-{지라번호}-{간단한-설명}
```

예시

```
feat/KAN-12-signup-api
fix/KAN-31-jwt-expiration
chore/KAN-29-monorepo-setup
```

> 💡 브랜치 이름에 `KAN-번호`가 있으면 **Jira 이슈에 자동 연결**됩니다.

---

## 🔄 작업 순서

### 1️⃣ 작업 시작 전 최신화

```bash
git checkout develop
git pull origin develop
git checkout -b feat/KAN-12-signup-api
```

> 💡 `pull` 하면 다른 파트(프론트 / AI) 코드도 같이 받아집니다. **정상입니다.**
> 다른 폴더 파일만 바뀌는 것이라 내 작업에는 영향이 없습니다.

### 2️⃣ 작업 진행 및 커밋

```bash
# 내 담당 폴더만 스테이징 (git add . 지양)
git add backend/

# 스테이징된 파일 확인 — 다른 폴더 파일이 섞이지 않았는지 체크
git status

# 커밋 (컨벤션 준수)
git commit -m "feat(be): 회원가입 API 구현 (KAN-12)"

# 원격 저장소에 푸시
git push origin feat/KAN-12-signup-api
```

| 파트 | add 명령 |
|---|---|
| BE | `git add backend/` |
| AI | `git add ai/` |
| FE 웹 | `git add frontend/web/` |
| FE 익스텐션 | `git add frontend/extension/` |
| 공통 문서 | `git add docs/` |

### 3️⃣ 작업 중 develop 변경사항 반영

```bash
git checkout develop
git pull origin develop
git checkout feat/KAN-12-signup-api
git merge develop
```

---

## 📮 Pull Request 규칙

### PR 방향

```
feat/* → develop   ✅
develop → main     ✅ (배포 시)
```

### PR 제목

```
[KAN-12] feat(be): 회원가입 API 구현
```

### PR 전 체크리스트

- [ ] 로컬에서 정상 동작 확인
- [ ] 커밋 컨벤션 준수
- [ ] 충돌(conflict) 해결 완료
- [ ] 내 담당 폴더 외 파일 변경 없음 (있다면 PR 본문에 사유 작성)
- [ ] Jira 이슈 번호 연결

### PR 절차

1. 기능 개발 완료 후 로컬 테스트
2. `develop` 브랜치로 PR 생성 (PR 템플릿 작성)
3. 팀 채널에 리뷰 요청 💬
4. 리뷰 승인 후 머지
5. 머지된 브랜치 삭제
6. Jira 이슈 상태 `완료`로 변경

---

## ✍️ 커밋 컨벤션

```
<type>(<scope>): <subject> (KAN-번호)
```

### Type

| Type | 설명 |
|---|---|
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 코드 리팩토링 (기능 변화 X) |
| `docs` | 문서 수정 |
| `style` | 코드 포맷팅 (기능 변화 X) |
| `test` | 테스트 코드 |
| `chore` | 빌드, 설정 파일 수정 |

### Scope (파트)

| Scope | 대상 폴더 |
|---|---|
| `be` | `backend/` |
| `ai` | `ai/` |
| `web` | `frontend/web/` |
| `ext` | `frontend/extension/` |
| `infra` | `docker-compose.yml`, `.github/` |
| `docs` | `docs/` |

### 예시

```bash
git commit -m "feat(be): 프롬프트 진단 API 구현 (KAN-15)"
git commit -m "fix(be): JWT 토큰 만료 시간 오류 수정 (KAN-31)"
git commit -m "feat(ext): 사이드바 진단 화면 구현 (KAN-20)"
git commit -m "feat(ai): 형식 요소 판정 로직 구현 (KAN-18)"
git commit -m "chore(infra): 백엔드 CI 워크플로 추가 (KAN-29)"
```

> 💡 한 커밋에는 한 파트만. 여러 파트를 동시에 바꿔야 하면 커밋을 나눠주세요.

---

## ⚠️ 주의사항

### 🚫 절대 금지

- ❌ `main`, `develop` 브랜치에 직접 push
- ❌ 다른 사람 브랜치에 함부로 push
- ❌ 다른 파트 폴더를 상의 없이 수정
- ❌ `.env` 파일 커밋
- ❌ API Key, 비밀번호 등 민감 정보 커밋

### ✅ 필수 수칙

- 충돌 발생 시 해당 파트 담당자와 상의 후 해결
- PR 전 반드시 로컬 테스트
- 커밋 메시지는 컨벤션 준수
- 작업 시작 전 Jira 이슈 생성 → 브랜치 생성
- 커밋 작성자 이메일 = GitHub 계정 이메일 (`git config --global user.email`)

---

## 🔒 환경 변수 관리

보안을 위해 모든 API Key 및 민감 정보는 `.env` 파일에서 관리합니다.

- `.env` 파일은 절대 GitHub에 push하지 마세요 (`.gitignore`에 등록됨)
- 필요한 키 목록은 각 폴더의 `.env.example`을 참고하세요
- 실제 값은 팀 노션 페이지에서 확인 후 로컬에 복사하여 사용해주세요

```
backend/.env
ai/.env
frontend/web/.env
frontend/extension/.env
```

---

## 🐳 로컬 실행

```bash
# DB 실행
docker compose up -d

# 백엔드
cd backend
./gradlew bootRun
```

AI · 프론트 실행 방법은 각 폴더의 README를 참고하세요.