# 20. Issue writing

## Purpose

Write a Jira issue body from the settled scope, and register it, or update an existing issue, after the user approves.

## When to use

- After `10-plan` settles the scope and decisions, once per issue
- When `00-session-start` finds an existing issue whose description is empty or differs from the scope

## Inputs

- The decisions, goal and done criteria, and commit units from `10-plan` — or a scope the user gives
- `docs/dev/ai/jira-guide.md` (title and field rules, templates by type)
- When updating an existing issue: that issue's current content

## Steps

1. **Decide whether it is new or an update.**
    - For an update, read the current title and body, and report every difference from the plan (title, missing items, leftover placeholders).
    - Do not assume the body is correct just because it exists.
2. **Choose the type.** Choose the template in `jira-guide.md` that fits the kind of work.
    - Use the type the template names: Story or Task for development, Bug for a bug (`jira-guide.md` > `3`·`5`·`6`·`7`). Do not create a subtask (Subtask) unless the user explicitly asks for one (`jira-guide.md` > `4. 하위 작업 (Subtask)`).
    - When updating an existing subtask, confirm its parent issue key and check that the parent issue exists.
3. **Write the body.**
    - Write only the settled scope. Do not write implementation choices that are not decided.
    - Put reference document locations and prerequisite issues in the reference and dependency fields.
    - Leave values not in the inputs (due date, assignee, issue keys) empty, and list them as questions outside the body.
    - Do not copy template example sentences as facts. Remove guide comments and placeholders.
    - Write the lists `jira-guide.md` marks as checkboxes (work items, done criteria, to-dos, follow-ups) as `- [ ]`. Write other lists as plain bullets (`-`).
4. **Save it as a draft file.** `.claude/drafts/KAN-<number>-issue.md` (for a new issue with no key yet, `KAN-new-<summary>-issue.md`). Give the path.
    - For an update, show only the changed parts as a comparison. If the description was empty, show the whole body.
5. **Wait for approval.** When the user has reviewed or edited the file and says to register, register or update it with the file content as it is.
    - If the body has checkboxes (`- [ ]`), convert the whole body to ADF and send it with `format: "adf"` (`jira-guide.md` > `9. AI로 작성·등록할 때`). With `format: "markdown"`, checkboxes stay as `[ ]` text.
    - After registering, read the issue again and check that the number of checklist items matches the draft.
6. After registration, give the issue key and link.

## Output format

```markdown
### 이슈 초안 — 파일: .claude/drafts/KAN-<번호>-issue.md
- 신규 / 수정:
- 유형: Story / Task / Bug (기존 Subtask 수정이면 상위 이슈)
- 제목:

### 본문 밖
- 고른 양식과 이유:
- 비워 둔 값과 질문:
- (수정일 때) 바뀌는 부분:
```

Save the body to the file, and show only the summary above in the chat.

## Do not

- Fill empty values by guessing.
- Register without approval, or edit a registered issue without approval.
- Change the assignee, labels, or status without approval.

## Human gate

- Review the draft file, fill the empty values, and approve registration.
- After registration, check the assignee, labels, and epic.
- If there are related parts, add their owners as watchers.

## Tool differences

| | Claude Code | Web chat |
| --- | --- | --- |
| Reading an existing issue | Reads it through Jira MCP | The user pastes it |
| Draft | Saved as a file under `.claude/drafts/` | Output as a Markdown code block |
| Registration | After approval, creates or updates it through Jira MCP and gives the key. Converts to ADF when there are checkboxes (creating or updating shows an approval prompt before it runs) | The user pastes it into Jira and turns checkboxes into checklist items in the editor (`[]` + space). If the Atlassian connector is connected, registers it after approval |
