# CLAUDE.md

Entry point for Claude Code. The working rules are in `AGENTS.md`, loaded by the line below. Add rules only to `AGENTS.md`.

@AGENTS.md

## Claude Code only

- Use the MCP server `jira` for Jira and the `gh` CLI for GitHub first. Use the GitHub MCP server `github` only for reads that `gh` cannot do.
    - If a connection is not available, ask the user to paste the issue or PR content and continue.
    - Setup: `docs/dev/ai/README.md` > `MCP·CLI 연결`.
- `git push`, Jira issue creation, and PR creation are set to `ask` in `.claude/settings.json`. Even when the approval prompt appears, do not run them without the approval required by `AGENTS.md` §0.
- Save the draft files in `AGENTS.md` §8 as files. Register a PR with `gh pr create --body-file <draft file>`.
- Do not run commands that include secret values. Give the command for the user to run in a separate terminal.
- Personal instructions go in each user's `CLAUDE.local.md` (not committed).
