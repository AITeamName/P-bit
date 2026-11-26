# CONTRIBUTING

> **최종 수정일:** 2026-11-26
> **기준 브랜치:** `develop` (기본 브랜치), `main` (배포/릴리즈)

---

## 0. 작업 사이클

| 순서 | 내용 | Jira 상태 |
| --- | --- | --- |
| 1 | Jira 이슈 생성 (8절) | 해야 할 일 |
| 2 | `develop` 최신화 → 작업 브랜치 생성 (선행 PR이 머지 전이면 Stack, 1.4) | 진행 중 |
| 3 | 개발 및 커밋 | 진행 중 |
| 4 | 원격 브랜치 push | 진행 중 |
| 5 | 셀프 리뷰 후 Draft PR 생성 (base: `develop`, Stack이면 선행 브랜치) | 진행 중 |
| 6 | Ready for review 전환, 팀 채널에 리뷰 요청 | PR 리뷰 중 |
| 7 | 리뷰 반영 후 `Merge commit`으로 머지 | PR 리뷰 중 |
| 8 | 병합된 브랜치 삭제, Jira 이슈 상태 변경 (QA가 필요하면 QA 대기) | 완료 / QA 대기 |
| 9 | 팀원 전원 `git pull origin develop` | — |

- AI 협업 도구로 이 사이클을 진행하는 방법은 10절을 봅니다.

---

## 1. 브랜치 전략

### 1.1 브랜치 종류

| 브랜치 | 용도 | 규칙 |
| --- | --- | --- |
| `main` | 배포/릴리즈 | `develop`·`hotfix/*`에서만 PR, Merge commit, 태그 부여 |
| `develop` | 개발 통합 브랜치 (기본 브랜치) | 작업 브랜치에서 PR, Merge commit |
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

- 기본적으로 Jira 이슈 하나에 작업 브랜치 하나를 씁니다.
- 한 이슈를 PR 여러 개로 나눌 때는 브랜치 이름의 `<subject>`로 구분합니다 (예: `docs/KAN-42-docs-cleanup`, `docs/KAN-42-ai-collab-prompts`).

### 1.3 Scope 정의

**이 표에 없는 scope는 사용하지 않습니다.**

scope는 변경한 파일의 **담당 파트**입니다. 문서인지 코드인지는 type(`docs`, `feat` 등)으로 구분합니다.

| 파트 | scope | 코드·설정 | 문서 |
| --- | --- | --- | --- |
| 백엔드 (Spring Boot) | `be` | `backend/` | |
| LLM 서버 (FastAPI) | `llm` | `ai-server/` | `docs/dev/llm/` |
| 웹 프론트엔드 | `web` | `frontend/web/` | |
| 크롬 익스텐션 | `ext` | `frontend/extension/` | |
| 프론트엔드 공통 | `fe` | `frontend/` 공통 (`AGENTS.md`, `CLAUDE.md` 등) | |
| 인프라·CI/CD | `infra` | `docker-compose.yml`, `.github/workflows/` | `docs/ops/` |
| 디자인 | `de` | | `docs/brand/` |
| AI 협업 도구 | `ai` | 루트 `AGENTS.md`·`CLAUDE.md`, `.claude/settings.json`, `.mcp.json` | `docs/dev/ai/` |
| 레포 설정 | `repo` | `.gitignore`, 루트 `.env.example`, `.github/CODEOWNERS`, `.github/pull_request_template.md`, 폴더 구조 | |
| 팀 공통 문서 | `docs` | | `README.md`, `CONTRIBUTING.md`, `docs/planning/`, `docs/dev/specs/`, `docs/adr/` |

예시:

```bash
docs(be): 백엔드 기술 명세 인증 절 수정 (KAN-35)    # 파트 소유 문서
docs(docs): README 로컬 실행 방법 갱신 (KAN-31)     # 팀 공통 문서
chore(ai): Claude Code 공용 설정 추가 (KAN-41)      # AI 협업 설정
chore(repo): CODEOWNERS 담당자 추가 (KAN-40)        # 레포 설정
```

scope 판단 기준 (위에서부터 처음 맞는 것):

