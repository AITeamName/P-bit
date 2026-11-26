# ai-server — AI working rules

FastAPI scoring server. It judges the format elements of one prompt per request. Only Spring Boot calls this server.
The root `AGENTS.md` also applies. Inside this folder, this file wins on conflict.
Workflow rules (branches, commits, PRs, approvals) live only in the root `AGENTS.md`. This file holds tech rules only.

Reference documents are written in Korean. Section titles below keep the Korean originals so you can search for them as written.

## 1. Stack and commands

- Python (version undecided — `docs/dev/llm/llm-tech-spec.md` > 1. 스택), uv, FastAPI, Pydantic v2, Uvicorn
- Port 8001. Local run: `uv run --directory ai-server uvicorn src.main:app --reload --port 8001`

Pre-commit routine (run from the repository root)

```bash
uv run --directory ai-server ruff check .
uv run --directory ai-server ruff format --check .
uv run --directory ai-server mypy src
uv run --directory ai-server pytest -q
```

- `ai-server/src/` is still empty and there is no `pyproject.toml`, so this routine cannot run yet. Until then, report it as not run, with this reason. The commit that adds the first code also adds `pyproject.toml` with the ruff, mypy, and pytest config.

## 2. Structure

```
ai-server/
├─ src/            main.py, routers/, services/, schemas/
├─ prompts/rubric/{version}/   structure.yaml + ko/ (examples · phrases · ambiguity)
└─ tests/
```

- Rubrics, thresholds, and sentence templates live in `prompts/rubric/{RUBRIC_VERSION}/`. Do not hardcode them (`docs/dev/llm/model-design.md` > 4.1).
- Define request and response models as Pydantic models in `src/schemas/`. These models are the contract in `interfaces.md` 3장.

## 3. Rules

### Boundaries (`docs/dev/specs/interfaces.md` > 1.2)

- Never connect to the DB. Judge each request on its own input; keep no state across requests. The only allowed state is the 60-second `Idempotency-Key` response cache (`interfaces.md` > 3.11, 5.2).
- Use `channel` (sidebar or web) for logging only, never to branch judgment.
- Never write `prompt_text` to logs or error responses.

### Contract (`docs/dev/specs/interfaces.md` > 3.8)

- Fields are `snake_case`; enums are lowercase. Spring does the conversion.
- `start`/`end` are Python string indexes (code points).
- An unknown enum value is `400 VALIDATION_ERROR`. Do not replace it with a default.
- Add fields only in a backward-compatible way. To remove a field or change its meaning, update `interfaces.md` first and raise `rubric_version`.

### Rubric (`docs/dev/llm/model-design.md` > 4.1)

- Version = directory. Never edit a deployed version directory. To change it, create a new version directory.
- Raise `RUBRIC_VERSION` when the judgment criteria (`structure` · `examples` · `ambiguity`) or the judge model change. Text-only changes that do not affect judgment (`phrases`) get a patch version.

### Judgment (`docs/dev/llm/model-design.md` > 3.1 · 3.2 · 3.3 · 1.10)

- Judge, assemble, and re-judge calls use temperature 0; judge output is forced with `json_schema`.
- One LLM call per prompt for element judgment. Do not split calls per element.
- If `evidence` is not found verbatim in the original text, drop that span and add `SPAN_MISMATCH` to `warnings`.
- If `present: true` has no span, the code sets it back to `false`.
    - When every span of an element is dropped by the rule above, which rule wins is undecided (`model-design.md` > 3.2 keeps `present`, 3.3 sets it to `false`). Ask the user before implementing it.
- `clarity` is decided by the ambiguity detector (code), not the LLM (`model-design.md` > 6장).
- On judgment failure (`degraded: true`), `elements` · fulfillment rate · `level` are `null`. Never make up a level.

### Sentences (`docs/dev/llm/system-design.md` > 1.5, `model-design.md` > 5장)

- Build user-facing sentences only from templates plus injected variables. The LLM never writes them freely.
- Do not use the expressions banned by the copy guardrail. This also applies to `user_message` in error responses.

### Assembly (`docs/dev/llm/model-design.md` > 7장)

- The After prompt must pass `after_set ⊆ before_set ∪ picked_set` and the value match check.
- On violation, regenerate, at most 2 times.

### Config (`docs/ops/env.md` > 2. AI 서버)

- Read models, timeouts, and retries from environment variables (`LLM_MODEL_JUDGE`, `LLM_MODEL_ANSWER`, `LLM_TIMEOUT_*`, `LLM_MAX_RETRY`, `RUBRIC_VERSION`).
- Internal call auth: `X-Internal-Token` = `AI_INTERNAL_TOKEN`. Mismatch returns 401.
- To add a key, follow `docs/ops/env.md` > 8. 새 환경변수를 추가할 때.

## 4. Tests

- Replace LLM calls with mocks so tests run without an API key.
- Mark tests that call the real LLM, and report them separately when stating what was run.
- When judgment logic, the rubric, or the judge model changes, put Before/After measurements in the PR body "측정 결과" (`docs/dev/llm/model-design.md` > 8장).

## 5. Prohibitions

- Do not commit `.env` or API keys.
- Do not create endpoints, fields, or error codes that are not in `interfaces.md`. If needed, update the document first.

## 6. Reference documents

| Work | Read first |
| --- | --- |
| Stack and rationale | `docs/dev/llm/llm-tech-spec.md` |
| Engine layout, call flow, log fields | `docs/dev/llm/system-design.md` |
| Levels, metrics, rubric, judgment, assembly, validation | `docs/dev/llm/model-design.md` |
| Endpoints, schemas, errors, timeouts | `docs/dev/specs/interfaces.md` |
| Environment variables | `docs/ops/env.md` > 2. AI 서버 |
