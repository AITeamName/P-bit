# 40. Review

## Purpose

Check changes against `docs/dev/ai/review-checklist.md`.

- A. Self-review: the author does it before the PR.
- B. Review assist: used when reviewing a teammate's PR.

In both modes, people make the decisions.

## When to use

- A: after all commits of `30-implement`, before creating the PR
- B: when reviewing a teammate's PR

## Inputs

- A: run `git fetch origin` first, then `git diff origin/<base>...HEAD` — `<base>` is `develop` for a normal PR, the preceding branch for a Stack PR
- B: the PR number, the PR body (especially `💬 리뷰어에게`), and the files and range the reviewer chose
- `docs/dev/ai/review-checklist.md` and the documents each item points to

## Steps

### A. Self-review

1. Look at the changed paths with `git diff --stat origin/<base>...HEAD` and identify the parts (`CONTRIBUTING.md` > `1.3 Scope 정의`).
2. Choose the checklist's common sections and the sections for those parts. For sections you do not apply, give the reason.
3. For each item, open the grounds document and judge it as one of 통과 (pass), 미통과 (fail), 해당 없음 (not applicable), or 미확인 (unverified).
    - For 통과, give the file and location, or the command run and its result.
4. Run the check commands of the relevant parts (`AGENTS.md` > `3. Scope and prohibitions`). Leave commands you could not run as 미확인 and give the reason.
5. For 미통과 items, propose a fix only. Do not change anything.

### B. Review assist

1. Read `💬 리뷰어에게` in the PR body first, and record the result for each request.
2. Identify the parts from the changed paths. Apply all common sections (A–E) and the sections for the changed parts (`review-checklist.md` > `적용 범위`).
3. Look from three viewpoints: function (gaps or mismatches against the specification), code (responsibility, exceptions, boundaries), and tests (scenarios).
4. List separately where the PR body and the code differ.
5. Write all three of the following for each finding (`CONTRIBUTING.md` > `9. 리뷰`).
    - 지적: file path and location, and what is wrong
    - 근거: the checklist number and `document > section`, or a permalink to the related code
    - 대안: the code or method to change. For a few lines, use a `suggestion` block. If there are several options, list them all.
6. Give each finding a severity.
    - 필수 (required — Request changes): build or test failure, security problem, contract violation
    - 권장 (recommended — Comment): rules and consistency, readability, performance
    - 질문 (question — Comment): unclear intent or needs confirmation
7. If there is no 필수 finding, draft an approval comment.
8. Save the result to `.claude/drafts/PR-<number>-review.md` and give the path. Do not post anything on GitHub. Write document item numbers as `N번`, not `#N` (`AGENTS.md` > `8. Draft files`).

## Output format

A:

```markdown
### 적용 섹션
- 적용: 
- 해당 없음: (이유)

### 판정
| # | 판정 | 근거 |
| --- | --- | --- |

### 실행한 명령
| 명령 | 결과 |
| --- | --- |

### 수정 필요
| # | 위치 | 수정 방안 |
| --- | --- | --- |
```

B (saved to the file):

```markdown
### 리뷰어에게 확인 결과
| 확인 요청 | 결과 | 근거 |
| --- | --- | --- |

### 지적
**지적**: `파일 경로` 위치 — 무엇이 문제인지
**근거**: [체크리스트 번호] `문서 > 섹션` 또는 permalink
**대안**: 바꿀 코드나 방법
**심각도**: 필수 / 권장 / 질문

### PR 본문과 코드의 차이
-

### 승인 코멘트 초안 (필수 지적이 없을 때)
{PR 주제} 확인했습니다.
- {리뷰어에게 항목} 확인했습니다.
- (이전 변경 요청이 있었으면) {요청 항목} 반영 확인했습니다.
승인합니다.
```

## Do not

- Mark a command you did not run as 통과.
- A: Fix 미통과 items without approval.
- B: Post comments, approvals, or change requests on GitHub directly. A person posts them, without exception.
- B: Mark a matter of taste without grounds as 필수.
- B: Write a finding that is missing any of 지적, 근거, or 대안.

## Human gate

- A: Decide how to fix each item and fix it through `30-implement` B. Do not create the PR until there are no 미통과 items.
- B: Decide which findings to post and post them on GitHub directly. Approvals and change requests are also done directly.

## Tool differences

| | Claude Code | Web chat |
| --- | --- | --- |
| Reading the diff | A uses `git diff`, B uses `gh pr diff <number>` | The user pastes the diff or changed files |
| Running commands | Runs the check commands directly | Cannot run them. Those items are 미확인 |
| Result | Saved as a file under `.claude/drafts/` | Output as a Markdown code block |
