# 60. Applying feedback

## Purpose

Organize review comments on a PR, CI failures, and conflicts with `develop`. Handle what will be applied as commits, and what will not be applied as replies.

## When to use

- When review comments are posted on a PR
- When a local or CI build or test fails
- When a conflict occurs with `develop` or the preceding branch

## Inputs

- The PR number
- The review comments, the CI result, or the full error message
- The related code and reference documents

## Steps

### A. Review comments

1. Read all review comments and conversations on the PR: conversation comments with `gh pr view <number> --comments`, inline comments with `gh api repos/{owner}/{repo}/pulls/<number>/comments`.
2. Classify each comment as one of:
    - 반영 (apply): the finding is right; fix it
    - 논의 필요 (needs discussion): judgments differ, or another part must decide
    - 반영 안 함 (decline): give the reason (grounds: `document > section` or code)
3. Output the classification table and wait for approval.
4. After approval:
    - 반영: fix each piece of feedback as one commit, in the `30-implement` B way.
    - 논의 필요, 반영 안 함: draft replies.
5. Save the reply drafts and the applied results to `.claude/drafts/PR-<number>-feedback.md` and give the path. Write document item numbers as `N번`, not `#N` (`AGENTS.md` > `8. Draft files`).
6. If the applied commits require changing the PR body, update it with `50-pr-write` step 11.

### B. After replies are posted

- Check that all handled conversations are resolved (GitHub MCP `pull_request_read` with `get_review_comments`, or the PR page). If any remain open, tell the user.

### C. CI failure, conflict, error

1. Summarize the symptom in one line.
2. List possible causes from most to least likely, each with a way to check it (command, log, or file location).
3. Ask before running check commands. After approval, run them to narrow the cause.
4. When the cause is confirmed, propose a fix. If it affects service boundaries or contracts, mark it "팀 협의 필요" and stop.
5. For a conflict, explain the intent of both changes and propose how to combine them. Resolve by merging (`git merge origin/develop`, or `origin/<preceding-branch>` for a Stack) as in `CONTRIBUTING.md` > `6. 충돌(Conflict) 해결`. Do not propose rebase or force push. For conflicts in another part's code, mark that it must be confirmed with that part.

## Output format

A:

```markdown
### 코멘트 분류 — PR #번호
| # | 코멘트 (작성자·위치) | 분류 | 처리 | 근거 |
| --- | --- | --- | --- | --- |

### 반영 커밋 계획
| # | 커밋 메시지 | 대상 코멘트 |
| --- | --- | --- |
```

A reply file (`.claude/drafts/PR-<number>-feedback.md`), one section per comment:

```markdown
## n. 코멘트 위치 (스레드 ID)

(무엇을 어떻게 반영했는지 한 줄)

- (바꾼 내용)

반영 커밋: `해시`
```

C:

```markdown
### 원인 후보
| 순위 | 후보 | 확인 방법 | 결과 |
| --- | --- | --- | --- |

### 수정 방안
- 대상:
- 커밋 메시지:
- 팀 협의 필요: 예 / 아니오

### 정리
- 증상:
- 원인:
- 해결:
- 교훈:
```

## Do not

- Propose a fix that only hides the symptom without confirming the cause (disabling a test, ignoring an exception, only raising a timeout). If it cannot be avoided, give the reason and a follow-up issue together.
- Proceed with a fix that needs team agreement without approval.
- Post a reply or resolve a conversation without chat approval. The user usually posts them; post with `gh` only when the user asks.
- Mix several pieces of feedback in one commit.

## Human gate

- Approve the classification. Settle 논의 필요 items with the reviewer or the owning part.
- Approve running check commands and the fix.
- Post the replies and resolve the conversations, or approve the drafts and ask the AI to post them.

## Tool differences

| | Claude Code | Web chat |
| --- | --- | --- |
| Reading comments and CI | `gh pr view <number> --comments`, `gh api …/pulls/<number>/comments`, `gh pr checks <number>` | The user pastes the comments or error message |
| Fixing | Commits through `30-implement` B (after approval) | Gives a fix proposal only. A person commits |
| Replies | Saved as a file under `.claude/drafts/` | Output as a Markdown code block |
