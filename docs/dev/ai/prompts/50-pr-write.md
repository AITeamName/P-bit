# 50. PR writing

## Purpose

Write a PR body from the actual changes and the checks actually run only, and register it as a Draft PR after the user approves.

## When to use

- After `40-review` A shows no 미통과 items and the user has pushed the branch

## Inputs

- `git diff <base>...HEAD`, `git log --oneline <base>..HEAD` — `develop` for a normal PR, the preceding branch for a Stack PR
- `.github/pull_request_template.md`
- The `40-review` A result
- The PR rules in `CONTRIBUTING.md`

## Steps

1. Fill each section by following the guide comments in the PR template. Remove the guide comments and examples from the posted version.
2. Title: `[KAN-<number>] type(scope): summary` (`CONTRIBUTING.md` > `4.2 PR 제목`).
3. In the implementation section, list only changes visible in the current diff.
    - Add the reason as a sub-bullet only for items that involved a design judgment.
    - If a sentence gets long, split it into sub-bullets.
4. In the test section, check only what appears under 실행한 명령 in `40-review`, and give the commands and results in the summary.
5. In the self-checklist, check only items that passed in `40-review`, and add `(해당 없음)` at the end of items that do not apply.
6. If files owned by another part changed, write the target, reason, and confirmation request in `💬 리뷰어에게`. If you do not know the owner's GitHub account, leave it empty and ask the user.
7. For a Stack PR, write the preceding PR and the plan to change the base after it merges in `💬 리뷰어에게`.
8. Save the body to `.claude/drafts/KAN-<number>-pr.md` and give the path.
9. When the user has reviewed or edited the file and says to register, register a Draft PR.
    - `gh pr create --draft --base <base> --title "<title>" --body-file .claude/drafts/KAN-<number>-pr.md`
    - For a Stack PR (base is not `develop`), always add `--label "🧱 stack"`. Other labels are optional; ask the user whether to add them.
10. After registration, give the PR number and link.

## Output format

```markdown
### PR 초안 — 파일: .claude/drafts/KAN-<번호>-pr.md
- 제목:
- base ← head:
- Draft: 예
- 라벨: Stack이면 `🧱 stack` (필수) / 그 밖은 선택 — 후보:

### 본문 밖
- 비워 둔 값과 질문:
- 리뷰어 후보: (CODEOWNERS 기준, 모르면 질문)
```

Save the body to the file, and show only the summary above in the chat.

## Do not

- Describe planned work as done.
- Check tests that are not in the `40-review` result.
- Include test results of other parts that are not in the diff.
- Register without approval, or edit a registered PR without approval.
- Assign reviewers without approval.

## Human gate

- Review the draft file, fill the empty values, and approve registration.
- Assign reviewers, check CI, and switch to Ready for review.

## Tool differences

| | Claude Code | Web chat |
| --- | --- | --- |
| Reading the diff | `git diff`, `git log` | The user pastes a change summary or the diff |
| Draft | Saved as a file under `.claude/drafts/` | Output as a Markdown code block |
| Registration | After approval, registers a Draft with `gh pr create --body-file` (an approval prompt appears before it runs) | The user creates it on GitHub directly |
