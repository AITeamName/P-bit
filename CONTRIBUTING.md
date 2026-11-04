# CONTRIBUTING

> **최종 수정일:** 2026-11-03 
> **기준 브랜치:** `develop` (기본 브랜치), `main` (배포/릴리즈)

---

## 0. 작업 사이클

| 순서 | 내용 |
| --- | --- |
| 1 | Jira 이슈 생성 |
| 2 | `develop` 최신화 → 작업 브랜치 생성 |
| 3 | 개발 및 커밋 |
| 4 | 원격 브랜치 push |
| 5 | Draft PR 생성 (base: `develop`) |
| 6 | Ready for review 전환 후 팀 채널에 리뷰 요청 |
| 7 | 리뷰 후 `Squash and merge` |
| 8 | 병합된 브랜치 삭제, Jira 이슈 `완료` 처리 |
| 9 | 팀원 전원 `git pull origin develop` |

---

## 1. 브랜치 전략

### 1.1 브랜치 종류

| 브랜치 | 용도 | 규칙 |
| --- | --- | --- |
| `main` | 배포/릴리즈 | `develop`·`hotfix/*`에서만 PR, Merge commit, 태그 부여 |
| `develop` | 개발 통합 브랜치 (기본 브랜치) | 작업 브랜치에서 PR, Squash and merge |
| `feat/*` | 신규 기능 | 서비스 기능, API, 화면, LLM 로직 구현 |
| `fix/*` | 버그 수정 | 재현 조건이 Jira 이슈에 있어야 함 |
| `refactor/*` | 구조 개선 | 동작 변경 금지 |
| `perf/*` | 성능·품질 개선 | PR에 Before/After 측정값 필수 |
| `docs/*` | 문서 | README, API 명세, 설계 문서 |
| `chore/*` | 설정·빌드·기타 | CI/CD, 의존성, 환경 설정 |
| `style/*` | 포맷팅 | 동작 변경 금지 |
| `hotfix/*` | 운영 긴급 수정 | `main`에서 분기 → `main` 병합 → `develop` 역병합 |

> 파트별 장기 브랜치(`backend`, `frontend` 등)는 두지 않습니다. 파트 구분은 **폴더**와 **커밋 scope**로 합니다.
> 

### 1.2 브랜치 네이밍

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

### 1.3 Scope 정의

**이 표에 없는 scope는 사용하지 않습니다.**

| 파트 | scope | 위치 |
| --- | --- | --- |
| 백엔드 (Spring Boot) | `be` | `backend/` |
| LLM 서버 (FastAPI) | `llm` | `ai-server/` |
| 웹 프론트엔드 | `web` | `frontend/web/` |
| 크롬 익스텐션 | `ext` | `frontend/extension/` |
| 인프라·CI/CD | `infra` | `docker-compose.yml`, `.github/` |
| 문서 | `docs` | `docs/` |

scope 판단 기준:

1. 한 PR이 두 scope를 건드리면 PR을 쪼갤 수 있는지 먼저 검토합니다.
2. 쪼갤 수 없으면 변경의 주된 목적이 속한 scope 하나만 씁니다.

---

## 2. 커밋 규칙 (Conventional Commits)

```
<type>(<scope>): <subject> (KAN-번호)
```

### 타입

| Type | 의미 |
| --- | --- |
| `feat` | 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 코드 개선 (기능 변경 없음) |
| `perf` | 성능·품질 개선 (측정값 필수) |
| `docs` | 문서 수정 |
| `style` | 포맷팅, import 정리 |
| `test` | 테스트 코드 |
| `chore` | 설정, 빌드, 기타 |
| `hotfix` | 긴급 수정 |

> **`perf`의 범위:** 응답 지연·처리량뿐 아니라 LLM 판정 정확도·루브릭 품질 개선도 포함합니다.
Before/After 측정이 있어야 완료로 판정 가능한 변경은 모두 `perf`입니다.
> 

### 작성 기준

- 한 커밋 = 한 목적. 기능·테스트·리팩터링은 분리합니다.
- 제목은 명령형·현재형, 마침표 없이. 한글 사용 가능합니다.
- 한 커밋에는 한 파트만. 여러 파트를 동시에 바꿔야 하면 커밋을 나눕니다.

### 예시

```bash
git commit -m "feat(be): 회원가입 API 구현 (KAN-12)"
git commit -m "fix(be): JWT 토큰 만료 시간 오류 수정 (KAN-31)"
git commit -m "feat(ext): 사이드바 진단 화면 구현 (KAN-20)"
git commit -m "feat(llm): 형식 요소 판정 로직 구현 (KAN-18)"
git commit -m "perf(llm): 루브릭 v2 적용 — clarity 정확도 0.71 → 0.83 (KAN-45)"
git commit -m "chore(infra): CI 워크플로우 추가 (KAN-29)"
```

---

## 3. 작업 흐름

### 3.1 작업 시작 전 최신화

작업을 시작할 때마다 반드시 실행합니다. 건너뛰면 나중에 충돌이 생깁니다.

```bash
git switch develop
git pull origin develop
git switch -c feat/KAN-18-llm-format-judgement
git push -u origin feat/KAN-18-llm-format-judgement   # 원격에 최초 push
```

### 3.2 작업 진행 및 커밋

```bash
# 담당 폴더만 스테이징 (git add . 지양)
git add backend/

# 스테이징 확인 — 다른 폴더 파일이 섞이지 않았는지 체크
git status

# 커밋
git commit -m "feat(be): 회원가입 API 구현 (KAN-12)"

# push
git push origin feat/KAN-12-signup-api
```