1. 파트 폴더 안의 파일이거나 파트 소유 문서면 그 파트 scope를 씁니다. `backend/AGENTS.md`, `backend/.env.example`도 `be`입니다.
2. AI 협업 설정이면 `ai`를 씁니다.
3. 팀 공통 문서면 `docs`를 씁니다.
4. 그 외 루트 설정은 `repo`를 씁니다.

여러 scope에 걸친 변경:

1. 한 PR이 두 scope를 건드리면 PR을 쪼갤 수 있는지 먼저 검토합니다.
2. 쪼갤 수 없으면 변경의 주된 목적이 속한 scope 하나만 씁니다.
3. 여러 파트가 함께 승인해야 하는 계약 파일(`docs/dev/specs/interfaces.md` 등)도 위치 기준으로 scope를 정합니다. `.github/CODEOWNERS`에 적힌 파트 모두에게 리뷰를 받습니다. GitHub은 그중 한 명의 승인만 있어도 통과시키므로, 나머지 파트 확인은 PR에서 직접 요청합니다.

### 1.4 Stack 브랜치

선행 PR이 아직 `develop`에 머지되지 않았는데 그 위에서 이어서 작업해야 할 때 씁니다.

**만들기**

```bash
git fetch origin
git switch -c <새-브랜치> origin/<선행-브랜치>
git push -u origin <새-브랜치>
```

- PR의 base는 `develop`이 아니라 **선행 브랜치**로 지정합니다.
    - base를 `develop`으로 두면 선행 PR의 변경까지 diff에 섞입니다.
- Stack의 두 번째 이상 PR에는 `🧱 stack` 라벨을 **반드시** 붙입니다 (4.6 PR 본문 작성).
- PR "리뷰어에게"에 선행 PR 번호와, 선행 PR 머지 후 base를 바꾼다는 점을 적습니다.

**선행 브랜치가 바뀌었을 때**

```bash
git fetch origin
git switch <현재-브랜치>
git merge origin/<선행-브랜치>        # 또는 git rebase origin/<선행-브랜치>
git push                               # rebase했다면 git push --force-with-lease
```

**선행 PR이 `develop`에 머지됐을 때**

선행 PR은 Merge commit으로 머지되어 선행 커밋이 `develop`에 그대로 들어갑니다. 현재 브랜치를 옮기거나 다시 쓸 필요가 없습니다.

- GitHub에서 PR의 base를 `develop`으로 바꿉니다. 머지 후 선행 브랜치를 삭제하면 자동으로 바뀝니다.
- Files changed에 내 변경만 남았는지 확인합니다.
- PR이 `develop`보다 뒤처졌다고 나오면 `develop`을 merge해서 올립니다.
- base를 바꾸는 것만으로는 CI가 다시 돌지 않습니다. 머지 전에 `ci-result` ✅가 있는지 확인하고, 없으면 다음 push 때 생깁니다.

```bash
git fetch origin
git switch <현재-브랜치>
git merge origin/develop
git push
```

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
- 다른 파트 문서를 옮겨 올 때는 "옮기기" 커밋과 "고치기" 커밋을 나눕니다. 리뷰어가 원문과 수정분을 따로 볼 수 있습니다.

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
git diff --cached --stat

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

- 파일을 지정해 `git add` 해도, 이미 스테이징돼 있던 다른 변경은 함께 커밋됩니다. 커밋 직전에 `git diff --cached --stat`으로 범위를 확인합니다.

### 3.3 작업 중 develop 변경사항 반영

```bash
git switch develop
git pull origin develop
git switch feat/KAN-12-signup-api
git merge develop
```

- 커밋하지 않은 변경이 있으면 먼저 커밋하거나 `git stash`로 보관한 뒤 반영합니다.

```bash
git stash            # 변경 임시 보관
# (위 develop 반영 명령)
git stash pop        # 보관한 변경 복원
```

### 3.4 실수했을 때 되돌리기

