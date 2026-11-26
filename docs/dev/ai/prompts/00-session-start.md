# 00. Session start

## Purpose

From one Jira key, confirm this work's issue, branch, Stack, and reference documents, and organize the context for the next stage.

## When to use

- At the start of a new AI session
- When switching to another issue in the same session

## Inputs

- `KAN-<number>` (the user gives it)
- `AGENTS.md` (loaded automatically; in web chat, from project knowledge)

## Steps

Report results only. Do not change code or documents in this stage.

1. **Read the issue**
    - Confirm the title, type, parent issue, status, and assignee.
    - From the description, confirm the purpose, work items, done criteria, and related parts. Mark missing items as "비어 있음".
2. **Check the working location**
    - Check the current branch and `git status`.
    - Check whether the branch name follows `CONTRIBUTING.md` > `1.2 브랜치 네이밍`, and whether its KAN key matches the issue.
3. **Check linked PRs and Stack**
    - Check whether an open PR with the same KAN key exists.
    - If the current branch was cut from another work branch instead of `develop`, check whether that preceding PR is merged.
    - If it is merged, report that this PR's base must change to `develop` and that `origin/develop` must be merged if the branch is behind (`CONTRIBUTING.md` > `1.4 Stack 브랜치`). Do not run it.
4. **Choose reference documents**
    - Choose the documents to read for this work from `AGENTS.md` > `4. Reference documents` and the document list in `docs/README.md`.
    - Include the `AGENTS.md` of the folder that holds the files to change.
5. **Mark gaps**
    - If the description is empty or has no done criteria, mark it and recommend `20-issue-write` after `10-plan`.
    - List scope items with no grounds in the documents as questions.

## Output format

```markdown
### 세션 준비 — KAN-번호
| 항목 | 확인 결과 |
| --- | --- |
| 이슈 | 제목 · 유형 (상위 이슈) · 상태 · 담당자 |
| 설명란 | 목적 / 작업 내용 / 완료 조건 / 연관 파트 — 있음·비어 있음 |
| 브랜치 | 이름 · 규칙 일치 여부 · KAN 번호 일치 여부 |
| Stack | 선행 브랜치·PR과 머지 여부 / 없음 |
| 연결 PR | #번호 상태 / 없음 / 미확인 |
| 읽을 기준 문서 | 문서 > 섹션 |

### 확인이 필요한 항목
1.

다음: 10-plan (설명란이 비었으면 10-plan 결과로 20-issue-write를 거쳐 30-implement)
```

## Do not

- Fill in the issue scope, due date, or assignee by guessing.
- Create or switch branches without approval.
- Edit the issue.
- Plan or write code.

## Human gate

- The user reviews the report and says to proceed to the next stage.
- The user answers the items under 확인이 필요한 항목, or confirms them with the owning part.

## Tool differences

| | Claude Code | Web chat |
| --- | --- | --- |
| Reading the issue | Reads it through Jira MCP | The user pastes the issue body |
| Checking branch and PR | Checks with `git` and `gh` | The user tells it. If unknown, "미확인" |