| 파트 | add 명령 |
| --- | --- |
| BE | `git add backend/` |
| LLM | `git add ai-server/` |
| FE 웹 | `git add frontend/web/` |
| FE 익스텐션 | `git add frontend/extension/` |
| 공통 문서 | `git add docs/` |

### 3.3 작업 중 develop 변경사항 반영

```bash
git checkout develop
git pull origin develop
git checkout feat/KAN-12-signup-api
git merge develop
```

---

## 4. Pull Request

### 4.1 PR 방향

```
feat/*   → develop  ✅
fix/*    → develop  ✅
develop  → main     ✅ (배포 시)
hotfix/* → main     ✅ (긴급 수정)
main     → develop  ✅ (hotfix 역병합)
```

### 4.2 PR 제목

```
[KAN-번호] type(scope): 작업 내용 요약
```

예시

```
[KAN-12] feat(be): 회원가입 API 구현
[KAN-18] feat(llm): 형식 요소 판정 로직 구현
[KAN-20] feat(ext): 사이드바 진단 화면 구현
[KAN-31] chore(be): 로컬 개발 환경 구축 (Docker DB + Spring 연결)
[KAN-45] perf(llm): 루브릭 v2 적용 — clarity 정확도 개선
```

> 💡 `Squash and merge` 시 이 제목이 `develop`의 커밋 메시지가 됩니다.
> 

### 4.3 PR 절차

1. Draft PR 생성 (base: `develop`, PR 템플릿 작성)
2. `Ready for review` 전환 후 팀 채널에 리뷰 요청 💬
3. 리뷰 후 `Squash and merge`
4. 병합된 브랜치 삭제
5. Jira 이슈 상태 `완료`로 변경
6. 팀원 전원 `git pull origin develop`

### 4.4 병합 정책

| 대상 | 방식 |
| --- | --- |
| 작업 브랜치 → `develop` | Squash and merge |
| `develop` → `main` (릴리즈) | Merge commit |
| `hotfix/*` → `main` | Merge commit |
| `main` → `develop` (역병합) | Merge commit |

### 4.5 PR 전 체크리스트

- [ ]  로컬에서 정상 동작 확인
- [ ]  커밋 컨벤션 준수
- [ ]  충돌(conflict) 해결 완료
- [ ]  내 담당 폴더 외 파일 변경 없음 (있다면 PR 본문에 사유 작성)
- [ ]  Jira 이슈 번호 연결 (`Closes #이슈번호` 또는 Jira 연결)
- [ ]  `.env`, API Key 등 민감 정보 커밋 없음

---

## 5. Hotfix

운영 중 긴급 수정이 필요한 경우 `main`에서 직접 분기합니다.

```bash
# 1. main 최신화 후 hotfix 브랜치 생성
git switch main && git pull origin main
git switch -c hotfix/KAN-번호-간단한-설명

# 2. 수정 후 커밋
git commit -m "hotfix(be): 로그인 세션 오류 수정 (KAN-번호)"

# 3. main으로 PR 생성 (base: main) → Merge commit 병합

# 4. develop 역병합 PR 생성 (base: develop, head: main)
#    제목: chore(infra): main → develop 동기화
```

> ⚠️ 역병합을 빠뜨리면 다음 릴리즈에서 같은 버그가 되살아납니다. `develop`은 직접 push가 막혀 있으므로 PR로만 반영합니다.
> 

---

## 6. 충돌(Conflict) 해결

```bash
# develop 최신화 후 내 브랜치에 병합
git switch develop && git pull origin develop
git switch feat/KAN-18-llm-format-judgement
git merge develop
```

```
<<<<<<< HEAD
내 브랜치의 코드
=======
develop(팀원)에서 온 코드
>>>>>>> develop
```

- `<<<`, `===`, `>>>` 마커를 모두 삭제하고 최종 코드만 남깁니다.
- 양쪽 로직이 모두 필요하면 직접 합쳐서 작성합니다.
- 해결이 어려우면 해당 코드를 작성한 팀원에게 알립니다.
- 충돌 해결 후 테스트를 다시 실행하고 커밋합니다.

```bash
git add .
git commit -m "chore(scope): develop 병합 충돌 해결"
```

---

## 7. 주의사항

### 🚫 절대 금지

- ❌ `main`·`develop` 브랜치에 직접 push
- ❌ 다른 사람 브랜치에 함부로 push
- ❌ 다른 파트 폴더를 상의 없이 수정
- ❌ `.env` 파일 커밋
- ❌ API Key, 비밀번호 등 민감 정보 커밋
- ❌ `ai-server/prompts/rubric/vN.yaml` 기존 파일 덮어쓰기 (버전업은 신규 파일 생성)

### ✅ 필수 수칙

- 작업 시작 전 Jira 이슈 생성 → 브랜치 생성
- 작업 시작 전 `git pull origin develop` 무조건 실행
- 충돌 발생 시 해당 파트 담당자와 상의 후 해결
- PR 전 반드시 로컬 테스트
- 커밋 메시지는 컨벤션 준수
- 커밋 작성자 이메일 = GitHub 계정 이메일

```bash
git config --global user.email "your@email.com"
```

---

## 메모

PR 제목·브랜치명·커밋 컨벤션 규칙이 바뀌면 이 파일을 함께 수정합니다.

---

end.