| 상황 | 명령 | 주의 |
| --- | --- | --- |
| 잘못 스테이징한 파일 | `git restore --staged <파일>` | 수정 내용은 그대로 남음 |
| 커밋 전 수정 내용 버리기 | `git restore <파일>` | 버린 내용은 복구하기 어려움. diff 먼저 확인 |
| push 전 마지막 커밋 메시지 수정 | `git commit --amend -m "<type>(<scope>): <subject> (KAN-번호)"` | push한 커밋에는 쓰지 않음 |
| push 전 마지막 커밋 취소 (변경은 유지) | `git reset --soft HEAD~1` | push한 커밋에는 쓰지 않음 |
| 이미 push·머지된 커밋 되돌리기 | `git revert <커밋>` (머지된 PR 전체는 `git revert -m 1 <머지 커밋>`) | 히스토리를 지우지 않고 되돌리는 커밋을 새로 만듦. `develop`은 새 브랜치에서 revert한 뒤 PR로 올림 |
| 실수로 `develop`에서 작업함 (커밋 전) | `git switch -c <새-브랜치>` | 커밋 전 변경은 새 브랜치로 따라옴 |

- 이미 push한 커밋은 팀원에게 영향을 줄 수 있으므로 임의로 amend하거나 강제 push하지 않습니다. 수정은 새 커밋으로 올립니다.
- 예외: 내 브랜치를 선행 브랜치 위로 rebase했을 때만 `--force-with-lease`를 씁니다 (1.4 "선행 브랜치가 바뀌었을 때").
- 비밀값(토큰·비밀번호·키)을 커밋했다면 커밋을 지우기 전에 **해당 값을 먼저 폐기·재발급**하고 팀에 알립니다. push된 값은 이미 노출된 것으로 봅니다.

---

## 4. Pull Request

### 4.1 PR 방향

