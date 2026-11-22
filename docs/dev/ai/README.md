# AI 협업 가이드

> 담당: AI 협업 도구 · 최종 수정: 2026-11-22

AI 도구(Claude Code, Codex, claude.ai 웹 채팅)로 이 저장소 작업을 할 때 필요한 파일 구조, 단계별 프롬프트 사용법, Jira·GitHub 연결 방법을 적는다. AI가 따르는 규칙 자체는 루트 `AGENTS.md`에 있다.

`prompts/`라는 이름이 두 곳에 있다.

| 경로 | 무엇 |
| --- | --- |
| `docs/dev/ai/prompts/` | 개발자가 AI에게 단계별로 요청할 때 쓰는 프롬프트 (이 문서) |
| `ai-server/prompts/rubric/` | 서비스가 LLM에 보내는 판정 루브릭·문구 (제품 코드, `docs/dev/llm/model-design.md` 4장) |

---

## 1. 파일 구조

| 파일 | 읽는 도구 | 역할 |
| --- | --- | --- |
| `AGENTS.md` | 모든 도구 | AI 작업 규칙 원본 |
| `CLAUDE.md` | Claude Code | `@AGENTS.md`로 규칙을 불러오고, Claude Code 전용 규칙만 추가 |
| `backend/`·`frontend/`·`ai-server/`의 `AGENTS.md` | 모든 도구 | 그 폴더의 기술 규칙 (스택, 구조, 커밋 전 루틴) |
| 같은 폴더의 `CLAUDE.md` | Claude Code | 그 폴더 `AGENTS.md`를 불러옴 |
| `.mcp.json` | Claude Code | Jira·GitHub MCP 서버 (저장소 공유) |
| `.claude/settings.json` | Claude Code | push·Jira 이슈 생성·PR 생성 전 확인 창 |
| `docs/dev/ai/prompts/` | 모든 도구 | 단계별 프롬프트 7종 |
| `docs/dev/ai/review-checklist.md` | 모든 도구, 사람 | 리뷰 기준 |
| `docs/dev/ai/jira-guide.md` | 모든 도구, 사람 | Jira 이슈 양식 |

- AI 규칙을 바꿀 때는 `AGENTS.md`만 고친다.
- 개인 지시는 각자 `CLAUDE.local.md`에 둔다. 커밋되지 않는다.

---

## 2. 쓰는 곳별 차이

단계 프롬프트는 마크다운 글이라 어디서든 쓸 수 있다. 다른 것은 AI가 직접 할 수 있는 일의 범위다.

| 쓰는 곳 | 프롬프트 쓰는 법 | AI가 직접 할 수 있는 것 |
| --- | --- | --- |
| Claude Code (VS Code·IntelliJ·터미널·데스크톱 앱) | "`docs/dev/ai/prompts/10-plan.md`대로 KAN-42 계획 세워줘"처럼 경로만 말한다 | 파일 읽기·수정, git·`gh`, Jira·GitHub MCP, 규칙 파일 자동 적용 |
| Codex 등 다른 코딩 도구 | 위와 같다. `AGENTS.md`는 도구가 읽는다 | 도구에 따라 다르다. MCP는 각자 설정 |
| claude.ai 웹 채팅 | 프롬프트 파일을 프로젝트 지식에 넣어 두거나 붙여 넣는다 (§6) | 글 작성·분석만. 파일 수정·git은 사람이 한다 |

- 터미널에서 사람이 직접 하는 일은 따로 있다: 커밋·push, 환경변수 등록, MCP 설정.

---

## 3. 단계별 프롬프트

### 3.1 흐름

```text
[00 세션 시작] ── KAN 번호 → 이슈·브랜치·Stack·기준 문서 확인
      │
[10 계획] ── 빠진 결정 질문 → (사람 답변) → 결정 사항·목표·커밋 단위
      │
[20 이슈 작성] ── 초안 파일 → (사람 승인) → Jira 등록·수정
      │
[30 구현] ── 커밋마다 보고 → (사람 승인) → 커밋
      │
[40 리뷰 A] ── review-checklist 판정 ── 미통과 → 30
      │
[50 PR 작성] ── 초안 파일 → (사람 승인) → Draft PR
      │
[팀 리뷰] ── 리뷰어가 40 B 사용, 코멘트는 사람이 등록
      │
[60 피드백 반영] ── 코멘트·CI 실패·충돌 → 30 · 필요하면 50
```

