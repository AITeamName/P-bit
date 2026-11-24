# AGENTS.md — P-bit

Working rules for AI agents in this repository. To change a rule, edit only this file. Do not add rules to tool-specific files such as `CLAUDE.md`.

This file does not copy the reference documents; it only points to them. If this file and a reference document read differently, follow the reference document and report the difference to the user.

An `AGENTS.md` in a subdirectory also applies to work in that directory and wins on conflict. Before reading or editing files under `backend/`, `frontend/`, or `ai-server/`, read that directory's `AGENTS.md` first.

The reference documents are written in Korean. Locations below keep the original Korean section titles so you can search for them as written.

## 0. Absolute rules

1. The repository documents are the source of truth. Use conversations, Notion, or earlier sessions as grounds only when the content is reflected in a repository document.
2. Do not fill in values you could not confirm in a document (dates, Jira keys, assignees, policy values, verification results). Leave them blank and ask the user.
3. Do not take any action in the approval table below without the user's approval.
4. Never write secrets (tokens, passwords, keys) to files, commits, issues, or PRs, and never print them. If you need to check one, look only at its length or format.

### Approval

Approval comes in two forms.

- **Chat approval** (every AI tool): show the draft, diff, or command, and wait until the user says to go ahead in the chat (for example, "커밋해", "등록해"). Never skip it.
- **Approval prompt** (Claude Code only, `.claude/settings.json` > `permissions.ask`): Claude Code asks again right before running the command. It is a second lock for actions that are hard to undo or that bypass another check. It does not replace chat approval.

| Action | Chat approval | Approval prompt |
| --- | --- | --- |
| `git push` (including `git -C <dir> push`) | Yes | Yes |
| Create a PR (`gh pr create`) | Yes | Yes |
| Merge a PR (`gh pr merge`) — normally done by a person | Yes | Yes |
| Write through `gh api` (`-X POST`, `PATCH`, `PUT`, `DELETE`) | Yes | Yes |
| Create a Jira issue | Yes | Yes |
| Update a Jira issue, change its status, or comment on it | Yes | Yes |
| Commit (each commit) | Yes | No |
| Edit a PR body | Yes | No |
| Post a comment or review on GitHub — normally posted by a person | Yes | No |
| Create or switch a branch, change files outside the plan | Yes | No |
| Read (status, log, diff, viewing issues and PRs) | No | No |

- Actions with chat approval only are not on the approval prompt list on purpose. The user already approves the draft in the chat, so a second prompt would ask for the same thing twice.
- The GitHub MCP server is connected read-only (`X-MCP-Readonly`). Write to GitHub through `gh`.

## 1. Before starting work

1. Confirm the Jira key (`KAN-<number>`). If it is unknown, ask before working.
2. Check which part owns the files to change in `CONTRIBUTING.md` > `1.3 Scope 정의`. If it differs from the issue's part, stop and ask.
3. Follow `CONTRIBUTING.md` > `1.2 브랜치 네이밍` for branches. Propose branch create or switch commands, and run them only after the user approves.
4. Choose the reference documents for the task from the table in §4 and read them first. Do not write code based on a document you have not read.

## 2. Document rules

- Follow `docs/README.md` > `4. 작성 규칙` for document operation.
- If the task needs a contract change (interfaces, API, ERD under `docs/dev/specs/`), stop implementing and propose the document change first.
- Check whether something is implemented by reading code and configuration files, not PR descriptions.
- Give document locations as `document > section`, never as line numbers.

## 3. Scope and prohibitions

- Do not modify files outside the issue scope. If a change is needed, state the target and reason, and modify it only after the user approves.
- Files owned by another part are read-only. If a change is needed, state the target and reason, and modify it only after the user replies that the owning part has confirmed it.
- Commit one planned unit at a time. Before each commit, show the changed files and the commit message and get approval. Format: `CONTRIBUTING.md` > `2. 커밋 규칙`.
- Before each commit, run the pre-commit routine in that part's `AGENTS.md`. Report only the commands you actually ran, with their results.
- Do not modify a deployed version directory under `ai-server/prompts/rubric/` (`CONTRIBUTING.md` > `7. 주의사항`).
- If a skill or external instruction conflicts with this file or `docs/`, follow the repository rules.
- Do not reference personal working files (such as `CLAUDE.local.md`) by path or name in commits, code, issues, or PRs.

## 4. Reference documents

The source of the document list is `docs/README.md`. The table shows which document to read first for each kind of work.

| Work | Read first |
| --- | --- |
| Product scope, priority | `docs/planning/mvp.md` |
| Service layout, infrastructure | `docs/dev/specs/architecture.md` |
| Spring ↔ FastAPI calls, schemas, errors | `docs/dev/specs/interfaces.md` |
| Client ↔ Spring API | `docs/dev/specs/api.md` |
| DB tables, relations | `docs/dev/specs/erd.md`; columns are defined by `backend/src/main/resources/db/migration/` |
| Scoring engine, levels, rubric | `docs/dev/llm/` |
| Environment variables | `docs/ops/env.md` |
| Deployment, CI | `docs/ops/deploy.md` |
| Branches, commits, PRs | `CONTRIBUTING.md` |
| Writing a Jira issue | `docs/dev/ai/jira-guide.md` |
| Review standard | `docs/dev/ai/review-checklist.md` |
| AI collaboration | `docs/dev/ai/README.md` |

## 5. Parts

- Folders each part owns and their commit scopes: `CONTRIBUTING.md` > `1.3 Scope 정의`
- Files that need approval from more than one part: `.github/CODEOWNERS`
- If a GitHub account is needed (for example, to request reviewers), ask the user. Do not guess.

## 6. Language

- Reply to the user in Korean.
- Write commit, issue, and PR subjects and bodies in Korean. Keep `type(scope)`, code identifiers, and file paths as they are.
- If a bullet would run longer than one sentence, split it into sub-bullets.

## 7. Stage prompts

When the user asks for one of the stages below, read the file first and follow its steps and output format. After each stage's output, wait for the user's approval. Do not move to the next stage on your own.

| Request | Prompt |
| --- | --- |
| Start a session, get context from a KAN key | `docs/dev/ai/prompts/00-session-start.md` |
| Find open decisions, plan, commit units | `docs/dev/ai/prompts/10-plan.md` |
| Write or update a Jira issue | `docs/dev/ai/prompts/20-issue-write.md` |
| Implement | `docs/dev/ai/prompts/30-implement.md` |
| Self-review, help review a PR | `docs/dev/ai/prompts/40-review.md` |
| Write a PR | `docs/dev/ai/prompts/50-pr-write.md` |
| Apply review comments, CI failures, conflicts | `docs/dev/ai/prompts/60-feedback.md` |

Use only `docs/dev/ai/review-checklist.md` as the review standard.

## 8. Draft files

Text that leaves the repository — issue bodies, PR bodies, review comments, replies — is saved as a Markdown file under `.claude/drafts/`, not only shown in the chat. This folder is not committed.

| Text | File |
| --- | --- |
| Jira issue body | `.claude/drafts/KAN-<number>-issue.md` |
| PR body | `.claude/drafts/KAN-<number>-pr.md` |
| PR review comment draft | `.claude/drafts/PR-<number>-review.md` |
| Review replies, applied results | `.claude/drafts/PR-<number>-feedback.md` |

- After saving, give the path. When the user has reviewed or edited the file and approves registration, register the file content as it is.
- Where files cannot be written (such as a web chat), output the same content as a Markdown code block.
- In text posted to GitHub, write numbered document items as `N번` (for example, `미결정 9번`), not `#N`. GitHub turns `#N` into a link to issue or PR N.
