# frontend — AI working rules

Front end for the web (`frontend/web`) and the Chrome extension (`frontend/extension`).
The root `AGENTS.md` also applies. Inside this folder, this file wins on conflict.
Workflow rules (branches, commits, PRs, approvals) live only in the root `AGENTS.md`. This file holds tech rules only.

Reference documents are written in Korean. Section titles below keep the Korean originals so you can search for them as written.

- Commit scopes: `web` (web) · `ext` (extension) · `fe` (shared files in this folder)
- The Chrome extension has not started, and its tech choices are undecided. Before working in `frontend/extension/`, confirm the scope with the user.

## 1. Stack and commands (web)

- Vite, React 19, TypeScript, npm (`frontend/web/package.json`)
- Node.js version: follow `frontend/web/.nvmrc` and `package.json` > `engines`.
- Routing `react-router`, lint `oxlint`, styles CSS + CSS variables (`src/styles/tokens.css`)
- UI text lives in `src/i18n/` as `ko` · `en` · `ja` JSON. The default locale is `ko`

Pre-commit routine (run in `frontend/web/`; same commands as the CI `web` job)

```bash
npm run lint      # oxlint + message JSON check (same keys in all three locales, no empty ko text)
npm run build     # tsc type check + vite build
```

- Dev server: `npm run dev`, port fixed at 5173 (`strictPort`). The backend CORS allows only `http://localhost:5173`. Open it in the browser as `localhost`.

## 2. Structure (web)

```
frontend/web/src
├─ api         API client, error types and parsing
├─ components  shared UI components
├─ i18n        message JSON (ko · en · ja), locale selection
├─ pages       screens
├─ routes      route definitions, protected routes
├─ stores      auth state
└─ styles      design tokens
```

- Call the server only through the client in `src/api`. Do not call `fetch` directly from screens.
- Write user-facing text as keys in `src/i18n`. When adding text, add the same key to all three locale files.

## 3. Rules — API and auth

- The base URL is the environment variable `VITE_API_BASE_URL` (including `/api/v1`). Do not hardcode hosts.
- The front end never calls the AI server (FastAPI) directly. All requests go to the Spring API (`docs/dev/specs/interfaces.md` > 1.2).
- Send `X-Client-Channel: WEB` and the UI language (`Accept-Language`) on every request. Do not add them by hand; the `src/api` client adds them.
- For APIs that go through AI, send an `Idempotency-Key` (UUID) created by the front end. Reuse the same key on retry (`docs/dev/specs/interfaces.md` > 5.1).
- Keep the access token in memory only. Do not store it in `localStorage` or similar.
- The refresh token arrives as an HttpOnly cookie, so requests use `credentials: 'include'`.
- On a 401 for an expired token, call `POST /auth/refresh` and resend the original request. Even if several requests get 401 at once, send the refresh only once.
- Failure responses are `{ error: { code, userMessage, retryable, requestId, fieldErrors }, fallback }`. Show `userMessage` on screen. If it is missing (`null`: network failure, a response not in the agreed shape), do not make up text; use the i18n default message (`src/api/errors.ts`).
- The Google login start URL (`/oauth2/authorization/google`) is outside the base URL.
- The OAuth callback code is single-use. Prevent calling twice with the same code.

## 4. Tests

- No test tool is installed yet (`package.json` has no test script). Add one only after confirming with the user.
- Until then, verification is the pre-commit routine plus manual checks in the browser. Report only manual checks actually done.

## 5. Prohibitions

- Do not commit `.env`. Keep only key names and examples in `.env.example`.
- Values starting with `VITE_` are exposed to the browser. Never put secrets in them.
- Do not call APIs or fields that are not in the specification by assumption.
- The items below are undecided. Do not implement them on your own; ask the user first.
    - Error codes that separate token expiry from auth failure, and logout when the access token has expired
    - Web and API domain layout in deployment (refresh cookie `SameSite`)
    - Whether terms content includes HTML
    - Design token values for Purple 500/600, semantic tokens

## 6. Reference documents

| Work | Read first |
| --- | --- |
| Client API | `docs/dev/specs/api.md` (in progress — if empty, ask the user) |
| Server layout, domains, cookies | `docs/dev/specs/architecture.md` |
| How AI responses reach the screen | `docs/dev/specs/interfaces.md` > 1.2 · 5.1 |
| Screen scope, done criteria | `docs/planning/mvp.md` |
| Environment variables | `docs/ops/env.md` > 3. 웹 · 4. 익스텐션 |
| How to run | `frontend/web/README.md` |