- 각 단계는 결과를 낸 뒤 사람의 승인을 기다린다. AI가 다음 단계로 스스로 넘어가지 않는다.
- push, Jira 이슈 등록, PR 등록, 리뷰 코멘트 등록은 사람이 승인하거나 직접 한다.

### 3.2 프롬프트별 요청 방법

| 단계 | 프롬프트 | 요청에 넣을 것 |
| --- | --- | --- |
| 세션 시작 | `00-session-start.md` | `KAN-번호` |
| 계획 | `10-plan.md` | (00 결과 다음이면 없음) |
| 이슈 작성 | `20-issue-write.md` | 신규 / `KAN-번호` 수정, 하위 작업이면 상위 이슈 |
| 구현 | `30-implement.md` | "구현해줘", 커밋 번호 |
| 리뷰 | `40-review.md` | "자체 리뷰" / `PR #번호` 리뷰, 볼 파일·범위 |
| PR 작성 | `50-pr-write.md` | `KAN-번호` |
| 피드백 반영 | `60-feedback.md` | `PR #번호` / 오류 메시지 전문 |

### 3.3 프롬프트 구성

모든 프롬프트는 같은 섹션으로 되어 있다.

| 섹션 | 내용 |
| --- | --- |
| 목적 | 이 단계가 만드는 것 |
| 언제 | 이 단계를 시작하는 때 |
| 입력 | AI가 읽을 파일·값 |
| 절차 | 순서대로 할 일 |
| 출력 형식 | 결과 표·본문 형식 |
| 하지 않는 것 | 이 단계에서 하면 안 되는 것 |
| 사람이 결정할 것 | AI가 멈추고 사람이 정하는 지점 |
| 도구별 차이 | Claude Code와 웹 채팅의 차이 |

- 프롬프트가 실제 작업과 맞지 않으면 개인 설정에서 고쳐 쓰지 말고 개선 이슈를 만든다.

---

## 4. 초안 파일

이슈 본문, PR 본문, 리뷰 코멘트, 답글은 대화창에만 쓰지 않고 `.claude/drafts/`에 마크다운 파일로 저장한다. 에디터에서 보고 고친 뒤 그대로 등록한다.

| 글 | 파일 |
| --- | --- |
| Jira 이슈 본문 | `.claude/drafts/KAN-<번호>-issue.md` |
| PR 본문 | `.claude/drafts/KAN-<번호>-pr.md` |
| PR 리뷰 코멘트 초안 | `.claude/drafts/PR-<번호>-review.md` |
| 리뷰 답글·반영 결과 | `.claude/drafts/PR-<번호>-feedback.md` |

- `.gitignore`의 `.claude/*` 규칙으로 커밋되지 않는다. 다 쓴 초안은 지워도 된다.
- PR은 `gh pr create --draft --body-file .claude/drafts/KAN-<번호>-pr.md`로 파일 그대로 등록한다.

---

## 5. MCP·CLI 연결 (Claude Code)

처음 한 번만, PC마다 한다. 토큰은 본인 터미널에서만 입력하고, Claude Code 대화창·`.env`·`.mcp.json`에는 넣지 않는다.

| 연동 | 방식 | 쓰는 단계 |
| --- | --- | --- |
| Jira | MCP — `.mcp.json`의 `jira` (`mcp-jira-stdio`) | `00` 이슈 조회, `20` 이슈 등록·수정 |
| GitHub | `gh` CLI (기본) + MCP — `.mcp.json`의 `github` (GitHub 공식 원격 서버) | `40` B PR 조회, `50` PR 등록, `60` 코멘트·CI 조회 |

연동이 없어도 모든 단계는 붙여 넣기 방식으로 끝까지 진행된다.

### 5.1 토큰 준비

| 값 | 어디서 | 비고 |
| --- | --- | --- |
| `JIRA_BASE_URL` | — | `https://ppbbit.atlassian.net` |
| `JIRA_EMAIL` | — | Atlassian 로그인 이메일 |
| `JIRA_API_TOKEN` | https://id.atlassian.com/manage-profile/security/api-tokens → Create API token | 만료일을 정해서 발급 |
| `GITHUB_TOKEN` | GitHub → Settings → Developer settings → Fine-grained tokens | 대상 저장소만, Contents·Pull requests·Issues 읽기 |

