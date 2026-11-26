# 30. Implementation

## Purpose

Implement the settled scope one commit unit at a time. Get the user's approval for each commit.

## When to use

- After the user approves the commit units from `10-plan` and says "구현해줘"

## Inputs

- The decisions and commit units from `10-plan`
- The `AGENTS.md` of the folder that holds the files to change (stack, structure, rules, check commands)
- Reference documents (chosen in `00-session-start`)
- Similar existing code — before creating a new pattern, look for an existing implementation in the same folder.

## Steps

### A. Before starting

1. Confirm the work branch. If it does not exist, propose the commands and run them after approval. Leave out `git push -u`; a push needs its own approval.
    - Normal: `git switch develop` → `git pull origin develop` → `git switch -c <branch>` (`CONTRIBUTING.md` > `3.1 작업 시작 전 최신화`)
    - Stack: `git fetch origin` → `git switch -c <branch> origin/<preceding-branch>` (`CONTRIBUTING.md` > `1.4 Stack 브랜치`)
2. If the commit units and the actual code do not fit, report it before starting.

### B. Each commit

1. Write or change only the files planned for this commit. If another file is needed, say so and wait for approval.
2. If a judgment the documents do not settle comes up (a structural choice, a value or error handling not in the specification), do not decide it; report it as an options table.
3. If implementation is impossible without changing a contract (`docs/dev/specs/`), stop and propose the document change.
4. Run the check commands in the folder's `AGENTS.md` (`AGENTS.md` > `3. Scope and prohibitions`) and show the results. If they fail, do not commit. If the folder has none, say so.
5. Give the commit report: changed files, design judgments, check results, `git diff --stat`, commit message.
6. Commit when the user says "커밋해". Give the push command. Run it only if the user asks you to push (`AGENTS.md` > `0. Absolute rules`).
7. If something fails or you get stuck, organize the cause with `60-feedback` part C (CI failure, conflict, error).

## Output format

For each commit:

```markdown
### 커밋 n — `type(scope): 요약 (KAN-번호)`
| 파일 | 역할 |
| --- | --- |

설계 판단
- (근거: 문서 > 섹션)

확인 결과
| 명령 | 결과 |
| --- | --- |

결정 필요 (있을 때)
| 판단 | 선택지 | 장점 | 단점 | 근거 문서 |
| --- | --- | --- | --- | --- |

`git diff --stat`
```

## Do not

- Change files outside the plan without approval.
- Use a response format, status code, or error code that is not in the specification.
- Decide values that are not in the documents.
- Change files owned by another part without that part's confirmation.
- Commit while the routine fails. Report a command you did not run as passed.
- Push without being asked.
- Ask the user to type secret values into the Claude Code terminal. Tell them to run commands that include secrets in a separate terminal.

## Human gate

- Decide the items under 결정 필요. If a contract change is needed, decide whether to do the document change first.
- For each commit, review the report and the diff and approve the commit.
- Push (or approve the push).

## Part blocks

Read the documents for the part you change. The rules live in these documents, not here.

- **Backend** — `backend/AGENTS.md`, `docs/dev/specs/interfaces.md` > `7`
- **Frontend** — `frontend/AGENTS.md`, `docs/dev/specs/api.md`, `interfaces.md` > `5`
- **LLM** — `ai-server/AGENTS.md`, `docs/dev/llm/model-design.md`
- **Infrastructure** — `docs/ops/deploy.md`, `docs/ops/env.md`

## Tool differences

| | Claude Code | Web chat |
| --- | --- | --- |
| File changes, commits | Does them directly (commits after approval) | Cannot. Gives explanations and example code only; a person changes files and commits in the repository |