```
feat/*   → develop  ✅
fix/*    → develop  ✅
develop  → main     ✅ (배포 시)
hotfix/* → main     ✅ (긴급 수정)
main     → develop  ✅ (hotfix 역병합)
Stack 후속 → 선행 브랜치 ✅ (선행 PR 머지 후 develop으로 변경, 1.4)
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

> 💡 Merge commit으로 머지하면 이 제목이 `develop`의 머지 커밋 메시지가 됩니다.
> 

### 4.3 PR 절차

1. 셀프 리뷰 (`docs/dev/ai/review-checklist.md`) — 미통과가 없어야 PR을 만듭니다
2. Draft PR 생성 (base: `develop`, Stack PR은 선행 브랜치 · PR 템플릿 작성 · Stack이면 `🧱 stack` 라벨) 후 `Ready for review` 전환, 팀 채널에 리뷰 요청 💬
3. 리뷰 반영 후 `Merge commit`으로 머지
4. 병합된 브랜치 삭제
5. Jira 이슈 상태 변경 (`완료`, QA가 필요하면 `QA 대기`)
6. 팀원 전원 `git pull origin develop`

### 4.4 병합 정책

| 대상 | 방식 |
| --- | --- |
| 작업 브랜치 → `develop` | Merge commit |
| Stack 후속 PR | 선행 PR 머지 → base를 `develop`으로 변경 → Merge commit |
| `develop` → `main` (릴리즈) | Merge commit |
| `hotfix/*` → `main` | Merge commit |
| `main` → `develop` (역병합) | Merge commit |

- `develop`은 저장소 설정상 Merge commit만 허용합니다.
- 머지 조건: 아래를 모두 충족해야 합니다 (`docs/ops/deploy.md` > `1. CI — 빌드·테스트 (사용 중)`).
    - `ci-result` 체크 통과
    - 승인 2명 이상
    - `.github/CODEOWNERS`에 등록된 경로를 바꿨다면 그 경로 소유자의 승인
- PR 생성·Ready 전환·머지와 CI 결과는 Discord로 알림이 갑니다 (`.github/workflows/discord-notification.yml`). PR 본문에 CI 결과를 따로 적지 않습니다.
    - base가 `develop`·`main`이 아닌 Stack PR은 CI·알림 대상이 아닙니다.

### 4.5 PR 전 체크리스트

- [ ]  로컬에서 정상 동작 확인
- [ ]  커밋 컨벤션 준수
- [ ]  충돌(conflict) 해결 완료
- [ ]  내 담당 폴더 외 파일 변경 없음 (있다면 PR 본문에 사유 작성)
- [ ]  브랜치 이름·PR 제목에 `KAN-번호` 포함 (Jira 자동 연결)
- [ ]  `.env`, API Key 등 민감 정보 커밋 없음

### 4.6 PR 본문 작성

- `.github/pull_request_template.md`의 섹션을 모두 채웁니다. 내용이 없는 "(선택)" 섹션은 지웁니다.
- 게시하기 전에 템플릿의 HTML 안내 주석과 예시·자리표시자를 지웁니다. 저장소의 템플릿 원본은 그대로 둡니다.
- 구현 내용은 실제 diff에 있는 변경만 적습니다. 계획만 있는 내용을 완료로 적지 않습니다.
- 테스트는 실제로 수행한 항목만 체크합니다. 실행하지 않았다면 `테스트 미진행`과 이유를 적습니다.
- 다른 파트 파일을 바꿨다면 "리뷰어에게"에 대상·이유·확인할 파트를 적습니다.
    - `@계정` 멘션은 Draft라도 PR을 만드는 순간 알림이 갑니다. 알림을 보낼 때가 아니면 파트명으로 적습니다.
- 리뷰 피드백을 반영한 커밋이 있으면 구현 내용에 `**피드백:** 반영한 내용`으로 추가합니다.
- 글머리 한 항목이 한 문장을 넘길 것 같으면 하위 글머리로 내립니다.
- 문서 항목 번호는 `#N`이 아니라 `N번`으로 적습니다 (예: `미결정 9번`). GitHub이 `#N`을 이슈·PR 링크로 바꿉니다. 리뷰 코멘트·답글도 같습니다.
- 라벨은 선택입니다. 단, Stack의 두 번째 이상 PR에는 `🧱 stack` 라벨이 필수입니다.

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
git add <해결한 파일>
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
- ❌ `ai-server/prompts/rubric/vN/` 이미 배포된 버전 디렉터리 수정 (버전업은 디렉터리를 새로 만든다. 상세: `docs/dev/llm/model-design.md` 4.1)

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

## 8. Jira 이슈

### 8.1 작성

- 제목·필드 규칙과 유형별 본문 양식은 `docs/dev/ai/jira-guide.md`를 따릅니다.
    - 제목: `[파트] 동사 + 대상`
    - 유형: 개발(Story·Task) · 버그 · 비개발 · 회의. 하위 작업(Subtask)은 가능하면 만들지 않습니다.
- 하루 안에 끝나지 않을 크기면 착수 전에 나눕니다.
- 완료 조건이나 범위가 바뀌면 이슈 본문에도 반영합니다.
- 한 이슈를 PR 여러 개로 나누면 이슈 작업 내용에 PR별 항목을 적고, 마지막 PR이 머지된 뒤 `완료`로 바꿉니다.

### 8.2 상태 흐름

- 상태와 각 상태로 옮기는 시점은 `docs/dev/ai/jira-guide.md` > `8. 상태 흐름`을 따릅니다.

---

## 9. 리뷰

### 9.1 리뷰어가 확인할 내용

- PR 본문 `💬 리뷰어에게`의 확인 요청을 먼저 봅니다.
- 그다음 Files changed에서 Jira 이슈 범위와 실제 변경이 맞는지 확인합니다.
- 기준은 `docs/dev/ai/review-checklist.md`입니다. AI 없이 리뷰할 때도 같은 기준을 씁니다.
- 모르는 부분은 PR 대화에서 질문합니다.

### 9.2 코멘트 작성

수정이 필요한 곳은 Files changed의 해당 줄에 코멘트를 달고, 아래 세 가지를 모두 적습니다.

- **지적:** 파일 경로와 코드 위치, 무엇이 문제인지
- **근거:** 기준 문서 위치(`문서 > 섹션 > 찾을 문구`) 또는 관련 코드의 permalink
    - permalink는 파일 화면에서 줄을 선택하고 `y`를 누르면 커밋 해시가 들어간 주소로 바뀝니다.
- **대안:** 바꿀 코드나 방법
    - 몇 줄 수정이면 GitHub `suggestion` 블록으로 적습니다.
    - 하나로 정할 수 없으면 선택지를 나열합니다.
- 문서 항목 번호는 `N번`으로 적습니다 (4.6).

### 9.3 제출 방식

| 종류 | 제출 | 예 |
| --- | --- | --- |
| 필수 (머지 전 반드시 수정) | `Review changes → Request changes` | 빌드·테스트 실패, 보안 문제, 계약(`docs/dev/specs/`) 위반 |
| 권장·질문 | `Comment` | 컨벤션·가독성, 의도 확인 |
| 승인 | `Review changes → Approve` | 아래 내용을 코멘트로 함께 적음 |

승인 코멘트에 적을 것:

- PR의 주제 (무엇을 바꾼 PR인지 한 줄)
- `💬 리뷰어에게`의 확인 요청마다 확인한 결과
    - 확인 요청이 "특이사항 없음"이면 직접 확인한 범위
- 이전에 변경 요청을 남겼다면 요청마다 반영 여부

---

## 10. AI 협업 도구로 작업할 때

Claude Code 등 AI 협업 도구를 쓸 때의 규칙과 사용법은 아래에 있습니다. 이 절에는 위치만 적습니다.

| 알고 싶은 것 | 문서 |
| --- | --- |
| AI가 따르는 규칙 | 루트 `AGENTS.md` (Claude Code는 `CLAUDE.md`가 불러옴), 파트 폴더의 `AGENTS.md` |
| 워크플로·도구 연결(Jira·GitHub)·웹 채팅에서 쓰기 | `docs/dev/ai/README.md` |
| 단계별 프롬프트 (세션 시작 → 계획 → 이슈 → 구현 → 리뷰 → PR → 피드백) | `docs/dev/ai/prompts/` |
| Jira 이슈 양식 | `docs/dev/ai/jira-guide.md` |
| 리뷰 기준 | `docs/dev/ai/review-checklist.md` |

AI 도구를 써도 바뀌지 않는 것:

- **push, Jira 이슈 등록·수정, PR 등록·머지, 답글 등록은 사람이 승인하거나 직접 합니다.** AI는 초안을 먼저 보여 줍니다.
    - 대화로 승인하는 것과 Claude Code 승인 창이 함께 뜨는 것의 구분은 `AGENTS.md` > `0. Absolute rules` > `Approval` 표를 따릅니다.
- 리뷰·승인·변경 요청은 AI가 등록하지 않습니다. 사람이 직접 등록합니다.
- 이슈·PR 본문 초안은 `.claude/drafts/`에 마크다운 파일로 만들 수 있습니다.
    - `.claude/` 아래는 `.gitignore`로 커밋되지 않습니다 (`.claude/settings.json`만 예외).
    - 파일을 열어 직접 고친 뒤 등록합니다.
- 날짜·담당자·이슈 번호·검증 결과는 추측해서 채우지 않습니다. 비워 두고 확인합니다.
- 토큰·비밀번호는 AI 대화창에 입력하지 않습니다.

---

## 11. 문서 기준

- 작업 기준은 저장소 문서입니다. 문서 목록과 작성 규칙은 `docs/README.md`를 봅니다.
- Notion·회의·대화에서 정한 내용은 저장소 문서나 ADR(`docs/adr/`)에 반영된 뒤에 기준이 됩니다.
- 계약 문서(`docs/dev/specs/`)를 바꿔야 하면 구현보다 문서를 먼저 고치고 관련 파트의 확인을 받습니다.
- 같은 규칙을 여러 문서에 옮겨 적지 않습니다. 원본 한 곳에 두고 다른 곳은 위치만 가리킵니다.
- 문서 위치는 줄 번호가 아니라 `문서 > 섹션 > 찾을 문구`로 알려 줍니다.

---

## 메모

PR 제목·브랜치명·커밋 컨벤션 규칙이 바뀌면 이 파일을 함께 수정합니다.

---

end.