### 5.2 환경변수 등록

Claude Code는 `.mcp.json`의 `${VAR}`를 **실행 중인 프로세스의 OS 환경변수**로 채운다. `.env`·`.env.local` 파일은 읽지 않는다.

**Windows — PowerShell에서 실행**

```powershell
[Environment]::SetEnvironmentVariable("JIRA_BASE_URL", "https://ppbbit.atlassian.net", "User")
[Environment]::SetEnvironmentVariable("JIRA_EMAIL", "본인 이메일", "User")
[Environment]::SetEnvironmentVariable("JIRA_API_TOKEN", (Read-Host "Jira API token"), "User")
[Environment]::SetEnvironmentVariable("GITHUB_TOKEN", (Read-Host "GitHub token"), "User")
```

**macOS / Linux — `~/.zshrc` 또는 `~/.bashrc`**

```bash
export JIRA_BASE_URL="https://ppbbit.atlassian.net"
export JIRA_EMAIL="본인 이메일"
export JIRA_API_TOKEN="..."
export GITHUB_TOKEN="..."
```

등록 후 **에디터(VS Code·IntelliJ)를 완전히 종료하고 다시 연다.** 열려 있던 프로세스는 새 환경변수를 받지 못한다.

### 5.3 연결 확인

새 터미널(Git Bash)에서 확인한다. 토큰은 값 대신 길이만 본다.

```bash
# ① 환경변수
echo "$JIRA_BASE_URL"
echo "${#JIRA_API_TOKEN} ${#GITHUB_TOKEN}"     # 0이 아니면 등록됨

# ② Jira API 직접 호출 — 여기서 실패하면 MCP 문제가 아니다
curl -s -u "$JIRA_EMAIL:$JIRA_API_TOKEN" "$JIRA_BASE_URL/rest/api/3/myself" | head -c 200

# ③ gh CLI
gh auth status            # 로그인 안 되어 있으면 gh auth login
gh pr list --limit 3
```

④ Claude Code에서

1. 저장소 루트에서 세션을 연다. 처음이면 프로젝트 MCP 서버 사용 승인 창이 뜬다.
2. `/mcp` → `jira`·`github`가 connected인지 확인한다.
3. 대화창에 "KAN-번호 이슈 조회해줘" → 제목·상태가 나오면 성공이다.

### 5.4 선택: GitHub MCP를 로컬 Docker로 실행

GitHub MCP 서버 버전을 고정하고 싶으면 `.mcp.json`은 그대로 두고, 개인 설정에 같은 이름 `github`로 공식 Docker 이미지를 추가한다. 같은 이름이면 개인(local) 설정이 우선한다. Docker Desktop이 실행 중이어야 한다.

```bash
# Git Bash · macOS · Linux
claude mcp add github --scope local -e GITHUB_PERSONAL_ACCESS_TOKEN="$GITHUB_TOKEN" -- docker run -i --rm -e GITHUB_PERSONAL_ACCESS_TOKEN ghcr.io/github/github-mcp-server:v2.2.0
claude mcp get github           # Scope: Local config 이면 적용됨

# 되돌리기
claude mcp remove github --scope local
```

- 이 명령은 토큰 값을 개인 설정 파일(`~/.claude.json`)에 저장한다. 되돌리면 함께 지워진다.
- 버전을 올릴 때는 태그(`v2.2.0`)만 바꿔 다시 추가한다.

### 5.5 실패할 때

| 증상 | 원인 | 조치 |
| --- | --- | --- |
| ① 값이 비어 있음 | 등록 후 에디터를 재시작하지 않음 | 에디터를 완전히 종료하고 다시 열기 |
| ① 값이 비어 있음 (`.env`에 넣음) | Claude Code는 `.env`·`.env.local`을 읽지 않음 | §5.2대로 OS 환경변수로 등록 |
| ② `401 Unauthorized` | 이메일·토큰 불일치, 토큰 만료 | 토큰 재발급, `JIRA_EMAIL`이 Atlassian 계정 이메일인지 확인 |
| ② `404` (myself는 성공) | 프로젝트 권한 없음 | Jira 관리자에게 KAN 프로젝트 권한 요청 |
| ②는 성공, ④ `Network error` | Claude Code 프로세스가 환경변수를 못 받음 | 에디터 재시작, `/mcp`에서 재연결 |
| `/mcp`에 서버가 없음 | 프로젝트 MCP 승인을 거부함 | 세션을 다시 열어 승인 |
| PowerShell 명령이 `command not found` | Git Bash에서 PowerShell 문법을 실행함 | 터미널을 PowerShell로 바꿔 실행 |
| `$env:...` 값이 문자 그대로 들어감 | Git Bash에서 PowerShell 변수 문법을 씀 | Git Bash에서는 `$GITHUB_TOKEN`처럼 쓴다 |

