# 10. Plan

## Purpose

Compare the reference documents with the issue to find undecided decisions first, then build the goal, done criteria, and commit units from the settled scope.

## When to use

- After `00-session-start` and before implementation, once per issue
- Again when the issue scope or a decision changes

## Inputs

- The session report from `00-session-start`
- This work's reference documents and the `AGENTS.md` of the relevant folder

## Steps

### Step 1 — Find open decisions

1. Compare each work item in the issue against the reference documents.
2. Write the decisions the documents do not settle, and the ambiguous assumptions, as questions. Include options when there are any.
3. Output the questions and wait for answers. Do not move to Step 2 before the answers arrive.
    - If there are no questions, write "없음" and go to Step 2.

### Step 2 — Write the spec

1. Put the Step 1 answers into the decisions table.
2. Write the goal and done criteria. Write the done criteria in a checkable form (command results, response values, document match, and so on).
3. Summarize the grounds and scope of impact.

### Step 3 — Commit units

1. Split commits in dependency order. Each commit must not break the build or tests on its own.
2. One purpose per commit: split feature, test, and refactoring commits (`CONTRIBUTING.md` > `2. 커밋 규칙 (Conventional Commits)` > `작성 기준`).
3. One scope per commit. Message format: `CONTRIBUTING.md` > `2. 커밋 규칙 (Conventional Commits)`.

### When to stop

- If a contract document (`docs/dev/specs/`) must change, make that change the first commit and mark it "관련 파트 확인 필요".
- If another part's folder must change, write the target, the reason, and the part to confirm with.

## Output format

Step 1:

```markdown
### 빠진 결정
| # | 질문 | 선택지 | 관련 문서 > 섹션 |
| --- | --- | --- | --- |
```

Steps 2 and 3:

```markdown
### 결정 사항
| # | 항목 | 선택지 | 결정 |
| --- | --- | --- | --- |

### 목표 & 완료 기준
- 목표:
- 완료 기준:
    -

### 근거 및 영향 범위
| 항목 | 내용 |
| --- | --- |
| 기준 문서 | 문서 > 섹션 |
| 계약 변경 | 없음 / 있음 (문서 > 섹션, 확인할 파트) |
| 다른 파트 파일 | 없음 / 있음 (파일, 확인할 파트) |
| Stack base | `develop` / 선행 브랜치 |

### 커밋 단위
| # | 커밋 메시지 | 파일 | 요약 |
| --- | --- | --- | --- |

다음: (이슈 설명이 비었거나 범위가 바뀌었으면) 20-issue-write → 30-implement
```

## Do not

- Add features that are not in the planning documents.
- List an item that needs a decision as settled.
- Put APIs, fields, or error codes that are not in the documents into the scope.
- Start implementing. Move to `30-implement` only after the user says "구현해줘".

## Human gate

- Answer the Step 1 questions. If another part must decide, settle it with that part.
- Approve the decisions, done criteria, and commit units.
- If a contract change is needed, decide whether to do the document change first.

## Tool differences

| | Claude Code | Web chat |
| --- | --- | --- |
| Reading reference documents | Reads repository files directly | Reads only the documents in project knowledge. If one is missing, asks the user to paste it |