### 5.6 보안

- 토큰을 `.mcp.json`·`.env`·이슈·PR·스크린샷에 남기지 않는다.
- 노출이 의심되면 즉시 토큰을 폐기하고 재발급한다. push된 경우에도 커밋 정리보다 폐기를 먼저 한다.

---

## 6. claude.ai 웹 채팅에서 쓰기

저장소를 직접 열지 않고 웹 채팅으로 이슈 작성·계획·문서·리뷰 보조를 하는 경우다. 채팅에는 `AGENTS.md`·`.mcp.json`·`.claude/settings.json`이 자동으로 적용되지 않으므로 직접 준비한다.

### 6.1 Claude 프로젝트 만들기 (처음 한 번)

1. claude.ai → Projects → 새 프로젝트
2. 프로젝트 지식에 아래 파일을 넣는다. GitHub 연동으로 `develop` 브랜치에서 고르면 나중에 동기화만 하면 된다.

| 파일 | 용도 |
| --- | --- |
| `AGENTS.md` | 팀 공통 규칙 |
| 주로 다루는 폴더의 `AGENTS.md` | 그 폴더 규칙 (있을 때만) |
| `docs/README.md` | 문서 목록 — 어떤 문서를 볼지 찾는 기준 |
| `docs/dev/ai/jira-guide.md` | Jira 이슈 양식 |
| `docs/dev/ai/review-checklist.md` | 리뷰 기준 |
| `docs/dev/ai/prompts/` 중 쓰는 단계 | 단계 프롬프트 |

- 저장소 전체를 넣지 않는다. 필요한 파일만 넣어야 엉뚱한 문서를 참고하지 않는다.

3. 프로젝트 지침에 한 줄을 넣는다.

```
프로젝트 지식의 AGENTS.md 규칙을 따른다. 저장소 파일을 직접 수정하거나 git·Jira·GitHub에 직접 등록하지 않고, 결과는 마크다운 코드블록으로 낸다.
```

- 규칙(`AGENTS.md`)이나 양식(`jira-guide.md`)이 바뀌면 프로젝트 지식을 다시 동기화한다.

### 6.2 단계별로 웹에서 되는 것

| 단계 | 웹에서 | 사람이 할 것 |
| --- | --- | --- |
| `00` | 이슈 본문을 붙여 넣으면 요약·빈칸 표시 | 브랜치·PR 상태는 직접 확인 |
| `10` | 빠진 결정·목표·커밋 단위 초안 | — |
| `20` | 이슈 초안 (코드블록) | Jira에 붙여 넣기 |
| `30` | 설명·예시 수준 | 파일 수정·커밋은 저장소에서 |
| `40` B | PR diff를 붙여 넣으면 판정 표·코멘트 초안 | 코멘트는 GitHub에 직접 등록 |
| `50` | PR 본문 초안 | PR 생성은 직접 |
| `60` | 리뷰 코멘트를 붙여 넣으면 분류·답글 초안 | 수정·답글 등록은 직접 |

### 6.3 Jira·GitHub 커넥터 (선택)

- 채팅에서 Jira·GitHub를 직접 조회·등록하려면 claude.ai 커넥터(Atlassian, GitHub)를 연결한다. 저장소의 `.mcp.json`과는 별개다.
- 커넥터로 등록할 때도 초안을 먼저 보여 주고 승인한 뒤 등록한다.

---

## 7. 참고

- AGENTS.md 형식: <https://agents.md>
- Claude Code 메모리(`CLAUDE.md`, `@import`): <https://docs.claude.com/en/docs/claude-code/memory>
- Claude Code MCP(`.mcp.json`): <https://docs.claude.com/en/docs/claude-code/mcp>
- GitHub MCP Server: <https://github.com/github/github-mcp-server>
